package com.historicconquest.historicconquest.model.network.event;

import com.historicconquest.historicconquest.model.network.model.NetworkPlayer;

import java.util.List;
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
    void onZoneSelectionStarted(int seconds, long startAt, Map<String, String> selectedZones);
    void onZoneSelectionUpdated(Map<String, String> selectedZones);
    void onGameStartCancelled(String reason);
    void onGameStarted(Map<String, String> selectedZones, List<String> turnOrder, String currentPlayerId, Map<String, String> listThemeZone);

    void onGameAction(String action, String playerId, String zoneName, Integer difficulty, Boolean correct);
    void onTurnChanged(String currentPlayerId, Integer currentPlayerIndex);
    void onAnswerResult(String currentPlayerId, Boolean currentCorrect, Integer currentDifficulty);

    void onRoomDeleted();
}
