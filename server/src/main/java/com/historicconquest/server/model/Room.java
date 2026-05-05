package com.historicconquest.server.model;

import com.historicconquest.server.model.map.WorldMap;
import com.historicconquest.server.model.map.Zone;
import com.historicconquest.server.model.player.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Room {
    private final static int MAX_PLAYER = 4;

    private final String code;
    private String hostId;
    private boolean gameStarting;
    private boolean zoneSelectionStarted;
    private boolean gameStarted;

    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private WorldMap worldMap;

    private final Map<String, String> selectedZones = new ConcurrentHashMap<>();
    private final List<String> playerOrder = new CopyOnWriteArrayList<>();

    private final Map<String, String> currentZoneByPlayerId = new ConcurrentHashMap<>();

    private static final String ALLIANCE_COLOR_PRIMARY = "#F2F2F2";
    private static final String ALLIANCE_COLOR_SECONDARY = "#A9A9A9";
    private final Map<String, String> allianceByPlayer = new ConcurrentHashMap<>();
    private final Map<String, String> pendingAllianceRequests = new ConcurrentHashMap<>();
    private int allianceCount;

    private int currentPlayerIndex;
    private String pendingZoneName;
    private String pendingAction;

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

    public synchronized void markGameStarting() {
        this.gameStarting = true;
        this.gameStarted = false;
        this.zoneSelectionStarted = false;
        this.selectedZones.clear();
    }

    public synchronized void markZoneSelectionStarted() {
        this.gameStarting = false;
        this.gameStarted = false;
        this.zoneSelectionStarted = true;
        this.selectedZones.clear();
    }

    public synchronized void cancelGameStarting() {
        this.gameStarting = false;
        this.zoneSelectionStarted = false;
        this.selectedZones.clear();
    }

    public synchronized void markGameStarted() {
        this.gameStarting = false;
        this.zoneSelectionStarted = false;
        this.gameStarted = true;
        this.currentPlayerIndex = 0;
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

    public void addPlayer(Player player) throws Exception {
        if (players.containsKey(player.getId())) throw new Exception("Player already in the room");
        if (players.size() >= MAX_PLAYER) throw new Exception("Room is full");

        players.put(player.getId(), player);
        playerOrder.add(player.getId());
    }

    public void removePlayer(String playerId) {
        players.remove(playerId);
        selectedZones.remove(playerId);
        currentZoneByPlayerId.remove(playerId);
        pendingAllianceRequests.remove(playerId);
        pendingAllianceRequests.entrySet().removeIf(entry -> playerId.equals(entry.getValue()));

        int removedIndex = playerOrder.indexOf(playerId);
        if (removedIndex >= 0) {
            playerOrder.remove(removedIndex);
            if (removedIndex < currentPlayerIndex) {
                currentPlayerIndex = Math.max(0, currentPlayerIndex - 1);
            } else if (currentPlayerIndex >= playerOrder.size()) {
                currentPlayerIndex = 0;
            }
        }
    }




    public void setHostId(String hostId) throws Exception {
        if (!players.containsKey(hostId)) {
            throw new Exception("Host must be a player in the room");
        }

        this.hostId = hostId;
    }

    public void generateWorldMap() {
        worldMap = new WorldMap();
    }

    public synchronized void initializeGameState(Map<String, String> selectedZonesByPlayerId) {
        if (worldMap == null || selectedZonesByPlayerId == null) return;

        currentZoneByPlayerId.clear();
        for (Map.Entry<String, String> entry : selectedZonesByPlayerId.entrySet()) {
            String playerId = entry.getKey();
            String zoneName = entry.getValue();
            if (zoneName == null) continue;

            Zone zone = getZone(zoneName);
            if (zone == null) continue;

            zone.setNameOwner(playerId);
            zone.setPower(4);
            currentZoneByPlayerId.put(playerId, zoneName);
        }
    }

    public synchronized String getCurrentZoneName(String playerId) {
        return playerId == null ? null : currentZoneByPlayerId.get(playerId);
    }

    public synchronized List<Zone> getPlayerZones(String playerId) {
        if (playerId == null || worldMap == null) return List.of();

        return worldMap.getBlocs().stream()
            .flatMap(bloc -> bloc.getZones().stream())
            .filter(zone -> playerId.equals(zone.getNameOwner()))
            .toList();
    }

    public synchronized void setCurrentZoneName(String playerId, String zoneName) {
        if (playerId == null) return;
        if (zoneName == null || zoneName.isBlank()) {
            currentZoneByPlayerId.remove(playerId);
        } else {
            currentZoneByPlayerId.put(playerId, zoneName);
        }
    }


    public String getCode() {
        return code;
    }

    public Map<String, String> getAllThemeZone() {
        Map<String, String> themeZones = new HashMap<>();

        worldMap.getBlocs()
            .forEach(bloc -> bloc.getZones()
            .forEach(zone -> themeZones.put(
                zone.getName(), zone.getThemes().getLabel()
            )));

        return themeZones;
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

    public List<String> getPlayerOrder() {
        return List.copyOf(playerOrder);
    }

    public String getCurrentPlayerId() {
        if (playerOrder.isEmpty()) return null;
        if (currentPlayerIndex < 0 || currentPlayerIndex >= playerOrder.size()) {
            currentPlayerIndex = 0;
        }
        return playerOrder.get(currentPlayerIndex);
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void advanceTurn() {
        if (playerOrder.isEmpty()) {
            currentPlayerIndex = 0;
            return;
        }
        currentPlayerIndex = (currentPlayerIndex + 1) % playerOrder.size();
    }

    public Zone getZone(String name) {
        return worldMap.getAllZones().stream()
            .filter(zone -> zone.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }

    public void setPendingZone(String zoneName) {
        pendingZoneName = zoneName;
    }

    public void setPendingAction(String action) {
        pendingAction = action;
    }

    public String getPendingZone() {
        return pendingZoneName;
    }

    public String getPendingAction() {
        return pendingAction;
    }

    public synchronized String getPendingAllianceRequester(String targetPlayerId) {
        return pendingAllianceRequests.get(targetPlayerId);
    }

    public synchronized boolean hasPendingAllianceRequestFrom(String requesterId) {
        return pendingAllianceRequests.values().stream().anyMatch(requesterId::equals);
    }

    public synchronized boolean setPendingAllianceRequest(String requesterId, String targetPlayerId) {
        if (requesterId == null || targetPlayerId == null) return false;
        if (requesterId.equals(targetPlayerId)) return false;
        if (hasAlliance(requesterId) || hasAlliance(targetPlayerId)) return false;
        if (pendingAllianceRequests.containsKey(targetPlayerId)) return false;
        if (hasPendingAllianceRequestFrom(requesterId)) return false;

        pendingAllianceRequests.put(targetPlayerId, requesterId);
        return true;
    }

    public synchronized void clearPendingAllianceRequest(String targetPlayerId) {
        pendingAllianceRequests.remove(targetPlayerId);
    }

    public synchronized void clearPendingAllianceRequestsFrom(String requesterId) {
        pendingAllianceRequests.entrySet().removeIf(entry -> requesterId.equals(entry.getValue()));
    }

    public synchronized boolean hasAlliance(String playerId) {
        return allianceByPlayer.containsKey(playerId);
    }

    public synchronized String getAlliancePartner(String playerId) {
        return allianceByPlayer.get(playerId);
    }

    public String getCurrentPhase() {
        if (isGameStarted()) return "IN_PROGRESS";
        if (isGameStarting()) return "STARTING";
        if (isZoneSelectionStarted()) return "ZONE_SELECTION";
        return "LOBBY";
    }

    public WorldMap getWorldMap() {
        return worldMap;
    }

    public synchronized String createAlliance(String playerAId, String playerBId) {
        String color = nextAllianceColor();
        allianceByPlayer.put(playerAId, playerBId);
        allianceByPlayer.put(playerBId, playerAId);
        return color;
    }

    public synchronized void clearAllianceForPlayer(String playerId) {
        String allyId = allianceByPlayer.remove(playerId);
        if (allyId != null) {
            allianceByPlayer.remove(allyId);
        }
    }

    private synchronized String nextAllianceColor() {
        allianceCount++;
        if (allianceCount == 1) return ALLIANCE_COLOR_PRIMARY;
        return ALLIANCE_COLOR_SECONDARY;
    }

    public synchronized Player checkWinner() {
        if (!gameStarted) return null;

        for (Player player : players.values()) {
            int totalZones = 0;
            String playerId = player.getId();

            totalZones += countZonesForPlayer(playerId);

            String allyId = allianceByPlayer.get(playerId);
            if (allyId != null && players.containsKey(allyId)) {
                totalZones += countZonesForPlayer(allyId);
            }

            if (totalZones >= 21) {
                return player;
            }
        }

        return null;
    }

    private int countZonesForPlayer(String playerId) {
        if (worldMap == null) return 0;

        return (int) worldMap.getBlocs().stream()
            .flatMap(bloc -> bloc.getZones().stream())
            .filter(zone -> playerId.equals(zone.getNameOwner()))
            .count();
    }
}
