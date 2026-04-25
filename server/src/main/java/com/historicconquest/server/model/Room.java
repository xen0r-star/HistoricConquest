package com.historicconquest.server.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Room {
    private final static int MAX_PLAYER = 4;

    private final String code;
    private String hostId;
    private boolean gameStarting;
    private boolean zoneSelectionStarted;
    private boolean gameStarted;
    private long gameStartAt;

    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private final Map<String, String> selectedZones = new ConcurrentHashMap<>();

    public Room(String code) {
        this.code = code;
    }


    public boolean isHost(String playerId) {
        return hostId != null && hostId.equals(playerId);
    }

    public synchronized boolean canStartGame() {
        return !gameStarting && !zoneSelectionStarted && hasLaunchConditions();
    }

    public synchronized boolean hasLaunchConditions() {
        if (gameStarted || zoneSelectionStarted) return false;
        if (players.size() != MAX_PLAYER) return false;
        if (hostId == null || !players.containsKey(hostId)) return false;

        for (Player player : players.values()) {
            if (isHost(player.getId())) continue;
            if (!isPlayerReady(player)) return false;
        }

        return true;
    }

    public synchronized boolean isGameStarting() {
        return gameStarting;
    }

    public synchronized boolean isGameStarted() {
        return gameStarted;
    }

    public synchronized long getGameStartAt() {
        return gameStartAt;
    }

    public synchronized void markGameStarting(long gameStartAt) {
        this.gameStarting = true;
        this.gameStarted = false;
        this.zoneSelectionStarted = false;
        this.gameStartAt = gameStartAt;
        this.selectedZones.clear();
    }

    public synchronized void markZoneSelectionStarted(long gameStartAt) {
        this.gameStarting = false;
        this.gameStarted = false;
        this.zoneSelectionStarted = true;
        this.gameStartAt = gameStartAt;
        this.selectedZones.clear();
    }

    public synchronized void cancelGameStarting() {
        this.gameStarting = false;
        this.zoneSelectionStarted = false;
        this.gameStartAt = 0L;
        this.selectedZones.clear();
    }

    public synchronized void markGameStarted() {
        this.gameStarting = false;
        this.zoneSelectionStarted = false;
        this.gameStarted = true;
        this.gameStartAt = 0L;
    }

    public synchronized boolean isZoneSelectionStarted() {
        return zoneSelectionStarted;
    }

    public synchronized boolean selectZone(String playerId, String zoneName) {
        if (!zoneSelectionStarted || gameStarted) return false;
        if (playerId == null || zoneName == null || zoneName.isBlank()) return false;
        if (!players.containsKey(playerId)) return false;

        String normalizedZone = zoneName.trim();
        for (Map.Entry<String, String> entry : selectedZones.entrySet()) {
            if (playerId.equals(entry.getKey())) continue;
            if (normalizedZone.equalsIgnoreCase(entry.getValue())) return false;
        }

        selectedZones.put(playerId, normalizedZone);
        return true;
    }

    public synchronized Map<String, String> getSelectedZones() {
        return new HashMap<>(selectedZones);
    }

    public synchronized boolean areAllPlayersAssigned() {
        return players.size() == MAX_PLAYER && selectedZones.size() == players.size();
    }

    public synchronized void removeSelectedZone(String playerId) {
        selectedZones.remove(playerId);
    }

    public void addPlayer(Player player) throws Exception {
        if (players.containsKey(player.getId())) throw new Exception("Player already in the room");
        if (players.size() >= MAX_PLAYER) throw new Exception("Room is full");

        players.put(player.getId(), player);
    }

    public void removePlayer(String playerId) {
        players.remove(playerId);
        selectedZones.remove(playerId);
    }




    public void setHostId(String hostId) throws Exception {
        if (!players.containsKey(hostId)) {
            throw new Exception("Host must be a player in the room");
        }

        this.hostId = hostId;
    }


    public String getCode() {
        return code;
    }

    public Collection<Player> getPlayers() {
        return players.values();
    }

    private boolean isPlayerReady(Player player) {
        if (player == null) return false;
        if (Player.Type.Bot == player.getType()) return true;
        return (Player.Status.Ready == player.getStatus());
    }

    public Player getPlayerById(String playerId) {
        return players.get(playerId);
    }

    public Map<String, Integer> getAllPings() {
        Map<String, Integer> pings = new HashMap<>();

        for (Player player : players.values()) {
            pings.put(player.getId(), player.getPing());
        }

        return pings;
    }
}
