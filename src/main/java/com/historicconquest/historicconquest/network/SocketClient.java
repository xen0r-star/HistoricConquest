package com.historicconquest.historicconquest.network;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class SocketClient extends WebSocketClient {
    private static final String WEBSOCKET_LINK = "ws://localhost:8080/ws";

    public SocketClient() {
        super(createUri());
    }

    private static URI createUri() {
        try {
            return new URI(WEBSOCKET_LINK);

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URL WebSocket invalide : " + WEBSOCKET_LINK);
        }
    }



    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("Connected");

        send("""
        CONNECT
        accept-version:1.2
        host:localhost
        
        \u0000
        """);
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Closed");
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
}