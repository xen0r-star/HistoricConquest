package com.historicconquest.historicconquest.network.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.historicconquest.historicconquest.network.event.RoomEventListener;
import com.historicconquest.historicconquest.network.event.RoomInfo;
import com.historicconquest.historicconquest.network.stomp.SocketClient;
import com.historicconquest.historicconquest.network.stomp.StompListener;
import com.historicconquest.historicconquest.util.KeyLoaderUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.security.PublicKey;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RoomService {
    private static final long PING_INTERVAL_MS = 2000;
    private static final String STATUS_WAITING = "Waiting";
    private static final String STATUS_READY = "Ready";

    private final String playerId;
    private final String roomCode;
    private long pingTime;
    private String status = STATUS_WAITING;

    private final SocketClient socketClient;
    private final StompListener pingListener;
    private final StompListener roomListener;
    private final StompListener errorListener;

    private ScheduledExecutorService pingScheduler;
    private RoomEventListener listener;



    public RoomService(String token) {
        JwtRoomClaims claims = parseToken(token);
        this.playerId = claims.playerId();
        this.roomCode = claims.roomCode();
        this.socketClient = new SocketClient(token);
        this.pingListener = buildPingListener();
        this.roomListener = buildRoomListener();
        this.errorListener = buildErrorListener();

        subscribeAll();
        socketClient.connect();
    }

    private JwtRoomClaims parseToken(String token) {
        try {
            PublicKey publicKey = KeyLoaderUtils.loadPublicKey();
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

            return new JwtRoomClaims(claims.getSubject(), claims.get("roomCode", String.class));

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }



    private StompListener buildPingListener() {
        return new StompListener() {
            @Override
            public void onConnected() {
                startPingScheduler();
            }

            @Override
            public void onMessage(String destination, String rawMessage) {
                if (pingTime == 0) return;

                JsonNode payload = socketClient.getJson(rawMessage);
                JsonNode timestamp = payload.get("timestamp");
                if (timestamp == null || timestamp.isNull()) return;
                if (pingTime != timestamp.asLong()) return;

                try {
                    long measuredPing = System.currentTimeMillis() - pingTime;
                    socketClient.sendJson("/app/updatePing", Map.of("ping", measuredPing));

                } catch (Exception e) {
                    System.err.println("Failed to send ping update: " + e.getMessage());
                }
            }
        };
    }

    private StompListener buildRoomListener() {
        return new StompListener() {
            @Override
            public void onMessage(String destination, String rawMessage) {
                JsonNode payload = socketClient.getJson(rawMessage);
                JsonNode type = payload.get("type");
                if (type == null || type.isNull()) {
                    return;
                }

                RoomInfo.from(type.asText()).handle(payload, listener);
            }
        };
    }

    private StompListener buildErrorListener() {
        return new StompListener() {
            @Override
            public void onMessage(String destination, String rawMessage) {
//                System.out.println("Received message on destination: " + destination);
//                System.out.println("Error received: " + rawMessage);
            }
        };
    }



    private void subscribeAll() {
        socketClient.subscribe(
            "sub-ping", "/user/queue/ping",
            pingListener
        );

        socketClient.subscribe(
            "sub-room", "/topic/room-" + roomCode,
            roomListener
        );

        socketClient.subscribe(
            "sub-error", "/user/queue/errors",
            errorListener
        );
    }

    public void setListener(RoomEventListener listener) {
        this.listener = listener;
    }

    public void disconnect() {
        stopPingScheduler();
        socketClient.allUnsubscribe();
        socketClient.close();
    }




    private synchronized void startPingScheduler() {
        if (pingScheduler != null && !pingScheduler.isShutdown()) return;

        pingScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "room-ping-" + playerId);
            t.setDaemon(true);
            return t;
        });

        pingScheduler.scheduleAtFixedRate(this::sendPing, 0, PING_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private synchronized void stopPingScheduler() {
        if (pingScheduler == null) return;
        pingScheduler.shutdownNow();
        pingScheduler = null;
    }

    private void sendPing() {
        try {
            pingTime = System.currentTimeMillis();
            socketClient.sendJson(
                    "/app/ping",
                    Map.of("timestamp", String.valueOf(pingTime))
            );

        } catch (Exception e) {
            System.err.println("Failed to send ping: " + e.getMessage());
        }
    }



    public void addBot() {
        try {
            socketClient.sendNoData("/app/addBot");

        } catch (Exception e) {
            System.err.println("Failed to add bot: " + e.getMessage());
        }
    }

    public void switchStatus() {
        status = status.equals(STATUS_WAITING) ? STATUS_READY : STATUS_WAITING;

        try {
            socketClient.sendJson(
                "/app/update",
                Map.of(
                    "type", "PLAYER_STATUS_CHANGE",
                    "data", status
                )
            );

        } catch (Exception e) {
            System.err.println("Failed to update status: " + e.getMessage());
        }
    }

    public void quitRoom() {
        socketClient.sendNoData("/app/quit");
    }

    public void kickPlayer(String playerId) {
        try {
            socketClient.sendJson(
                "/app/kick",
                Map.of("playerId", playerId)
            );

        } catch (Exception e) {
            System.err.println("Failed to kick player: " + e.getMessage());
        }
    }

    public void deleteRoom() {
        socketClient.sendNoData("/app/delete");
    }


    public String getPlayerId() {
        return playerId;
    }

    private record JwtRoomClaims(String playerId, String roomCode) { }
}
