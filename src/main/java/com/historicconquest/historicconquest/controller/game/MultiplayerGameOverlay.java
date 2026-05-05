package com.historicconquest.historicconquest.controller.game;

import com.historicconquest.historicconquest.controller.overlay.Notification;
import com.historicconquest.historicconquest.controller.overlay.NotificationController;
import com.historicconquest.historicconquest.model.map.WorldMap;
import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.player.Player;

public final class MultiplayerGameOverlay {
    private static GameController controller;
    private static WorldMap worldMap;

    private MultiplayerGameOverlay() {
    }

    public static void attach(GameController gameController, WorldMap map) {
        controller = gameController;
        worldMap = map;
    }

    public static boolean isActive() {
        return controller != null && worldMap != null;
    }

    public static boolean ensureRemoteTurn(String actionLabel) {
        if (!GameNetworkService.isEnabled()) {
            return false;
        }

        if (GameNetworkService.isLocalTurn()) {
            return false;
        }

        String label = (actionLabel == null || actionLabel.isBlank()) ? "perform this action" : actionLabel;
        NotificationController.show(
            "Not your turn",
            "Wait for your turn before you " + label + ".",
            Notification.Type.INFORMATION,
            3000
        );
        return true;
    }

    public static void requestZoneAction(GameController.PendingAction action, Zone targetZone) {
        if (action == null) return;
        if (targetZone == null) {
            GameController controller = GameController.getInstance();
            if (controller == null) return;

            Player current = controller.getCurrentPlayer();
            if (current == null || current.getCurrentZone() == null) return;
            targetZone = current.getCurrentZone();
        }

        if (GameNetworkService.isEnabled()) {
            if (ensureRemoteTurn("choose an action")) {
                return;
            }


            switch (action) {
                case TRAVEL -> GameNetworkService.sendTravelAction(
                    targetZone.getName(),
                    GameController.getInstance().getCurrentDistance()
                );
                case ATTACK -> GameNetworkService.sendAttackAction(
                    targetZone.getName(),
                    GameController.getInstance().getCurrentDifficulty()
                );
                case POWER_UP -> GameNetworkService.sendPowerUpAction(
                    targetZone.getName(),
                    GameController.getInstance().getCurrentDifficulty()
                );
                default -> { }
            }
            return;
        }

        if (controller == null) return;
        controller.setTargetZone(targetZone);

        switch (action) {
            case TRAVEL -> controller.applyTravel(targetZone);
            case ATTACK -> controller.applyAttackZone(targetZone);
            case POWER_UP -> controller.applyPowerUp(targetZone);
            default -> { }
        }
    }

    public static void applyNetworkAction(String action, String zoneName, Integer difficulty, Boolean correct) {
        if (!isActive() || action == null) {
            return;
        }

        if (difficulty != null) {
            controller.setCurrentDifficulty(difficulty);
        }

        switch (action) {
            case "ANSWER_RESULT" -> {
                if (difficulty == null || correct == null) return;
                controller.applyQuestionResult(difficulty, correct);
            }
            case "TRAVEL" -> applyZoneAction(zoneName, (targetZone, advanceTurn) -> controller.applyTravel(targetZone));
            case "ATTACK" -> applyZoneAction(zoneName, (targetZone, advanceTurn) -> controller.applyAttackZone(targetZone));
            case "POWER_UP" -> applyZoneAction(zoneName, (targetZone, advanceTurn) -> controller.applyPowerUp(targetZone));
            default -> { }
        }
    }

    private static void applyZoneAction(String zoneName, ZoneAction actionHandler) {
        if (zoneName == null || zoneName.isBlank() || actionHandler == null) {
            return;
        }

        Zone zone = worldMap.findZoneByName(zoneName);
        if (zone == null) {
            return;
        }

        actionHandler.apply(zone, false);
    }


    @FunctionalInterface
    private interface ZoneAction {
        void apply(Zone zone, boolean advanceTurn);
    }

    public static WorldMap getWorldMap() {
        return worldMap;
    }
}

