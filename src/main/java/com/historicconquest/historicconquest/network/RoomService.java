package com.historicconquest.historicconquest.network;

import com.fasterxml.jackson.databind.JsonNode;
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

    private final String playerId;
    private final String roomCode;
    private long pingTime = 0;
    private String status = "Waiting";

    private final SocketClient socketClient;
    private final StompListener pingListener;
    private final StompListener roomListener;
    private final StompListener errorListener;

    private ScheduledExecutorService pingScheduler;
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
            public void onConnected() {
                startPingScheduler();
            }

            @Override
            public void onMessage(String destination, String rawMessage) {
                if (pingTime == 0) return;
                if (pingTime != socketClient.getJson(rawMessage).get("timestamp").asLong()) return;

                try {
                    long measuredPing = System.currentTimeMillis() - pingTime;
                    socketClient.sendJson(
                        "/app/updatePing",
                        Map.of("ping", measuredPing)
                    );

                } catch (Exception e) {
                    System.out.println("Error sending ping");
                }
            }
        };

        this.roomListener = new StompListener() {
            @Override
            public void onMessage(String destination, String rawMessage) {
                JsonNode node = socketClient.getJson(rawMessage);

                RoomInfo.from(node.get("type").asText())
                        .handle(node, listener);
            }
        };

        this.errorListener = new StompListener() {
            @Override
            public void onMessage(String destination, String rawMessage) {
                System.out.println("Received message on destination: " + destination);
                System.out.println("Error received: " + rawMessage);
            }
        };

        subscribeAll();
        this.socketClient.connect();
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
        if (socketClient != null) {
            stopPingScheduler();
            socketClient.removeListener(pingListener);
            socketClient.removeListener(roomListener);
            socketClient.removeListener(errorListener);
            socketClient.close();
        }
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
        if (status.equals("Waiting")) status = "Ready";
        else status = "Waiting";

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
}
