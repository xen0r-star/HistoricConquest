package com.historicconquest.historicconquest.model.game;

import com.historicconquest.historicconquest.controller.game.GameController;
import com.historicconquest.historicconquest.controller.game.MultiplayerGameOverlay;
import com.historicconquest.historicconquest.model.map.Zone;

public class Game {
    private final GameController gameController;


    public Game() {
        this.gameController = GameController.getInstance();
    }

    public void handleZoneSelection(Zone targetZone) {
        gameController.setTargetZone(targetZone);
        if (gameController.getSelectedAction() == GameController.PendingAction.TRAVEL) {
            gameController.previewTravelTarget();
        }

        if (!gameController.canSelectZone(targetZone)) return;
        if (!gameController.isHasAnsweredCorrectly()) return;


//        switch(gameController.getSelectedAction()) {
//            case TRAVEL:
//                MultiplayerGameOverlay.requestZoneAction(GameController.PendingAction.TRAVEL, targetZone);
//                break;
//
//            case ATTACK:
//                MultiplayerGameOverlay.requestZoneAction(GameController.PendingAction.ATTACK, targetZone);
//                break;
//
//            case POWER_UP:
//                MultiplayerGameOverlay.requestZoneAction(GameController.PendingAction.POWER_UP, targetZone);
//                break;
//
//            case NONE:
//            default:
//                break;
//        }
    }
}