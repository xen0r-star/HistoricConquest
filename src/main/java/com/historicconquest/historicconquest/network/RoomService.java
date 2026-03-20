package com.historicconquest.historicconquest.network;

import com.fasterxml.jackson.databind.JsonNode;
import com.historicconquest.historicconquest.util.KeyLoaderUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.security.PublicKey;

public class RoomService {
    private final String playerId;
    private final String roomCode;
    private long pingTime = 0;

    private final SocketClient socketClient;
    private final StompListener pingListener;
    private final StompListener roomListener;

    private RoomEventListener listener;



    public RoomService(String token) {
        try {
            PublicKey publicKey = KeyLoaderUtils.loadPublicKey();
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            this.playerId = claims.getSubject();
            this.roomCode = claims.get("roomCode",  String.class);

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }



        this.socketClient = new SocketClient(token);

        this.pingListener = new StompListener() {
            @Override
            public void onHeartbeat() {
                pingTime = System.currentTimeMillis();
                socketClient.sendJson("/app/ping", "{\"timestamp\":\"%s\"}".formatted(pingTime));
            }

            @Override
            public void onMessage(String destination, String rawMessage) {
                if (!getPingDestination().equals(destination)) return;

                long measuredPing = System.currentTimeMillis() - pingTime;
                socketClient.sendJson("/app/updatePing", "{\"ping\":%d}".formatted(measuredPing));
            }
        };

        this.roomListener = new StompListener() {
            @Override
            public void onMessage(String destination, String rawMessage) {
                if (!getRoomDestination().equals(destination)) return;

                JsonNode node = socketClient.getJson(rawMessage);

                RoomInfo.from(node.get("type").asText())
                        .handle(node, listener);
            }
        };

        this.socketClient.addListener(this.pingListener);
        this.socketClient.addListener(this.roomListener);
        this.socketClient.connect();

        subscribeToPing();
        subscribeToRoom();
    }


    public void sendNoData(String destination) {
        socketClient.sendNoData(destination);
    }


    private String getPingDestination() {
        return "/topic/ping-" + playerId;
    }

    private String getRoomDestination() {
        return "/topic/room-" + roomCode;
    }

    private void subscribeToPing() {
        socketClient.subscribe("sub-ping", getPingDestination());
    }

    private void subscribeToRoom() {
        socketClient.subscribe("sub-room", getRoomDestination());
    }

    public void setListener(RoomEventListener listener) {
        this.listener = listener;
    }

    public void disconnect() {
        if (socketClient != null) {
            socketClient.removeListener(pingListener);
            socketClient.removeListener(roomListener);
            socketClient.close();
        }
    }
}
