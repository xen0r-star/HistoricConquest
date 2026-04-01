package com.historicconquest.historicconquest.service.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;


public class SocketClient extends WebSocketClient {
    private static final String DEFAULT_WEBSOCKET_URL = "ws://localhost:8080/ws";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final Queue<String> messageQueue = new LinkedList<>();
    private final List<StompListener> listeners = new ArrayList<>();
    private final Map<String, StompListener> subscriptionListeners = new HashMap<>();
    private final Map<String, StompListener> destinationListeners = new HashMap<>();
    private final String authorizationToken;
    private boolean isConnected = false;



    public SocketClient(String authorizationToken) {
        this(DEFAULT_WEBSOCKET_URL, authorizationToken);
    }

    public SocketClient(String websocketUrl, String authorizationToken) {
        super(createUri(websocketUrl));

        this.authorizationToken = authorizationToken;
    }

    private static URI createUri(String websocketUrl) {
        try {
            return new URI(websocketUrl);

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URL WebSocket invalide : " + websocketUrl);
        }
    }

    public static boolean serverIsUp() {
        try {
            SocketClient tempClient = new SocketClient("ws://localhost:8080/ws", null);
            tempClient.connectBlocking();
            tempClient.close();
            return true;

        } catch (Exception e) {
            return false;
        }
    }



    // LISTENER ----------------------------------------------------------
    private void addListener(StompListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    private void removeListener(StompListener listener) {
        listeners.remove(listener);
    }


    // SUBSCRIBE AND SEND ------------------------------------------------
    public void subscribe(String id, String destination) {
        sendSafe(buildFrame(
            "SUBSCRIBE",
            List.of(
                "id:" + id,
                "destination:" + destination
            ),
            null
        ));
    }

    public void subscribe(String id, String destination, StompListener listener) {
        if (listener != null) {
            subscriptionListeners.put(id, listener);
            destinationListeners.put(destination, listener);
            addListener(listener);
        }

        subscribe(id, destination);
    }

    public void unsubscribe(String id) {
        sendSafe(buildFrame(
            "UNSUBSCRIBE",
            List.of("id:" + id),
            null
        ));

        StompListener listener = subscriptionListeners.remove(id);
        if (listener != null) {
            destinationListeners.values().remove(listener);
            removeListener(listener);
        }
    }

    public void allUnsubscribe() {
        List<String> subscriptionIds = new ArrayList<>(subscriptionListeners.keySet());
        for (String id : subscriptionIds) {
            unsubscribe(id);
        }

        subscriptionListeners.clear();
        destinationListeners.clear();
        listeners.clear();
    }



    public void sendJson(String destination, Map<String, Object> jsonBody) throws JsonProcessingException {
        sendSafe(buildFrame(
            "SEND",
            List.of(
                "destination:" + destination,
                "content-type:application/json"
            ),
            MAPPER.writeValueAsString(jsonBody)
        ));
    }

    public void sendNoData(String destination) {
        sendSafe(buildFrame(
            "SEND",
            List.of(
                "destination:" + destination
            ),
            null
        ));
    }

    public void sendSafe(String message) {
        if (isConnected) send(message);
        else messageQueue.add(message);
    }



    // OVERRIDE ----------------------------------------------------------
    @Override
    public void onMessage(String message) {
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
            String subscriptionId = getHeaderValue(message, "subscription");

            StompListener listener = null;
            if (subscriptionId != null) {
                listener = subscriptionListeners.get(subscriptionId);
            }
            if (listener == null && destination != null) {
                listener = destinationListeners.get(destination);
            }

            if (listener != null) {
                listener.onMessage(destination, message);
            }
            return;
        }

        if (message.startsWith("ERROR")) {
            System.err.println("Error received: " + getBody(message));
            return;
        }

        System.out.println("Request inconnue: " + message);
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        List<String> headers = new ArrayList<>();
        headers.add("accept-version:1.2");
        headers.add("host:localhost");
        headers.add("heart-beat:10000,20000");

        if (authorizationToken != null && !authorizationToken.isBlank()) {
            headers.add("Authorization:Bearer " + authorizationToken);
        }

        send(buildFrame("CONNECT", headers, null));
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        isConnected = false;
        System.out.println("Déconnecté");
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("Error : " + ex.getMessage());
    }



    // BUILD AND GET -----------------------------------------------------
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

        frame.append('\u0000');
        return frame.toString();
    }


    public JsonNode getJson(String message) {
        int index = message.indexOf("{");

        if (index != -1) {
            String json = message.substring(index).replace("\u0000", "");

            try {
                return MAPPER.readTree(json);

            } catch (Exception e) {
                System.err.println("Invalid JSON payload: " + e.getMessage());
                return NullNode.getInstance();
            }
        }

        return NullNode.getInstance();
    }

    public String getBody(String message) {
        String[] parts = message.split("\n\n", 2);

        if (parts.length > 1) {
            return parts[1].replace("\u0000", "").trim();
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
}