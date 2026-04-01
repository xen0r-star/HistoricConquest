package com.historicconquest.historicconquest.model.network.model;

public record NetworkPlayer(
    String id,
    String pseudo,
    String color,
    String type,
    String status,
    int ping
) {
}

