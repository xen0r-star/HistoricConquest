package com.historicconquest.historicconquest.game;

public class NewGameConfig {
    private final String playerName;
    private final int playerCount;

    public NewGameConfig(String playerName, int playerCount) {
        this.playerName = playerName;
        this.playerCount = playerCount;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getPlayerCount() {
        return playerCount;
    }
}