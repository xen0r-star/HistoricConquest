package com.historicconquest.historicconquest.network;

public interface StompListener {
    default void onConnected() {}
    default void onHeartbeat() {}
    default void onMessage(String destination, String rawMessage) {}
}
