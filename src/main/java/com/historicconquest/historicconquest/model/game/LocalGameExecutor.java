package com.historicconquest.historicconquest.model.game;

import com.historicconquest.historicconquest.controller.game.GameController;
import com.historicconquest.historicconquest.model.map.Zone;

public class LocalGameExecutor implements GameActionExecutor {
    private final GameController gameController;

    public LocalGameExecutor() {
        this.gameController = GameController.getInstance();
    }

    @Override
    public void handleZoneSelection(Zone targetZone) {
        gameController.setTargetZone(targetZone);
        if (gameController.getSelectedAction() == GameController.PendingAction.TRAVEL) {
            gameController.previewTravelTarget();
        }
    }
}

