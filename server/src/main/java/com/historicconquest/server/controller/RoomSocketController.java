package com.historicconquest.server.controller;

import com.historicconquest.server.security.StompPrincipal;
import com.historicconquest.server.model.Room;
import com.historicconquest.server.model.Player;
import com.historicconquest.server.service.RoomService;
import com.historicconquest.server.util.NameGenerator;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Map;


@Controller
public class RoomSocketController {
    private static final int GAME_COUNTDOWN_SECONDS = 5;

    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "room-start-scheduler");
        thread.setDaemon(true);
        return thread;
    });

    public RoomSocketController(RoomService roomService,
                                SimpMessagingTemplate messagingTemplate) {
        this.roomService = roomService;
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
                if (currentRoom == null) {
                    return;
                }

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

                    scheduler.schedule(() -> {
                        Room latestRoom = roomService.getRoom(roomCode);
                        if (latestRoom == null || latestRoom.isGameStarted()) {
                            return;
                        }

                        if (!latestRoom.isZoneSelectionStarted()) {
                            return;
                        }

                        try {
                            RoomService.ZoneSelectionUpdate finalSelection = roomService.completeZoneSelection(roomCode);
                            messagingTemplate.convertAndSend(
                                "/topic/room-" + roomCode,
                                (Object) Map.of(
                                    "type", "GAME_STARTED",
                                    "selectedZones", finalSelection.selectedZones()
                                )
                            );
                            roomService.deleteRoom(roomCode);

                        } catch (Exception ignored) {
                        }
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
}