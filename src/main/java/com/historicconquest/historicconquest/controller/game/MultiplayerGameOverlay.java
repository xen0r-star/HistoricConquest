package com.historicconquest.historicconquest.controller.game;

import com.historicconquest.historicconquest.controller.overlay.Notification;
import com.historicconquest.historicconquest.controller.overlay.NotificationController;
import com.historicconquest.historicconquest.model.map.WorldMap;
import com.historicconquest.historicconquest.model.map.Zone;

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

    public static boolean ensureLocalTurn(String actionLabel) {
        if (!GameNetworkService.isEnabled()) {
            return true;
        }

        if (GameNetworkService.isLocalTurn()) {
            return true;
        }

        String label = (actionLabel == null || actionLabel.isBlank()) ? "perform this action" : actionLabel;
        NotificationController.show(
                "Not your turn",
                "Wait for your turn before you " + label + ".",
                Notification.Type.INFORMATION,
                3000
        );
        return false;
    }

    public static void requestQuestionResult(int difficulty, boolean correct) {
        if (GameNetworkService.isEnabled()) {
            if (!ensureLocalTurn("answer")) {
                return;
            }
            GameNetworkService.sendAnswerResult(difficulty, correct);
            return;
        }

        if (controller != null) {
            controller.applyQuestionResult(difficulty, correct);
        }
    }

    public static void requestZoneAction(GameController.PendingAction action, Zone targetZone) {
        if (action == null || targetZone == null) {
            return;
        }

        if (GameNetworkService.isEnabled()) {
            if (!ensureLocalTurn("choose an action")) {
                return;
            }

            switch (action) {
                case TRAVEL -> GameNetworkService.sendTravelAction(targetZone.getName());
                case ATTACK -> GameNetworkService.sendAttackAction(targetZone.getName());
                case POWER_UP -> GameNetworkService.sendPowerUpAction(targetZone.getName());
                default -> { }
            }
            return;
        }

        if (controller == null) {
            return;
        }

        controller.setTargetZone(targetZone);

        switch (action) {
            case TRAVEL -> controller.applyTravel(targetZone);
            case ATTACK -> controller.applyAttackZone(targetZone);
            case POWER_UP -> controller.applyPowerUp(targetZone);
            default -> {
            }
        }
    }

    public static void applyNetworkAction(String action, String zoneName, Integer difficulty, Boolean correct) {
        if (!isActive() || action == null) {
            return;
        }

        switch (action) {
            case "ANSWER_RESULT" -> {
                if (difficulty == null || correct == null) return;
                controller.applyQuestionResult(difficulty, correct);
            }
            case "TRAVEL" -> applyZoneAction(zoneName, (targetZone, advanceTurn) -> controller.applyTravel(targetZone));
            case "ATTACK" -> applyZoneAction(zoneName, (targetZone, advanceTurn) -> controller.applyAttackZone(targetZone));
            case "POWER_UP" -> applyZoneAction(zoneName, (targetZone, advanceTurn) -> controller.applyPowerUp(targetZone));
            default -> {
            }
        }
    }

    private static void applyZoneAction(String zoneName, ZoneAction actionHandler) {
        if (zoneName == null || zoneName.isBlank() || actionHandler == null) {
            return;
        }

        Zone zone = findZoneByName(zoneName);
        if (zone == null) {
            return;
        }

        actionHandler.apply(zone, false);
    }

    private static Zone findZoneByName(String zoneName) {
        if (zoneName == null || zoneName.isBlank() || worldMap == null) {
            return null;
        }

        for (Zone zone : worldMap.getAllZones()) {
            if (zoneName.equalsIgnoreCase(zone.getName())) {
                return zone;
            }
        }

        return null;
    }

    @FunctionalInterface
    private interface ZoneAction {
        void apply(Zone zone, boolean advanceTurn);
    }
}

