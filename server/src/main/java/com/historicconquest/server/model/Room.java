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
    private boolean gameStarted;
    private long gameStartAt;

    private final Map<String, Player> players = new ConcurrentHashMap<>();

    public Room(String code) {
        this.code = code;
    }


    public boolean isHost(String playerId) {
        return hostId != null && hostId.equals(playerId);
    }

    public synchronized boolean canStartGame() {
        return !gameStarting && hasLaunchConditions();
    }

    public synchronized boolean hasLaunchConditions() {
        if (gameStarted) return false;
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
        this.gameStartAt = gameStartAt;
    }

    public synchronized void cancelGameStarting() {
        this.gameStarting = false;
        this.gameStartAt = 0L;
    }

    public synchronized void markGameStarted() {
        this.gameStarting = false;
        this.gameStarted = true;
        this.gameStartAt = 0L;
    }

    public boolean isPseudoAvailable(String pseudo) {
        for (Player player : players.values()) {
            if (player.getPseudo().equals(pseudo)) {
                return false;
            }
        }

        return true;
    }

    public void addPlayer(Player player) throws Exception {
        if (players.containsKey(player.getId())) throw new Exception("Player already in the room");
        if (players.size() >= MAX_PLAYER) throw new Exception("Room is full");

        players.put(player.getId(), player);
    }

    public void removePlayer(String playerId) {
        players.remove(playerId);
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
        if ("bot".equalsIgnoreCase(player.getType())) return true;
        return "Ready".equalsIgnoreCase(player.getStatus());
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
