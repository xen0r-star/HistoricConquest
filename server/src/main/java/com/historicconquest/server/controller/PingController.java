package com.historicconquest.server.controller;

import com.historicconquest.server.model.Room;
import com.historicconquest.server.security.StompPrincipal;
import com.historicconquest.server.service.RoomService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
public class PingController {

    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    public PingController(SimpMessagingTemplate messagingTemplate, RoomService roomService) {
        this.roomService = roomService;
        this.messagingTemplate = messagingTemplate;
    }



    @MessageMapping("/ping")
    public void handlePingRequest(@Payload Map<String, Object> message, Principal principal) {
        if (!(principal instanceof StompPrincipal sp)) return;

        messagingTemplate.convertAndSendToUser(
            sp.getName(),
            "/queue/ping",
            Map.of("timestamp", message.get("timestamp"))
        );
    }

    @MessageMapping("/updatePing")
    public void updatePing(@Payload Map<String, Object> message, Principal principal) {
        if (!(principal instanceof StompPrincipal sp)) return;

        String roomCode = sp.getRoomCode();
        String playerId = sp.getName();

        Object pingValue = message.get("ping");
        if (!(pingValue instanceof Number pingNumber)) return;
        int ping = pingNumber.intValue();

        Room room = roomService.getRoom(roomCode);
        if(room == null) return;

        room.getPlayerById(playerId).setPing(ping);
    }


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
