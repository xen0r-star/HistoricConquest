package com.historicconquest.historicconquest.model.game;

import com.historicconquest.historicconquest.controller.game.GameController;
import com.historicconquest.historicconquest.controller.game.MultiplayerGameOverlay;
import com.historicconquest.historicconquest.model.map.Zone;

public class NetworkGameExecutor implements GameActionExecutor {
    private final GameController gameController;

    public NetworkGameExecutor() {
        this.gameController = GameController.getInstance();
    }

    @Override
    public void handleZoneSelection(Zone targetZone) {
        gameController.setTargetZone(targetZone);
        if (gameController.getSelectedAction() == GameController.PendingAction.TRAVEL) {
            gameController.previewTravelTarget();
        }
        if (!gameController.canSelectZone(targetZone)) return;
        if (!gameController.isHasAnsweredCorrectly()) return;

        if (
            gameController.getSelectedAction() != GameController.PendingAction.TRAVEL ||
            gameController.getSelectedAction() != GameController.PendingAction.ATTACK ||
            gameController.getSelectedAction() != GameController.PendingAction.POWER_UP
        ) return;

        MultiplayerGameOverlay.requestZoneAction(gameController.getSelectedAction(), targetZone);
    }
}

