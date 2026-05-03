package com.historicconquest.historicconquest.model.game;

import com.historicconquest.historicconquest.model.map.Zone;

public class Game {
    private final GameActionExecutor actionExecutor;

    public Game(boolean modeMulti) {
        if (modeMulti) {
            this.actionExecutor = new NetworkGameExecutor();
        } else {
            this.actionExecutor = new LocalGameExecutor();
        }
    }

    public void handleZoneSelection(Zone targetZone) {
        actionExecutor.handleZoneSelection(targetZone);
    }
}