package com.historicconquest.historicconquest.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class SocketClient extends WebSocketClient {
    private static final String WEBSOCKET_LINK = "ws://localhost:8080/ws";
    private static final char STOMP_END = '\u0000';

    private static SocketClient instance;
    public static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Queue<String> messageQueue = new LinkedList<>();
    private static final List<StompListener> listeners = new ArrayList<>();
    private static boolean isConnected = false;
    private static String authorizationToken;

    public interface StompListener {
        default void onConnected() {}
        default void onHeartbeat() {}
        default void onMessage(String destination, String rawMessage) {}
    }

    private SocketClient() {
        super(createUri());
    }

    private static URI createUri() {
        try {
            return new URI(WEBSOCKET_LINK);

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URL WebSocket invalide : " + WEBSOCKET_LINK);
        }
    }



    public JsonNode getJson(String message) throws JsonProcessingException {
        int index = message.indexOf("{");

        if (index != -1) {
            String json = message.substring(index).replace("\u0000", "");
            return MAPPER.readTree(json);
        }

        return null;
    }

    public String getHeaderValue(String message, String headerName) {
        String prefix = headerName + ":";

        for (String line : message.split("\\n")) {
            if (line.startsWith(prefix)) {
                return line.substring(prefix.length()).trim();
            }
        }

        return null;
    }

    public static void setAuthorizationToken(String token) {
        authorizationToken = token;
    }

    public void addListener(StompListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(StompListener listener) {
        listeners.remove(listener);
    }




    @Override
    public void onOpen(ServerHandshake handshakeData) {
        System.out.println(buildConnectFrame());
        send(buildConnectFrame());
    }

    public void subscribe(String id, String destination) {
        sendSafe(buildFrame("SUBSCRIBE", List.of(
            "id:" + id,
            "destination:" + destination
        ), null));
    }

    public void sendJson(String destination, String jsonBody) {
        sendSafe(buildFrame("SEND", List.of(
            "destination:" + destination,
            "content-type:application/json"
        ), jsonBody));
    }

    public void sendSafe(String message) {
        if (isConnected) send(message);
        else messageQueue.add(message);
    }

    @Override
    public void onMessage(String message) {
        System.out.println("RAW: " + message);

        if (message.startsWith("CONNECTED")) {
            isConnected = true;

            while (!messageQueue.isEmpty()) {
                send(messageQueue.poll());
            }

            for (StompListener listener : List.copyOf(listeners)) {
                listener.onConnected();
            }
            return;
        }

        if (message.trim().isEmpty()) {
            for (StompListener listener : List.copyOf(listeners)) {
                listener.onHeartbeat();
            }
            return;
        }

        if (message.startsWith("MESSAGE")) {
            String destination = getHeaderValue(message, "destination");

            for (StompListener listener : List.copyOf(listeners)) {
                listener.onMessage(destination, message);
            }
            return;

        }

        System.out.println("Request inconnue: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        isConnected = false;
        System.out.println("Déconnecté");
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("Erreur : " + ex.getMessage());
    }


    public static SocketClient getInstance() {
        if  (instance == null) {
            instance = new SocketClient();
            instance.connect();
        }

        return instance;
    }

    private String buildConnectFrame() {
        List<String> headers = new ArrayList<>();
        headers.add("accept-version:1.2");
        headers.add("host:localhost");
        headers.add("heart-beat:10000,20000");

        if (authorizationToken != null && !authorizationToken.isBlank()) {
            headers.add("Authorization:Bearer " + authorizationToken);
        }

        return buildFrame("CONNECT", headers, null);
    }

    private String buildFrame(String command, List<String> headers, String body) {
        StringBuilder frame = new StringBuilder();
        frame.append(command).append('\n');

        for (String header : headers) {
            frame.append(header).append('\n');
        }

        frame.append('\n');

        if (body != null && !body.isEmpty()) {
            frame.append(body);
        }

        frame.append(STOMP_END);
        return frame.toString();
    }
}