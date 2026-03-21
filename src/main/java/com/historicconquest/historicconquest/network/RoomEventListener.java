package com.historicconquest.historicconquest.network;

import java.util.Map;

public interface RoomEventListener {
    void onPlayerJoin(ApiService.Player newPlayer);
    void onPlayerQuit(String playerId);
    void onPlayerKick(String playerId);
    void onPlayerColorChange(String playerId, String newColor);
    void onPlayerPseudoChange(String playerId, String newPseudo);
    void onPlayerStatusChange(String playerId, String newStatus);
    void onPlayerPings(Map<String, Integer> pings);

    void onRoomDeleted();
}
