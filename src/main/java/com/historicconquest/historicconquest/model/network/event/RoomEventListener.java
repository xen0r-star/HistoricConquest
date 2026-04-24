package com.historicconquest.historicconquest.model.network.event;

import com.historicconquest.historicconquest.model.network.model.NetworkPlayer;

import java.util.Map;

public interface RoomEventListener {
    void onPlayerJoin(NetworkPlayer newPlayer);
    void onPlayerQuit(String playerId);
    void onPlayerKick(String playerId);
    void onPlayerColorChange(String playerId, String newColor);
    void onPlayerPseudoChange(String playerId, String newPseudo);
    void onPlayerStatusChange(String playerId, String newStatus);
    void onPlayerPings(Map<String, Integer> pings);
    void onGameCountdownStarted(int seconds, long startAt);
    void onGameStartCancelled(String reason);
    void onGameStarted();

    void onRoomDeleted();
}
