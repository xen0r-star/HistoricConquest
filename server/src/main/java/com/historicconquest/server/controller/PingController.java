package com.historicconquest.server.controller;

import com.historicconquest.server.model.Room;
import com.historicconquest.server.service.RoomService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class PingController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomService roomService;

    public PingController(SimpMessagingTemplate messagingTemplate, RoomService roomService) {
        this.messagingTemplate = messagingTemplate;
        this.roomService = roomService;
    }



    @MessageMapping("/pingRequest")
    @SendToUser("/queue/pong")
    public Map<String, Object> handlePingRequest(@Payload Map<String, Object> message) {
        return Map.of("timestamp", message.get("timestamp"));
    }

    @MessageMapping("/updatePing")
    public void updatePing(@Payload Map<String, Object> message) {
        String roomCode = (String) message.get("roomCode");
        String playerId = (String) message.get("playerId");
        int ping = (Integer) message.get("ping");

        Room room = roomService.getRoom(roomCode);
        if(room == null) return;

        room.getPlayerById(playerId).setPing(ping);
    }


    // Broadcast of pings from all players in each room
    @Scheduled(fixedRate = 2000)
    public void broadcastPings() {
        for(Room room : roomService.getAllRooms()) {
            Map<String, Integer> pings = room.getAllPings();

            if(!pings.isEmpty()) {
                messagingTemplate.convertAndSend(
                    "/topic/room-" + room.getCode(),
                    (Object) Map.of(
                        "type", "PLAYER_PINGS",
                        "pings", pings
                    )
                );
            }
        }
    }
}
