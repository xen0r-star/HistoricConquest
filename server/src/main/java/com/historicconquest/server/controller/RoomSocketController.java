package com.historicconquest.server.controller;

import com.historicconquest.server.model.Room;
import com.historicconquest.server.model.Player;
import com.historicconquest.server.service.RoomService;
import com.historicconquest.server.service.JwtService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;


@Controller
public class RoomSocketController {
    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    public RoomSocketController(RoomService roomService,
                                SimpMessagingTemplate messagingTemplate) {
        this.roomService = roomService;
        this.messagingTemplate = messagingTemplate;
    }


    @MessageMapping("/update")
    public void updateData(@Payload Map<String, String> message) {
        String type = message.get("type");
        String token = message.get("token");
        String data = message.get("data");

        Map<String,String> info = JwtService.verifyToken(token);
        if (info == null) return;

        Room room = roomService.getRoom(info.get("roomCode"));
        Player player = room.getPlayerById(info.get("playerId"));

        switch (type) {
            case "PLAYER_COLOR_CHANGE":
                player.setColor(info.get("color"));
                break;

            case "PLAYER_PSEUDO_CHANGE":
                player.setPseudo(info.get("pseudo"));
                break;
        }


        messagingTemplate.convertAndSend(
            "/topic/room-" + info.get("roomCode"),
            (Object) Map.of(
                "type", type,
                "playerId", info.get("playerId"),
                "data", data
            )
        );
    }


    @MessageMapping("/quit")
    public void quitRoom(@Payload Map<String, String> message) {
        String token = message.get("token");

        Map<String,String> info = JwtService.verifyToken(token);
        if (info == null) return;

        roomService.removePlayer(info.get("roomCode"), info.get("playerId"));


        messagingTemplate.convertAndSend(
            "/topic/room-" + info.get("roomCode"),
            (Object) Map.of(
                "type", "PLAYER_QUIT",
                "playerId", info.get("playerId")
            )
        );
    }


    @MessageMapping("/delete")
    public void deleteRoom(@Payload Map<String, String> message) {
        String token = message.get("token");

        Map<String,String> info = JwtService.verifyToken(token);
        if (info == null) return;

        Room room = roomService.getRoom(info.get("roomCode"));
        Player player = room.getPlayerById(info.get("playerId"));

        if (!room.isHost(player.getId())) return;

        roomService.deleteRoom(info.get("roomCode"));

        messagingTemplate.convertAndSend(
            "/topic/room-" + info.get("roomCode"),
            (Object) Map.of(
                "type", "ROOM_DELETED"
            )
        );
    }
}