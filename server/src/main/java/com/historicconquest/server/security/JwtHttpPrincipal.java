package com.historicconquest.server.security;

import java.security.Principal;

public record JwtHttpPrincipal(
        String playerId,
        String roomCode
) implements Principal {
    @Override
    public String getName() {
        return playerId;
    }
}

