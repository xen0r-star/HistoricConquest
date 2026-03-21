package com.historicconquest.server.controller;

import com.historicconquest.server.security.StompPrincipal;
import com.historicconquest.server.model.Room;
import com.historicconquest.server.model.Player;
import com.historicconquest.server.service.RoomService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
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
    public void updateData(@Payload Map<String, String> payload, Principal principal) {
        StompPrincipal sp = (StompPrincipal) principal;
        String playerId = sp.getName();
        String roomCode = sp.getRoomCode();

        String type = payload.get("type");
        String data = payload.get("data");

        Room room = roomService.getRoom(roomCode);
        Player player = room.getPlayerById(playerId);

        switch (type) {
            case "PLAYER_COLOR_CHANGE":
                player.setColor(data);
                break;

            case "PLAYER_PSEUDO_CHANGE":
                player.setPseudo(data);
                break;

            case "PLAYER_STATUS_CHANGE":
                player.setStatus(data);
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

        if (!room.isHost(player.getId())) return;


        String playerIdToKick = payload.get("playerId");
        roomService.removePlayer(roomCode, playerIdToKick);

        messagingTemplate.convertAndSend(
            "/topic/room-" + roomCode,
            (Object) Map.of(
                "type", "PLAYER_KICK",
                "playerId", playerIdToKick
            )
        );
    }


    @MessageMapping("/delete")
    public void deleteRoom(Principal principal) {
        StompPrincipal sp = (StompPrincipal) principal;
        String playerId = sp.getName();
        String roomCode = sp.getRoomCode();

        Room room = roomService.getRoom(roomCode);
        Player player = room.getPlayerById(playerId);

        if (!room.isHost(player.getId())) return;

        roomService.deleteRoom(roomCode);

        messagingTemplate.convertAndSend(
            "/topic/room-" + roomCode,
            (Object) Map.of(
                "type", "ROOM_DELETED"
            )
        );
    }
}