package com.historicconquest.historicconquest.network.model;

public record NetworkPlayer(
    String id,
    String pseudo,
    String color,
    String type,
    String status,
    int ping
) {
}

