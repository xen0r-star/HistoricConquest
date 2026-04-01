package com.historicconquest.historicconquest.service.network;

public interface StompListener {
    default void onConnected() {}
    default void onHeartbeat() {}
    default void onMessage(String destination, String rawMessage) {}
}
