package com.historicconquest.server.controller;

import com.historicconquest.server.model.Room;
import com.historicconquest.server.model.Player;
import com.historicconquest.server.service.RoomService;
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



    @GetMapping("/gameroom/check")
    public Map<String, Object> checkRoom(@RequestParam String roomCode) {
        boolean exists = roomService.getRoom(roomCode) != null;

        if (!exists || !roomService.getRoom(roomCode).isPossibleAddPlayer()) {
            return Map.of(
                "exists", false
            );
        }

        return Map.of(
            "exists", true
        );
    }


    @PostMapping("/gameroom/create")
    public Map<String, Object> createRoom(@RequestParam String playerName) {
        Room room = roomService.createRoom();
        Player player = new Player(playerName, "player", room.getCode());

        try {
            roomService.addHost(room.getCode(), player);

        } catch (Exception e) {
            return Map.of(
                "error", e
            );
        }

        return Map.of(
            "roomCode", room.getCode(),
            "token", player.getToken()
        );
    }

    @PostMapping("/gameroom/join")
    public Map<String, Object> joinRoom(@RequestParam String roomCode,
                                        @RequestParam String playerName) {
        Room room = roomService.getRoom(roomCode);
        if (room == null) throw new RuntimeException("Room not found");

        Player player = new Player(playerName, "player", room.getCode());

        try {
            roomService.addPlayer(roomCode,  player);

        } catch (Exception e) {
            return Map.of(
                "error", e
            );
        }


        messagingTemplate.convertAndSend(
            "/topic/room-" + roomCode,
            (Object) Map.of(
                "type", "PLAYER_JOIN",
                "player", player
            )
        );

        return Map.of(
            "token", player.getToken(),
            "players", room.getPlayers()
        );
    }
}