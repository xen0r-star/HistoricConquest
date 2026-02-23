package com.historicconquest.historicconquest.player;

import java.util.List;

public class Player {
    private final int id;
    private final String pseudo;
    private final PlayerColor color;
    private int consecutiveSuccesses;
    private int consecutiveFailures;
    private Alliance alliance;
    private List<Pawn> pawns;

    public Player(int id, String pseudo, PlayerColor color) {
        this.id = id;
        this.pseudo = pseudo;
        this.color = color;
        this.consecutiveSuccesses = 0;
        this.consecutiveFailures = 0;
    }


    public void setAlliance(Alliance alliance) {
        this.alliance = alliance;
    }
}
