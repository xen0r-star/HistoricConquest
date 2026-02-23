package com.historicconquest.historicconquest.player;

import com.historicconquest.historicconquest.map.Zone;

public class Pawn {
    private int power;
    private Zone localisation;
    private boolean isTravelling;
    private int remainingRounds;

    public Pawn(int power, Zone localisation, boolean isTravelling, int remainingRounds) {
        this.power = power;
        this.localisation = localisation;
        this.isTravelling = isTravelling;
        this.remainingRounds = remainingRounds;
    }
}
