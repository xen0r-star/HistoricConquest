package com.historicconquest.server.controller;

import com.historicconquest.server.model.map.Zone;
import com.historicconquest.server.model.questions.Question;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Map;


@Controller
public class RoomSocketController {
    private static final int GAME_COUNTDOWN_SECONDS = 5;
    private static final String ACTION_TRAVEL = "TRAVEL";
    private static final String ACTION_ATTACK = "ATTACK";
    private static final String ACTION_POWER_UP = "POWER_UP";
    private static final String ACTION_SUBMIT_ANSWER = "SUBMIT_ANSWER";
    private static final String ACTION_SKIP = "SKIP";

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

    private void broadcastGameAction(String roomCode, Map<String, Object> payload) {
        messagingTemplate.convertAndSend(
            "/topic/room-" + roomCode,
            (Object) payload
        );
    }



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

            messagingTemplate.convertAndSend(
                "/topic/room-" + roomCode,
                (Object) Map.of(
                    "type", "PLAYER_JOIN",
                    "player", newBot
                )
            );

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

            messagingTemplate.convertAndSend(
                "/topic/room-" + roomCode,
                (Object) Map.of(
                    "type", "GAME_COUNTDOWN_STARTED",
                    "seconds", GAME_COUNTDOWN_SECONDS,
                    "startAt", startAt
                )
            );

            scheduler.schedule(() -> {
                Room currentRoom = roomService.getRoom(roomCode);
                if (currentRoom == null) return;
                if (!currentRoom.isGameStarting()) return;


                if (!roomService.stillCanStartGame(roomCode)) {
                    roomService.cancelGameStart(roomCode);
                    messagingTemplate.convertAndSend(
                        "/topic/room-" + roomCode,
                        (Object) Map.of(
                            "type", "GAME_START_CANCELLED",
                            "reason", "The room is no longer full and ready"
                        )
                    );
                    return;
                }

                try {
                    RoomService.ZoneSelectionStart selectionStart = roomService.startZoneSelection(roomCode);

                    messagingTemplate.convertAndSend(
                        "/topic/room-" + roomCode,
                        (Object) Map.of(
                            "type", "ZONE_SELECTION_STARTED",
                            "seconds", 30,
                            "startAt", selectionStart.startAt(),
                            "selectedZones", selectionStart.selectedZones()
                        )
                    );

                    currentRoom.generateWorldMap();

                    scheduler.schedule(() -> {
                        Room latestRoom = roomService.getRoom(roomCode);
                        if (latestRoom == null || latestRoom.isGameStarted()) return;
                        if (!latestRoom.isZoneSelectionStarted()) return;

                        try {
                            RoomService.ZoneSelectionUpdate finalSelection = roomService.completeZoneSelection(roomCode);
                            messagingTemplate.convertAndSend(
                                "/topic/room-" + roomCode,
                                (Object) Map.of(
                                    "type", "GAME_STARTED",
                                    "selectedZones", finalSelection.selectedZones(),
                                    "turnOrder", latestRoom.getPlayerOrder(),
                                    "currentPlayerId", latestRoom.getCurrentPlayerId(),
                                    "listThemeZone", latestRoom.getAllThemeZone()
                                )
                            );
                            broadcastTurnChanged(roomCode, latestRoom);

                        } catch (Exception ignored) { }
                    }, 30, TimeUnit.SECONDS);

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

        messagingTemplate.convertAndSend(
            "/topic/room-" + roomCode,
            (Object) Map.of(
                "type", "GAME_START_CANCELLED",
                "reason", "The host cancelled the game launch"
            )
        );
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

            messagingTemplate.convertAndSend(
                "/topic/room-" + roomCode,
                (Object) Map.of(
                    "type", "ZONE_SELECTION_UPDATED",
                    "selectedZones", update.selectedZones()
                )
            );

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


        messagingTemplate.convertAndSend(
            "/topic/room-" + roomCode,
            (Object) Map.of(
                "type", type,
                "playerId", playerId,
                "data", data
            )
        );
    }


    @MessageMapping("/quit")
    public void quitRoom(Principal principal) {
        StompPrincipal sp = (StompPrincipal) principal;
        String playerId = sp.getName();
        String roomCode = sp.getRoomCode();

        Room room = roomService.getRoom(roomCode);
        boolean cancelFlow = isGameFlowLocked(room);

        roomService.removePlayer(roomCode, playerId);


        messagingTemplate.convertAndSend(
            "/topic/room-" + roomCode,
            (Object) Map.of(
                "type", "PLAYER_QUIT",
                "playerId", playerId
            )
        );

        if (cancelFlow && roomService.getRoom(roomCode) != null) {
            roomService.cancelGameStart(roomCode);
            messagingTemplate.convertAndSend(
                "/topic/room-" + roomCode,
                (Object) Map.of(
                    "type", "GAME_START_CANCELLED",
                    "reason", "A player left the room"
                )
            );
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
            roomService.removePlayer(roomCode, playerIdToKick);

            messagingTemplate.convertAndSend(
                "/topic/room-" + roomCode,
                (Object) Map.of(
                    "type", "PLAYER_KICK",
                    "playerId", playerIdToKick
                )
            );

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

            messagingTemplate.convertAndSend(
                "/topic/room-" + roomCode,
                (Object) Map.of(
                    "type", "ROOM_DELETED"
                )
            );

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

                if (isCorrect) {
                    Map<String, Object> moveMsg = new HashMap<>();
                    moveMsg.put("type", "GAME_ACTION");
                    moveMsg.put("action", room.getPendingAction());
                    moveMsg.put("zone", room.getPendingZone());
                    moveMsg.put("playerId", playerId);
                    moveMsg.put("difficulty", question.difficulty());

                    broadcastGameAction(roomCode, moveMsg);
                }

                broadcastGameAction(roomCode, Map.of(
                    "type", "ANSWER_RESULT",
                    "playerId", playerId,
                    "correct", isCorrect,
                    "difficulty", question.difficulty()
                ));

                room.advanceTurn();
                broadcastTurnChanged(roomCode, room);
            }
            case "SKIP" -> {
                room.advanceTurn();
                broadcastTurnChanged(roomCode, room);
            }
        }
    }
}