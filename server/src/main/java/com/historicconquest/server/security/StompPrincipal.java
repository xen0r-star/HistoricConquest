package com.historicconquest.server.security;

import java.security.Principal;

public class StompPrincipal implements Principal {
    private final String playerId;
    private final String roomCode;

    public StompPrincipal(String playerId, String roomCode) {
        this.playerId = playerId;
        this.roomCode = roomCode;
    }

    @Override
    public String getName() {
        return playerId;
    }

    public String getRoomCode() {
        return roomCode;
    }
}