package com.historicconquest.historicconquest.service.network;

import com.historicconquest.historicconquest.controller.game.GameController;
import com.historicconquest.historicconquest.controller.page.QuestionController;
import javafx.application.Platform;
import tools.jackson.databind.JsonNode;
import com.historicconquest.historicconquest.model.network.event.RoomEventListener;
import com.historicconquest.historicconquest.model.network.event.RoomInfo;
import com.historicconquest.historicconquest.util.KeyLoader;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private final String token;
    private long pingTime;
    private String status = STATUS_WAITING;
    private String pseudo;
    private String color;

    private final SocketClient socketClient;
    private final StompListener pingListener, replyListener, roomListener, errorListener;
    private final ErrorNotifier errorNotifier;

    private ScheduledExecutorService pingScheduler;
    private RoomEventListener listener;
    private static RoomService instance;


    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);

    @FunctionalInterface
    public interface ErrorNotifier {
        void notify(String title, String message);
    }


    private RoomService(String token, ErrorNotifier errorNotifier) {
        JwtRoomClaims claims = parseToken(token);
        this.playerId = claims.playerId();
        this.roomCode = claims.roomCode();
        this.token = token;
        this.errorNotifier = errorNotifier;
        this.socketClient = new SocketClient(token);
        this.pingListener = buildPingListener();
        this.replyListener = buildReplyListener();
        this.roomListener = buildRoomListener();
        this.errorListener = buildErrorListener();

        subscribeAll();
        socketClient.connect();
    }

    public static synchronized void create(String token, ErrorNotifier errorNotifier) {
        if (instance != null) {
            instance.disconnect();
        }

        instance = new RoomService(token, errorNotifier);
    }

    public static RoomService getInstance() {
        if (instance == null) {
            throw new IllegalStateException("RoomService is not initialized");
        }

        return instance;
    }

    public static synchronized boolean isInitialized() {
        return instance != null;
    }

    public static synchronized void reset() {
        if (instance == null) return;

        instance.disconnect();
        instance = null;
    }

    private JwtRoomClaims parseToken(String token) {
        try {
            PublicKey publicKey = KeyLoader.loadPublicKey();
            Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            return new JwtRoomClaims(claims.getSubject(), claims.get("roomCode", String.class));

        } catch (Exception e) {
            logger.error("Failed to parse JWT token: {}", e.getMessage());
            System.exit(1);
            return null;
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

                RoomInfo.from(type.asString()).handle(payload, listener);
            }
        };
    }

    private StompListener buildErrorListener() {
        return new StompListener() {
            @Override
            public void onMessage(String destination, String rawMessage) {
                JsonNode payload = socketClient.getJson(rawMessage);
                String title = payload.get("title").asString();
                String message = payload.get("message").asString();

                notifyError(title, message);
            }
        };
    }

    private StompListener buildReplyListener() {
        return new StompListener() {
            @Override
            public void onMessage(String destination, String rawMessage) {
                try {
                    JsonNode payload = socketClient.getJson(rawMessage);
                    String type = payload.has("type") ? payload.get("type").asString() : "";

                    if ("QUESTION_CHALLENGE".equals(type)) {
                        String questionId = payload.get("questionId").asString();
                        String questionText = payload.get("question").asString();

                        List<String> choices = new ArrayList<>();
                        payload.get("choices").forEach(choice -> choices.add(choice.asString()));

                        Platform.runLater(() ->
                            QuestionController.showQuestionPage(questionId, questionText, choices)
                        );
                    }

                } catch (Exception e) {
                    System.err.println("Error reading private message (Reply) " + e);
                }
            }
        };
    }



    private void subscribeAll() {
        socketClient.subscribe(
            "sub-ping", "/user/queue/ping",
            pingListener
        );

        socketClient.subscribe(
            "sub-reply", "/user/queue/reply",
            replyListener
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

    public static void setListener(RoomEventListener listener) {
        getInstance().listener = listener;
    }

    private void disconnect() {
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



    public static void addBot() {
        try {
            getInstance().socketClient.sendNoData("/app/addBot");

        } catch (Exception e) {
            getInstance().notifyError(
                "Failed to add bot",
                "Impossible to add bot to room, Please try again."
            );
        }
    }

    public static void startGame() {
        try {
            getInstance().socketClient.sendNoData("/app/start");

        } catch (Exception e) {
            getInstance().notifyError(
                "Failed to start game",
                "Impossible to start game, Please try again."
            );
        }
    }

    public static void cancelGameStart() {
        try {
            getInstance().socketClient.sendNoData("/app/start/cancel");

        } catch (Exception e) {
            getInstance().notifyError(
                "Failed to cancel game start",
                "Impossible to cancel game start, Please try again."
            );
        }
    }

    public static void selectZone(String zoneName) {
        if (zoneName == null || zoneName.isBlank()) {
            getInstance().notifyError(
                "Failed to select zone",
                "Impossible to select the chosen zone, Please try again."
            );
            return;
        }

        try {
            getInstance().socketClient.sendJson(
                "/app/zone/select",
                Map.of("zone", zoneName)
            );

        } catch (Exception e) {
            getInstance().notifyError(
                "Failed to select zone",
                "Impossible to select the chosen zone, Please try again."
            );
        }
    }

    public static String switchStatus() {
        String nextStatus = getInstance().status.equals(STATUS_WAITING) ? STATUS_READY : STATUS_WAITING;
        return setStatus(nextStatus);
    }

    public static String setStatus(String newStatus) {
        String normalizedStatus = normalizeStatus(newStatus);
        if (normalizedStatus == null) {
            return getInstance().status;
        }

        getInstance().status = normalizedStatus;

        try {
            getInstance().socketClient.sendJson(
                "/app/update",
                Map.of(
                    "type", "PLAYER_STATUS_CHANGE",
                    "data", getInstance().status
                )
            );

        } catch (Exception e) {
            getInstance().notifyError(
                "Failed to switch status",
                "Impossible to switch status, Please try again."
            );
        }

        return getInstance().status;
    }

    public static void updatePseudo(String newPseudo) {
        try {
            getInstance().socketClient.sendJson(
                "/app/update",
                Map.of(
                    "type", "PLAYER_PSEUDO_CHANGE",
                    "data", newPseudo
                )
            );

        } catch (Exception e) {
            getInstance().notifyError(
                "Failed to change pseudo",
                "Impossible to change pseudo, Please try again."
            );
        }
    }

    public static void updateColor(String newColor) {
        try {
            getInstance().socketClient.sendJson(
                "/app/update",
                Map.of(
                    "type", "PLAYER_COLOR_CHANGE",
                    "data", newColor
                )
            );

        } catch (Exception e) {
            getInstance().notifyError(
                "Failed to change color",
                "Impossible to change color, Please try again."
            );
        }
    }

    public static void quitRoom() {
        getInstance().socketClient.sendNoData("/app/quit");
    }

    public static void kickPlayer(String playerId) {
        try {
            getInstance().socketClient.sendJson(
                "/app/kick",
                Map.of("playerId", playerId)
            );

        } catch (Exception e) {
            getInstance().notifyError(
                "Failed to kick player",
                "Impossible to kick player, Please try again."
            );
        }
    }

    public static void deleteRoom() {
        getInstance().socketClient.sendNoData("/app/delete");
    }

    public static String getPlayerId() {
        return getInstance().playerId;
    }

    public static String getToken() {
        return getInstance().token;
    }

    public static void setCurrentPseudo(String pseudo) {
        getInstance().pseudo = pseudo;
    }

    public static String getCurrentPseudo() {
        return getInstance().pseudo;
    }

    public static void setCurrentColor(String color) {
        getInstance().color = color;
    }

    public static String getCurrentColor() {
        return getInstance().color;
    }

    public static void sendGameAction(String action, Map<String, Object> data) {
        if (action == null || action.isBlank()) {
            return;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("action", action);
            if (data != null && !data.isEmpty()) {
                payload.putAll(data);
            }

            getInstance().socketClient.sendJson(
                "/app/game/action",
                payload
            );

        } catch (Exception e) {
            getInstance().notifyError(
                "Failed to send game action",
                "Impossible to send game action, Please try again."
            );
        }
    }

    private static String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        String normalized = status.trim();
        if (STATUS_WAITING.equalsIgnoreCase(normalized)) {
            return STATUS_WAITING;
        }

        if (STATUS_READY.equalsIgnoreCase(normalized)) {
            return STATUS_READY;
        }

        return null;
    }


    private void notifyError(String title, String message) {
        if (errorNotifier != null) {
            errorNotifier.notify(title, message);
            return;
        }

        logger.warn("{} - {}", title, message);
    }

    private record JwtRoomClaims(String playerId, String roomCode) { }
}
