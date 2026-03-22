package com.historicconquest.historicconquest.network.stomp;

public interface StompListener {
    default void onConnected() {}
    default void onHeartbeat() {}
    default void onMessage(String destination, String rawMessage) {}
}
