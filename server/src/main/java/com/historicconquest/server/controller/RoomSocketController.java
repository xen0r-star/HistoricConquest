package com.historicconquest.server.controller;

import com.historicconquest.server.model.map.Zone;
import com.historicconquest.server.model.map.ZonePathfinder;
import com.historicconquest.server.model.questions.Question;
import com.historicconquest.server.model.specialCard.SpecialCard;
import com.historicconquest.server.model.specialCard.SpecialCardFactory;
import com.historicconquest.server.security.StompPrincipal;
import com.historicconquest.server.model.Room;
import com.historicconquest.server.model.player.Player;
import com.historicconquest.server.service.QuestionService;
import com.historicconquest.server.service.RoomService;
import com.historicconquest.server.util.NameGenerator;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.Map;


@Controller
public class RoomSocketController {
    private static final int GAME_COUNTDOWN_SECONDS = 5;
    private static final int ZONE_SELECTION_COUNTDOWN_SECONDS = 30;
    private static final int ANSWER_STREAK_THRESHOLD = 3;
    private static final String ACTION_TRAVEL = "TRAVEL";
    private static final String ACTION_ATTACK = "ATTACK";
    private static final String ACTION_POWER_UP = "POWER_UP";
    private static final String ACTION_SUBMIT_ANSWER = "SUBMIT_ANSWER";
    private static final String ACTION_SKIP = "SKIP";
    private static final String EVENT_BONUS_MALUS = "BONUS_MALUS";
    private static final String STREAK_KIND_BONUS = "BONUS";
    private static final String STREAK_KIND_MALUS = "MALUS";
    private static final String ACTION_COALITION_REQUEST = "COALITION_REQUEST";
    private static final String ACTION_COALITION_ACCEPT = "COALITION_ACCEPT";
    private static final String ACTION_COALITION_DECLINE = "COALITION_DECLINE";
    private static final long BOT_THINKING_MIN_DELAY_MS = 2500L;
    private static final long BOT_THINKING_MAX_DELAY_MS = 4000L;
    private static final long BOT_ANSWER_MIN_DELAY_MS = 4000L;
    private static final long BOT_ANSWER_MAX_DELAY_MS = 8000L;
    private static final Random BOT_RANDOM = new Random();

    private final RoomService roomService;
    private final QuestionService questionService;
    private final SimpMessagingTemplate messagingTemplate;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "room-start-scheduler");
        thread.setDaemon(true);
        return thread;
    });

    public RoomSocketController(RoomService roomService, QuestionService questionService, SimpMessagingTemplate messagingTemplate) {
        this.roomService = roomService;
        this.questionService = questionService;
        this.messagingTemplate = messagingTemplate;
    }

    private boolean isGameFlowLocked(Room room) {
        return room != null && (room.isGameStarting() || room.isZoneSelectionStarted() || room.isGameStarted());
    }

    private void sendActionUnavailable(String playerId, String action, String message) {
        messagingTemplate.convertAndSendToUser(
            playerId,
            "/queue/errors",
            Map.of(
                "type", action,
                "title", "Action unavailable",
                "message", message
            )
        );
    }

    private void broadcastPayload(String roomCode, Map<String, Object> payload) {
        messagingTemplate.convertAndSend(
            "/topic/room-" + roomCode,
            (Object) payload
        );
    }

    private void broadcastTurnChanged(String roomCode, Room room) {
        if (room == null) return;

        messagingTemplate.convertAndSend(
            "/topic/room-" + roomCode,
            (Object) Map.of(
                "type", "TURN_CHANGED",
                "currentPlayerId", room.getCurrentPlayerId(),
                "currentPlayerIndex", room.getCurrentPlayerIndex()
            )
        );
    }

    private void broadcastBonusMalus(String roomCode, Player player, String kind, String nameKind, Map<String, Object> resultSpecialCard) {
        if (roomCode == null || player == null || kind == null) return;

        broadcastPayload(roomCode, Map.of(
            "type", EVENT_BONUS_MALUS,
            "playerId", player.getId(),
            "kind", kind,
            "nameKind", nameKind,
            "resultSpecialCard", resultSpecialCard
        ));
    }


    private void processBotTurns(String roomCode, Room room) {
        if (room == null || !room.isGameStarted()) return;
        processBotTurns(roomCode, room, room.getPlayerOrder().size());
    }

    private void processBotTurns(String roomCode, Room room, int remainingSkips) {
        if (room == null || !room.isGameStarted() || remainingSkips <= 0) return;

        String currentId = room.getCurrentPlayerId();
        if (currentId == null) return;

        Player current = room.getPlayerById(currentId);
        if (current == null || current.getType() != Player.Type.Bot) {
            return;
        }

        long delay = ThreadLocalRandom.current().nextLong(BOT_THINKING_MIN_DELAY_MS, BOT_THINKING_MAX_DELAY_MS);
        scheduler.schedule(() -> executeBotTurn(roomCode, remainingSkips), delay, TimeUnit.MILLISECONDS);
    }

    private void executeBotTurn(String roomCode, int remainingSkips) {
        Room room = roomService.getRoom(roomCode);
        if (room == null || !room.isGameStarted() || remainingSkips <= 0) return;

        String currentId = room.getCurrentPlayerId();
        Player current = currentId == null ? null : room.getPlayerById(currentId);
        if (current == null || current.getType() != Player.Type.Bot) return;

        BotAction choice = selectBotAction(room, currentId);
        if (choice == null) {
            room.advanceTurn();
            broadcastTurnChanged(roomCode, room);
            processBotTurns(roomCode, room, remainingSkips - 1);
            return;
        }

        room.setPendingAction(choice.action());
        room.setPendingZone(choice.zoneName());

        broadcastPayload(roomCode, Map.of(
            "type", "ACTION_SELECTED",
            "action", choice.action(),
            "playerId", currentId,
            "zone", choice.zoneName(),
            "difficulty", choice.difficulty()
        ));

        long delay = ThreadLocalRandom.current().nextLong(BOT_ANSWER_MIN_DELAY_MS, BOT_ANSWER_MAX_DELAY_MS);
        scheduler.schedule(() -> resolveBotAnswer(roomCode, currentId, choice, remainingSkips), delay, TimeUnit.MILLISECONDS);
    }

    private void resolveBotAnswer(String roomCode, String playerId, BotAction choice, int remainingSkips) {
        Room room = roomService.getRoom(roomCode);
        if (room == null || !room.isGameStarted()) return;

        String currentId = room.getCurrentPlayerId();
        if (currentId == null || !currentId.equals(playerId)) return;

        boolean correct = rollBotAnswer(choice.difficulty());
        if (correct) {
            applyActionToRoom(room, playerId, choice.action(), choice.zoneName(), choice.difficulty());

            Map<String, Object> moveMsg = new HashMap<>();
            moveMsg.put("type", "GAME_ACTION");
            moveMsg.put("action", choice.action());
            moveMsg.put("zone", choice.zoneName());
            moveMsg.put("playerId", playerId);
            moveMsg.put("difficulty", choice.difficulty());
            broadcastPayload(roomCode, moveMsg);
        }

        updateAnswerStreak(room, roomCode, playerId, correct);

        broadcastPayload(roomCode, Map.of(
            "type", "ANSWER_RESULT",
            "playerId", playerId,
            "correct", correct,
            "difficulty", choice.difficulty()
        ));

        Player winner = room.checkWinner();
        if (winner != null) {
            broadcastPayload(roomCode, Map.of(
                "type", "GAME_WON",
                "winnerName", winner.getPseudo()
            ));
            roomService.deleteRoom(roomCode);
            return;
        }

        room.advanceTurn();
        broadcastTurnChanged(roomCode, room);
        processBotTurns(roomCode, room, remainingSkips - 1);
    }

    private BotAction selectBotAction(Room room, String playerId) {
        String currentZoneName = room.getCurrentZoneName(playerId);
        Zone currentZone = currentZoneName == null ? null : room.getZone(currentZoneName);
        if (currentZone == null) return null;

        String ownerId = currentZone.getNameOwner();
        if (ownerId == null || !ownerId.equals(playerId)) {
            int difficulty = pickDifficulty(4);
            return new BotAction(ACTION_ATTACK, currentZone.getName(), difficulty);
        }

        if (currentZone.getPower() < currentZone.getMAX_POWER_ZONE() && BOT_RANDOM.nextDouble() < 0.45) {
            int maxBoost = Math.min(4, currentZone.getMAX_POWER_ZONE() - currentZone.getPower());
            int difficulty = pickDifficulty(maxBoost);
            return new BotAction(ACTION_POWER_UP, currentZone.getName(), difficulty);
        }

        BotAction travel = selectTravelAction(currentZone, playerId);
        if (travel != null) return travel;

        int difficulty = pickDifficulty(4);
        return new BotAction(ACTION_POWER_UP, currentZone.getName(), difficulty);
    }

    private BotAction selectTravelAction(Zone currentZone, String playerId) {
        List<Zone> inRange = List.copyOf(ZonePathfinder.getZonesWithinRange(currentZone, 4));
        inRange = inRange.stream().filter(zone -> zone != currentZone).toList();
        if (inRange.isEmpty()) return null;

        Zone target = pickEnemyZone(inRange, playerId);
        if (target == null) {
            target = inRange.get(BOT_RANDOM.nextInt(inRange.size()));
        }

        var distance = ZonePathfinder.getShortestDistance(currentZone, target, 4);
        int difficulty = Math.clamp(distance.distance(), 1, 4);
        return new BotAction(ACTION_TRAVEL, target.getName(), difficulty);
    }

    private Zone pickEnemyZone(List<Zone> zones, String playerId) {
        List<Zone> enemies = zones.stream()
            .filter(zone -> zone.getNameOwner() == null || !zone.getNameOwner().equals(playerId))
            .toList();

        if (enemies.isEmpty()) return null;
        return enemies.get(BOT_RANDOM.nextInt(enemies.size()));
    }

    private int pickDifficulty(int maxLevel) {
        int max = Math.clamp(maxLevel, 1, 4);
        double roll = BOT_RANDOM.nextDouble();
        if (roll < 0.35) return Math.min(1, max);
        if (roll < 0.65) return Math.min(2, max);
        if (roll < 0.85) return Math.min(3, max);
        return max;
    }

    private boolean rollBotAnswer(int difficulty) {
        return BOT_RANDOM.nextDouble() < switch (difficulty) {
            case 1 -> 0.85;
            case 2 -> 0.70;
            case 3 -> 0.55;
            case 4 -> 0.35;
            default -> 0.50;
        };
    }

    private void updateAnswerStreak(Room room, String roomCode, String playerId, boolean correct) {
        if (room == null || roomCode == null || playerId == null) return;

        Player player = room.getPlayerById(playerId);
        if (player == null) return;

        if (correct) {
            player.setConsecutiveSuccesses(player.getConsecutiveSuccesses() + 1);
            player.setConsecutiveFailures(0);

            if (player.getConsecutiveSuccesses() >= ANSWER_STREAK_THRESHOLD) {
                SpecialCard specialCard = SpecialCardFactory.drawBonus();
                Map<String, Object> resultSpecialCard = specialCard.apply(room, player);

                broadcastBonusMalus(
                    roomCode,
                    player,
                    STREAK_KIND_BONUS,
                    specialCard.getName(),
                    resultSpecialCard
                );
                player.resetStreaks();
            }

            return;
        }

        player.setConsecutiveFailures(player.getConsecutiveFailures() + 1);
        player.setConsecutiveSuccesses(0);

        if (player.getConsecutiveFailures() >= ANSWER_STREAK_THRESHOLD) {
            SpecialCard specialCard = SpecialCardFactory.drawBonus();
            Map<String, Object> resultSpecialCard = specialCard.apply(room, player);

            broadcastBonusMalus(
                roomCode,
                player,
                STREAK_KIND_MALUS,
                specialCard.getName(),
                resultSpecialCard
            );
            player.resetStreaks();
        }
    }

    private void applyActionToRoom(Room room, String playerId, String action, String zoneName, int difficulty) {
        if (room == null || playerId == null || action == null || zoneName == null) return;

        Zone target = room.getZone(zoneName);
        if (target == null) return;

        switch (action) {
            case ACTION_TRAVEL -> room.setCurrentZoneName(playerId, zoneName);
            case ACTION_ATTACK -> applyAttack(room, playerId, target, difficulty);
            case ACTION_POWER_UP -> applyPowerUp(playerId, target, difficulty);
            default -> { }
        }
    }

    private void applyAttack(Room room, String playerId, Zone target, int difficulty) {
        String currentZone = room.getCurrentZoneName(playerId);
        if (!target.getName().equalsIgnoreCase(currentZone)) return;
        if (playerId.equals(target.getNameOwner())) return;

        int result = target.getPower() - difficulty;
        if (result > 0) {
            target.setPower(result);
            return;
        }

        if (result == 0) {
            target.setPower(0);
            target.setNameOwner("Nobody");
            return;
        }

        target.setPower(Math.abs(result));
        target.setNameOwner(playerId);
    }

    private void applyPowerUp(String playerId, Zone target, int difficulty) {
        if (!playerId.equals(target.getNameOwner())) return;

        int maxPower = target.getMAX_POWER_ZONE();
        int newPower = Math.min(target.getPower() + difficulty, maxPower);
        target.setPower(newPower);
    }

    private record BotAction(String action, String zoneName, int difficulty) { }



    @MessageMapping("/addBot")
    public void addBot(Principal principal) {
        StompPrincipal sp = (StompPrincipal) principal;
        String playerId = sp.getName();
        String roomCode = sp.getRoomCode();

        Room room = roomService.getRoom(roomCode);
        if (room == null) {
            sendActionUnavailable(playerId, "ADD_BOT", "Room does not exist");
            return;
        }

        Player player = room.getPlayerById(playerId);

        if (isGameFlowLocked(room)) {
            sendActionUnavailable(
                playerId,
                "ADD_BOT",
                room.isGameStarting() ? "A game countdown is already in progress" :
                    room.isZoneSelectionStarted() ? "Zone selection is already in progress" :
                        "A game is already started"
            );

            return;
        }

        if (room.isHost(player.getId())) {
            Player newBot = new Player(NameGenerator.get(), Player.Type.Bot, room.getCode(), Player.Status.Ready);

            try {
                roomService.addPlayer(room.getCode(),  newBot);

            } catch (Exception e) {
                messagingTemplate.convertAndSendToUser(
                    playerId,
                    "/queue/errors",
                    Map.of(
                        "type", "ADD_BOT",
                        "title", "Failed to add bot",
                        "message", e.getMessage()
                    )
                );
            }

            broadcastPayload(roomCode, Map.of(
                "type", "PLAYER_JOIN",
                "player", newBot
            ));

        } else {
            messagingTemplate.convertAndSendToUser(
                playerId,
                "/queue/errors",
                Map.of(
                    "type", "ADD_BOT",
                    "title", "Failed to add bot",
                    "message", "Only the host can add bots"
                )
            );
        }
    }

    @MessageMapping("/start")
    public void startGame(Principal principal) {
        StompPrincipal sp = (StompPrincipal) principal;
        String playerId = sp.getName();
        String roomCode = sp.getRoomCode();

        Room room = roomService.getRoom(roomCode);
        if (room == null) {
            messagingTemplate.convertAndSendToUser(
                playerId,
                "/queue/errors",
                Map.of(
                    "type", "START_GAME",
                    "title", "Failed to start game",
                    "message", "Room does not exist"
                )
            );
            return;
        }

        if (room.isGameStarting() || room.isGameStarted()) {
            sendActionUnavailable(
                playerId,
                "START_GAME",
                room.isGameStarting() ? "A game countdown is already in progress" : "A game is already started"
            );
            return;
        }

        Player player = room.getPlayerById(playerId);
        if (player == null || !room.isHost(player.getId())) {
            messagingTemplate.convertAndSendToUser(
                playerId,
                "/queue/errors",
                Map.of(
                    "type", "START_GAME",
                    "title", "Failed to start game",
                    "message", "Only the host can start the game"
                )
            );
            return;
        }

        try {
            long startAt = roomService.startGame(roomCode);

            broadcastPayload(roomCode, Map.of(
                "type", "GAME_COUNTDOWN_STARTED",
                "seconds", GAME_COUNTDOWN_SECONDS,
                "startAt", startAt
            ));

            scheduler.schedule(() -> {
                Room currentRoom = roomService.getRoom(roomCode);
                if (currentRoom == null) return;
                if (!currentRoom.isGameStarting()) return;


                if (!roomService.stillCanStartGame(roomCode)) {
                    roomService.cancelGameStart(roomCode);
                    broadcastPayload(roomCode, Map.of(
                        "type", "GAME_START_CANCELLED",
                        "reason", "The room is no longer full and ready"
                    ));
                    return;
                }

                try {
                    currentRoom.generateWorldMap();
                    RoomService.ZoneSelectionStart selectionStart = roomService.startZoneSelection(roomCode);

                    broadcastPayload(roomCode, Map.of(
                        "type", "ZONE_SELECTION_STARTED",
                        "seconds", ZONE_SELECTION_COUNTDOWN_SECONDS,
                        "startAt", selectionStart.startAt(),
                        "selectedZones", selectionStart.selectedZones()
                    ));

                    scheduler.schedule(() -> {
                        Room latestRoom = roomService.getRoom(roomCode);

                        if (latestRoom == null || latestRoom.isGameStarted()) return;
                        if (!latestRoom.isZoneSelectionStarted()) return;

                        try {
                            RoomService.ZoneSelectionUpdate finalSelection = roomService.completeZoneSelection(roomCode);
                            broadcastPayload(roomCode, Map.of(
                                "type", "GAME_STARTED",
                                "selectedZones", finalSelection.selectedZones(),
                                "turnOrder", latestRoom.getPlayerOrder(),
                                "currentPlayerId", latestRoom.getCurrentPlayerId(),
                                "listThemeZone", latestRoom.getAllThemeZone()
                            ));
                            latestRoom.initializeGameState(finalSelection.selectedZones());
                            broadcastTurnChanged(roomCode, latestRoom);
                            processBotTurns(roomCode, latestRoom);

                        } catch (Exception ignored) { }
                    }, ZONE_SELECTION_COUNTDOWN_SECONDS, TimeUnit.SECONDS);

                } catch (Exception e) {
                    roomService.cancelGameStart(roomCode);
                    messagingTemplate.convertAndSendToUser(
                        playerId,
                        "/queue/errors",
                        Map.of(
                            "type", "START_GAME",
                            "title", "Failed to start game",
                            "message", e.getMessage() == null ? "An error has occurred" : e.getMessage()
                        )
                    );
                }
            }, GAME_COUNTDOWN_SECONDS, TimeUnit.SECONDS);

        } catch (Exception e) {
            sendActionUnavailable(playerId, "START_GAME", "An error has occurred");
        }
    }

    @MessageMapping("/start/cancel")
    public void cancelGameStart(Principal principal) {
        StompPrincipal sp = (StompPrincipal) principal;
        String playerId = sp.getName();
        String roomCode = sp.getRoomCode();

        Room room = roomService.getRoom(roomCode);
        if (room == null) {
            sendActionUnavailable(playerId, "CANCEL_START", "Room does not exist");
            return;
        }

        Player player = room.getPlayerById(playerId);
        if (player == null || !room.isHost(player.getId())) {
            messagingTemplate.convertAndSendToUser(
                playerId,
                "/queue/errors",
                Map.of(
                    "type", "CANCEL_START",
                    "title", "Failed to cancel game start",
                    "message", "Only the host can cancel the game launch"
                )
            );
            return;
        }

        if (!room.isGameStarting()) {
            sendActionUnavailable(
                playerId,
                "CANCEL_START",
                room.isZoneSelectionStarted() ? "Zone selection is already in progress" : "There is no game countdown to cancel"
            );
            return;
        }

        roomService.cancelGameStart(roomCode);

        broadcastPayload(roomCode, Map.of(
            "type", "GAME_START_CANCELLED",
            "reason", "The host cancelled the game launch"
        ));
    }


    @MessageMapping("/zone/select")
    public void selectZone(@Payload Map<String, String> payload, Principal principal) {
        StompPrincipal sp = (StompPrincipal) principal;
        String playerId = sp.getName();
        String roomCode = sp.getRoomCode();

        Room room = roomService.getRoom(roomCode);
        if (room == null) {
            sendActionUnavailable(playerId, "ZONE_SELECT", "Room does not exist");
            return;
        }

        if (!room.isZoneSelectionStarted()) {
            sendActionUnavailable(playerId, "ZONE_SELECT", "Zone selection is not active");
            return;
        }

        try {
            RoomService.ZoneSelectionUpdate update = roomService.selectZone(roomCode, playerId, payload.get("zone"));

            broadcastPayload(roomCode, Map.of(
                "type", "ZONE_SELECTION_UPDATED",
                "selectedZones", update.selectedZones()
            ));

        } catch (Exception e) {
            sendActionUnavailable(
                playerId,
                "ZONE_SELECT",
                e.getMessage() == null ? "Unable to select this zone" : e.getMessage()
            );
        }
    }


    @MessageMapping("/update")
    public void updateData(@Payload Map<String, String> payload, Principal principal) {
        StompPrincipal sp = (StompPrincipal) principal;
        String playerId = sp.getName();
        String roomCode = sp.getRoomCode();

        String type = payload.get("type");
        String data = payload.get("data");
        String action = type == null ? "UPDATE_DATA" : type;

        Room room = roomService.getRoom(roomCode);
        if (room == null) {
            sendActionUnavailable(playerId, type == null ? "UPDATE_DATA" : type, "Room does not exist");
            return;
        }

        Player player = room.getPlayerById(playerId);

        if (type == null || type.isBlank()) {
            sendActionUnavailable(playerId, "UPDATE_DATA", "Invalid action payload");
            return;
        }


        if (room.isZoneSelectionStarted() || room.isGameStarted()) {
            sendActionUnavailable(
                playerId,
                action,
                room.isZoneSelectionStarted() ? "Zone selection is already in progress" :
                    "A game is already started"
            );
            return;
        }

        switch (type) {
            case "PLAYER_COLOR_CHANGE":
                boolean colorIsUsed = roomService.getUsedColors(roomCode, playerId).stream()
                    .anyMatch(usedColor -> usedColor.equalsIgnoreCase(data));

                if (colorIsUsed) {
                    messagingTemplate.convertAndSendToUser(
                        playerId,
                        "/queue/errors",
                        Map.of(
                            "type", "PLAYER_COLOR_CHANGE",
                            "title", "Failed to change color",
                            "message", "This color is already taken"
                        )
                    );
                    return;
                }

                player.setColor(data);
                break;

            case "PLAYER_PSEUDO_CHANGE":
                if (!roomService.pseudoIsUsed(roomCode, data, playerId)) {
                    player.setPseudo(data);

                } else {
                    messagingTemplate.convertAndSendToUser(
                        playerId,
                        "/queue/errors",
                        Map.of(
                            "type", "PLAYER_PSEUDO_CHANGE",
                            "title", "Failed to change pseudo",
                            "message", "This pseudo is already taken"
                        )
                    );
                    return;
                }
                break;

            case "PLAYER_STATUS_CHANGE":
                player.setStatus(Player.Status.valueOf(data));
                break;
        }


        broadcastPayload(roomCode, Map.of(
            "type", type,
            "playerId", playerId,
            "data", data
        ));
    }


    @MessageMapping("/quit")
    public void quitRoom(Principal principal) {
        StompPrincipal sp = (StompPrincipal) principal;
        String playerId = sp.getName();
        String roomCode = sp.getRoomCode();

        Room room = roomService.getRoom(roomCode);
        boolean cancelFlow = isGameFlowLocked(room);

        String allyId = room == null ? null : room.getAlliancePartner(playerId);
        if (room != null) {
            room.clearAllianceForPlayer(playerId);
            room.clearPendingAllianceRequest(playerId);
            room.clearPendingAllianceRequestsFrom(playerId);
        }

        roomService.removePlayer(roomCode, playerId);


        broadcastPayload(roomCode, Map.of(
            "type", "PLAYER_QUIT",
            "playerId", playerId
        ));

        if (cancelFlow && roomService.getRoom(roomCode) != null) {
            roomService.cancelGameStart(roomCode);
            broadcastPayload(roomCode, Map.of(
                "type", "GAME_START_CANCELLED",
                "reason", "A player left the room"
            ));
        }

        if (allyId != null && roomService.getRoom(roomCode) != null) {
            broadcastPayload(roomCode, Map.of(
                "type", "COALITION_BROKEN",
                "playerAId", playerId,
                "playerBId", allyId
            ));
        }
    }


    @MessageMapping("/kick")
    public void kickRoom(@Payload Map<String, String> payload, Principal principal) {
        StompPrincipal sp = (StompPrincipal) principal;
        String playerId = sp.getName();
        String roomCode = sp.getRoomCode();

        Room room = roomService.getRoom(roomCode);
        if (room == null) {
            sendActionUnavailable(playerId, "KICK_PLAYER", "Room does not exist");
            return;
        }

        Player player = room.getPlayerById(playerId);

        if (isGameFlowLocked(room)) {
            sendActionUnavailable(
                playerId,
                "KICK_PLAYER",
                room.isGameStarting() ? "A game countdown is already in progress" :
                    room.isZoneSelectionStarted() ? "Zone selection is already in progress" :
                        "A game is already started"
            );
            return;
        }

        if (room.isHost(player.getId())) {
            String playerIdToKick = payload.get("playerId");
            String allyId = room.getAlliancePartner(playerIdToKick);
            room.clearAllianceForPlayer(playerIdToKick);
            room.clearPendingAllianceRequest(playerIdToKick);
            room.clearPendingAllianceRequestsFrom(playerIdToKick);
            roomService.removePlayer(roomCode, playerIdToKick);

            broadcastPayload(roomCode, Map.of(
                "type", "PLAYER_KICK",
                "playerId", playerIdToKick
            ));

            if (allyId != null && roomService.getRoom(roomCode) != null) {
                broadcastPayload(roomCode, Map.of(
                    "type", "COALITION_BROKEN",
                    "playerAId", playerIdToKick,
                    "playerBId", allyId
                ));
            }

        } else {
            messagingTemplate.convertAndSendToUser(
                playerId,
                "/queue/errors",
                Map.of(
                    "type", "KICK_PLAYER",
                    "title", "Failed to kick player",
                    "message", "Only the host can kick players"
                )
            );
        }
    }


    @MessageMapping("/delete")
    public void deleteRoom(Principal principal) {
        StompPrincipal sp = (StompPrincipal) principal;
        String playerId = sp.getName();
        String roomCode = sp.getRoomCode();

        Room room = roomService.getRoom(roomCode);
        if (room == null) {
            sendActionUnavailable(playerId, "DELETE_ROOM", "Room does not exist");
            return;
        }

        Player player = room.getPlayerById(playerId);

        if (isGameFlowLocked(room)) {
            sendActionUnavailable(
                playerId,
                "DELETE_ROOM",
                room.isGameStarting() ? "A game countdown is already in progress" :
                    room.isZoneSelectionStarted() ? "Zone selection is already in progress" :
                        "A game is already started"
            );
            return;
        }

        if (room.isHost(player.getId())) {
            roomService.deleteRoom(roomCode);

            broadcastPayload(roomCode, Map.of(
                "type", "ROOM_DELETED"
            ));

        } else {
            messagingTemplate.convertAndSendToUser(
                playerId,
                "/queue/errors",
                Map.of(
                    "type", "DELETE_ROOM",
                    "title", "Failed to delete room",
                    "message", "Only the host can delete the room"
                )
            );
        }
    }
    @MessageMapping("/game/coalition/request")
    public void requestCoalition(@Payload Map<String, String> payload, Principal principal) {
        StompPrincipal sp = (StompPrincipal) principal;
        String playerId = sp.getName();
        String roomCode = sp.getRoomCode();

        Room room = roomService.getRoom(roomCode);
        if (room == null) {
            sendActionUnavailable(playerId, ACTION_COALITION_REQUEST, "Room does not exist");
            return;
        }

        if (!room.isGameStarted()) {
            sendActionUnavailable(playerId, ACTION_COALITION_REQUEST, "Game is not started");
            return;
        }

        String targetPlayerId = payload == null ? null : payload.get("targetPlayerId");
        if (targetPlayerId == null || targetPlayerId.isBlank()) {
            sendActionUnavailable(playerId, ACTION_COALITION_REQUEST, "Missing target player");
            return;
        }

        if (playerId.equals(targetPlayerId)) {
            sendActionUnavailable(playerId, ACTION_COALITION_REQUEST, "You cannot request an alliance with yourself");
            return;
        }

        if (room.getPlayerById(targetPlayerId) == null) {
            sendActionUnavailable(playerId, ACTION_COALITION_REQUEST, "Target player not found");
            return;
        }

        if (!room.setPendingAllianceRequest(playerId, targetPlayerId)) {
            sendActionUnavailable(playerId, ACTION_COALITION_REQUEST, "Alliance request cannot be sent");
            return;
        }

        messagingTemplate.convertAndSendToUser(
            targetPlayerId,
            "/queue/reply",
            Map.of(
                "type", "COALITION_REQUESTED",
                "requesterId", playerId,
                "targetId", targetPlayerId
            )
        );
    }

    @MessageMapping("/game/coalition/accept")
    public void acceptCoalition(@Payload Map<String, String> payload, Principal principal) {
        StompPrincipal sp = (StompPrincipal) principal;
        String playerId = sp.getName();
        String roomCode = sp.getRoomCode();

        Room room = roomService.getRoom(roomCode);
        if (room == null) {
            sendActionUnavailable(playerId, ACTION_COALITION_ACCEPT, "Room does not exist");
            return;
        }

        if (!room.isGameStarted()) {
            sendActionUnavailable(playerId, ACTION_COALITION_ACCEPT, "Game is not started");
            return;
        }

        String requesterId = payload == null ? null : payload.get("requesterId");
        if (requesterId == null || requesterId.isBlank()) {
            requesterId = room.getPendingAllianceRequester(playerId);
        }

        if (requesterId == null || requesterId.isBlank()) {
            sendActionUnavailable(playerId, ACTION_COALITION_ACCEPT, "No pending alliance request");
            return;
        }

        String pendingRequester = room.getPendingAllianceRequester(playerId);
        if (pendingRequester == null || !pendingRequester.equals(requesterId)) {
            sendActionUnavailable(playerId, ACTION_COALITION_ACCEPT, "Alliance request no longer valid");
            return;
        }

        if (room.getPlayerById(requesterId) == null) {
            sendActionUnavailable(playerId, ACTION_COALITION_ACCEPT, "Requester not found");
            return;
        }

        if (room.hasAlliance(playerId) || room.hasAlliance(requesterId)) {
            sendActionUnavailable(playerId, ACTION_COALITION_ACCEPT, "One of the players is already allied");
            return;
        }

        room.clearPendingAllianceRequest(playerId);
        room.clearPendingAllianceRequestsFrom(requesterId);

        String allianceColor = room.createAlliance(requesterId, playerId);

        broadcastPayload(roomCode, Map.of(
            "type", "COALITION_ACCEPTED",
            "playerAId", requesterId,
            "playerBId", playerId,
            "allianceColor", allianceColor
        ));
    }

    @MessageMapping("/game/coalition/decline")
    public void declineCoalition(@Payload Map<String, String> payload, Principal principal) {
        StompPrincipal sp = (StompPrincipal) principal;
        String playerId = sp.getName();
        String roomCode = sp.getRoomCode();

        Room room = roomService.getRoom(roomCode);
        if (room == null) {
            sendActionUnavailable(playerId, ACTION_COALITION_DECLINE, "Room does not exist");
            return;
        }

        if (!room.isGameStarted()) {
            sendActionUnavailable(playerId, ACTION_COALITION_DECLINE, "Game is not started");
            return;
        }

        String requesterId = payload == null ? null : payload.get("requesterId");
        if (requesterId == null || requesterId.isBlank()) {
            requesterId = room.getPendingAllianceRequester(playerId);
        }

        String pendingRequester = room.getPendingAllianceRequester(playerId);
        if (pendingRequester == null || !pendingRequester.equals(requesterId)) {
            sendActionUnavailable(playerId, ACTION_COALITION_DECLINE, "No pending alliance request");
            return;
        }

        room.clearPendingAllianceRequest(playerId);
        room.clearPendingAllianceRequestsFrom(requesterId);

        broadcastPayload(roomCode, Map.of(
            "type", "COALITION_DECLINED",
            "requesterId", requesterId,
            "targetId", playerId
        ));
    }

    @MessageMapping("/game/action")
    public void handleGameAction(@Payload Map<String, Object> payload, Principal principal) {
        StompPrincipal sp = (StompPrincipal) principal;
        String playerId = sp.getName();
        String roomCode = sp.getRoomCode();

        Room room = roomService.getRoom(roomCode);
        if (room == null) {
            sendActionUnavailable(playerId, "GAME_ACTION", "Room does not exist");
            return;
        }

        if (!room.isGameStarted()) {
            sendActionUnavailable(playerId, "GAME_ACTION", "Game is not started");
            return;
        }

        String currentPlayerId = room.getCurrentPlayerId();
        if (currentPlayerId == null || !currentPlayerId.equals(playerId)) {
            sendActionUnavailable(playerId, "GAME_ACTION", "It is not your turn");
            return;
        }

        String action = payload == null ? null : String.valueOf(payload.get("action"));
        if (action == null || action.isBlank()) {
            sendActionUnavailable(playerId, "GAME_ACTION", "Invalid action payload");
            return;
        }

        if (
            !ACTION_TRAVEL.equals(action) && !ACTION_ATTACK.equals(action) &&
            !ACTION_POWER_UP.equals(action) && !ACTION_SUBMIT_ANSWER.equals(action) &&
            !ACTION_SKIP.equals(action)
        ) {
            sendActionUnavailable(playerId, "GAME_ACTION", "Unknown action");
            return;
        }


        Object zoneName = payload.get("zone");
        Object difficulty = payload.get("difficulty");
        Object questionId = payload.get("questionId");
        Object answer = payload.get("answer");

        if (ACTION_SUBMIT_ANSWER.equals(action)) {
            if (questionId == null || answer == null) {
                sendActionUnavailable(playerId, "GAME_ACTION", "Missing answer data");
                return;
            }

        } else if (ACTION_ATTACK.equals(action) || ACTION_POWER_UP.equals(action) || ACTION_TRAVEL.equals(action)) {
            if (zoneName == null || zoneName.toString().isBlank() || !(difficulty instanceof Number)) {
                sendActionUnavailable(playerId, "GAME_ACTION", "Missing target zone");
                return;
            }

            room.setPendingAction(action);
            room.setPendingZone((String) zoneName);
        }


        switch (action) {
            case "TRAVEL", "ATTACK", "POWER_UP" -> {
                Zone zone = roomService.getRoom(roomCode).getZone((String) zoneName);

                Question question;
                if (difficulty instanceof Integer number) {
                    question = questionService.getRandomQuestion(zone.getThemes(), number);

                } else {
                    question = questionService.getRandomQuestion(zone.getThemes(), 4);
                }

                Number number = (Number) difficulty;
                broadcastPayload(roomCode, Map.of(
                    "type", "ACTION_SELECTED",
                    "action", action,
                    "playerId", playerId,
                    "zone", zoneName,
                    "difficulty", number.intValue()
                ));


                messagingTemplate.convertAndSendToUser(
                    playerId,
                    "/queue/reply",
                    Map.of(
                        "type", "QUESTION_CHALLENGE",
                        "questionId", question.id(),
                        "theme", question.theme(),
                        "subject", question.subject(),
                        "difficulty", question.difficulty(),
                        "question", question.question(),
                        "choices", question.choices()
                    )
                );
            }
            case "SUBMIT_ANSWER" -> {
                Question question = questionService.getQuestion((String) questionId);
                boolean isCorrect = question.answer().equalsIgnoreCase(String.valueOf(answer));

                updateAnswerStreak(room, roomCode, playerId, isCorrect);

                if (isCorrect) {
                    applyActionToRoom(room, playerId, room.getPendingAction(), room.getPendingZone(), question.difficulty());
                    Map<String, Object> moveMsg = new HashMap<>();
                    moveMsg.put("type", "GAME_ACTION");
                    moveMsg.put("action", room.getPendingAction());
                    moveMsg.put("zone", room.getPendingZone());
                    moveMsg.put("playerId", playerId);
                    moveMsg.put("difficulty", question.difficulty());

                    broadcastPayload(roomCode, moveMsg);
                }

                broadcastPayload(roomCode, Map.of(
                    "type", "ANSWER_RESULT",
                    "playerId", playerId,
                    "correct", isCorrect,
                    "difficulty", question.difficulty()
                ));

                Player winner = room.checkWinner();
                if (winner != null) {
                    broadcastPayload(roomCode, Map.of(
                        "type", "GAME_WON",
                        "winnerName", winner.getPseudo()
                    ));
                    roomService.deleteRoom(roomCode);
                    return;
                }

                room.advanceTurn();
                broadcastTurnChanged(roomCode, room);
                processBotTurns(roomCode, room);
            }
            case "SKIP" -> {
                room.advanceTurn();
                broadcastTurnChanged(roomCode, room);
                processBotTurns(roomCode, room);
            }
        }
    }
}