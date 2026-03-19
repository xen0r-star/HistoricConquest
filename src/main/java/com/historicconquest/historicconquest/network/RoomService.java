package com.historicconquest.historicconquest.network;

public class RoomService {
    private String playerId = null;
    private String token = null;
    private long pingTime = 0;

    private SocketClient socketClient;
    private SocketClient.StompListener pingListener;
    private static RoomService instance;

    private RoomService() { }

    private String getPingDestination() {
        return "/topic/ping-" + playerId;
    }

    private void subscribeToPing() {
        socketClient.subscribe("sub-ping", getPingDestination());
    }

    public String getToken() {
        return token;
    }

    public String getPlayerId() {
        return playerId;
    }


    public static RoomService getInstance() {
        if (instance == null) {
            instance = new RoomService();
        }
        return instance;
    }

    public void init(String playerId, String token) {
        this.playerId = playerId;
        this.token = token;

        SocketClient.setAuthorizationToken(token);
        this.socketClient = SocketClient.getInstance();

        this.pingListener = new SocketClient.StompListener() {
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

        this.socketClient.addListener(this.pingListener);
        subscribeToPing();
    }
}
