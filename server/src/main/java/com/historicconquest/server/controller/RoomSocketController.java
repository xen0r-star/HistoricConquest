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



    @MessageMapping("/addBot")
    public void addBot(Principal principal) {
        StompPrincipal sp = (StompPrincipal) principal;
        String playerId = sp.getName();
        String roomCode = sp.getRoomCode();

        Room room = roomService.getRoom(roomCode);
        Player player = room.getPlayerById(playerId);

        if (room.isGameStarting() || room.isGameStarted()) {
            messagingTemplate.convertAndSendToUser(
                playerId,
                "/queue/errors",
                Map.of(
                    "type", "ADD_BOT",
                    "title", "Failed to add bot",
                    "message", room.isGameStarting() ?
                            "A game countdown is already in progress" :
                            "A game is already started"
                )
            );

            return;
        }

        if (room.isHost(player.getId())) {
            Player newBot = new Player(NameGenerator.get(), "bot", room.getCode(), Player.Status.Ready);

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
            messagingTemplate.convertAndSendToUser(
                playerId,
                "/queue/errors",
                Map.of(
                    "type", "START_GAME",
                    "title", "Failed to start game",
                    "message", room.isGameStarting() ?
                            "A game countdown is already in progress" :
                            "A game is already started"
                )
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

                roomService.finishGameStart(roomCode);
                messagingTemplate.convertAndSend(
                    "/topic/room-" + roomCode,
                    (Object) Map.of("type", "GAME_STARTED")
                );
                roomService.deleteRoom(roomCode);
            }, GAME_COUNTDOWN_SECONDS, TimeUnit.SECONDS);

        } catch (Exception e) {
            messagingTemplate.convertAndSendToUser(
                playerId,
                "/queue/errors",
                Map.of(
                    "type", "START_GAME",
                    "title", "Failed to start game",
                    "message", "An error has occurred"
                )
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

        Room room = roomService.getRoom(roomCode);
        Player player = room.getPlayerById(playerId);

        if (room.isGameStarting() || room.isGameStarted()) {
            messagingTemplate.convertAndSendToUser(
                playerId,
                "/queue/errors",
                Map.of(
                    "type", type,
                    "title", "Action unavailable",
                    "message", room.isGameStarting() ?
                            "A game countdown is already in progress" :
                            "A game is already started"
                )
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

        roomService.removePlayer(roomCode, playerId);


        messagingTemplate.convertAndSend(
            "/topic/room-" + roomCode,
            (Object) Map.of(
                "type", "PLAYER_QUIT",
                "playerId", playerId
            )
        );
    }


    @MessageMapping("/kick")
    public void kickRoom(@Payload Map<String, String> payload, Principal principal) {
        StompPrincipal sp = (StompPrincipal) principal;
        String playerId = sp.getName();
        String roomCode = sp.getRoomCode();

        Room room = roomService.getRoom(roomCode);
        Player player = room.getPlayerById(playerId);

        if (room.isGameStarting() || room.isGameStarted()) {
            messagingTemplate.convertAndSendToUser(
                playerId,
                "/queue/errors",
                Map.of(
                    "type", "KICK_PLAYER",
                    "title", "Action unavailable",
                    "message", room.isGameStarting() ?
                            "A game countdown is already in progress" :
                            "A game is already started"
                )
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
        Player player = room.getPlayerById(playerId);

        if (room.isGameStarting() || room.isGameStarted()) {
            messagingTemplate.convertAndSendToUser(
                playerId,
                "/queue/errors",
                Map.of(
                    "type", "DELETE_ROOM",
                    "title", "Action unavailable",
                    "message", room.isGameStarting() ?
                            "A game countdown is already in progress" :
                            "A game is already started"
                )
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