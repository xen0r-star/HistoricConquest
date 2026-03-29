package com.historicconquest.server.controller;

import com.historicconquest.server.model.Room;
import com.historicconquest.server.model.Player;
import com.historicconquest.server.service.RoomService;
import com.historicconquest.server.util.NameGenerator;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HttpController {
    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    public HttpController(RoomService roomService, SimpMessagingTemplate messagingTemplate) {
        this.roomService = roomService;
        this.messagingTemplate = messagingTemplate;
    }



    @GetMapping("/health")
    public Map<String, Object> healthServer() {
        return Map.of(
            "status", "ok",
            "timestamp", System.currentTimeMillis()
        );
    }


    @PostMapping("/gameroom/create")
    public Map<String, Object> createRoom(@RequestParam String playerName) {
        try {
            Room room = roomService.createRoom();
            Player player = new Player(playerName, "player", room.getCode());

            roomService.addHost(room.getCode(), player);

            return Map.of(
                "roomCode", room.getCode(),
                "token", player.getToken()
            );

        } catch (Exception e) {
            return Map.of(
                "error", Map.of(
                    "title", "Room Creation Failed",
                    "message", "An error occurred while creating the room. Please try again."
                )
            );
        }
    }

    @PostMapping("/gameroom/join")
    public Map<String, Object> joinRoom(@RequestParam String roomCode) {
        try {
            Room room = roomService.getRoom(roomCode);
            if (room == null) throw new RuntimeException("Room not found");

            Player player = new Player(NameGenerator.get(), "player", room.getCode());


            roomService.addPlayer(roomCode,  player);

            messagingTemplate.convertAndSend(
                "/topic/room-" + roomCode,
                (Object) Map.of(
                    "type", "PLAYER_JOIN",
                    "player", player
                )
            );

            return Map.of(
                "token", player.getToken(),
                "pseudo", player.getPseudo(),
                "players", room.getPlayers()
            );

        } catch (Exception e) {
            return Map.of(
                "error", Map.of(
                    "title", "Impossible to join",
                    "message", "You cannot join this room as it is not available or is full."
                )
            );
        }
    }
}