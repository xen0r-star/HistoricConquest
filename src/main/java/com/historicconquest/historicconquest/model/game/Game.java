package com.historicconquest.historicconquest.model.game;

import com.historicconquest.historicconquest.model.map.Zone;

public class Game {
    private static Game instance;
    private final GameActionExecutor actionExecutor;

    private Game(boolean modeMulti) {
        if (modeMulti) {
            this.actionExecutor = new NetworkGameExecutor();
        } else {
            this.actionExecutor = new LocalGameExecutor();
        }
    }

    public static Game init(boolean modeMulti) {
        if (instance == null) {
            instance = new Game(modeMulti);
        }
        return instance;
    }

    public static Game getInstance() {
        return instance;
    }

    public void handleZoneSelection(Zone targetZone) {
        actionExecutor.handleZoneSelection(targetZone);
    }

    public boolean isNetworkGame() {
        return actionExecutor instanceof NetworkGameExecutor;
    }
}
