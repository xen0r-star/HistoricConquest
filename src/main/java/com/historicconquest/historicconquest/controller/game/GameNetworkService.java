package com.historicconquest.historicconquest.controller.game;

import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.network.model.RoomPlayer;
import com.historicconquest.historicconquest.service.network.RoomService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GameNetworkService {
    private static final String ACTION_ANSWER_RESULT = "ANSWER_RESULT";
    private static final String ACTION_TRAVEL = "TRAVEL";
    private static final String ACTION_ATTACK = "ATTACK";
    private static final String ACTION_POWER_UP = "POWER_UP";

    private static GameController controller;
    private static boolean enabled;
    private static String localPlayerId;
    private static String currentPlayerId;
    private static List<String> startTurnOrder;
    private static String startCurrentPlayerId;
    private static final Map<String, Integer> playerIndexById = new HashMap<>();

    private GameNetworkService() {
    }

    public static void setStartInfo(List<String> turnOrder, String currentPlayerId) {
        startTurnOrder = turnOrder;
        startCurrentPlayerId = currentPlayerId;
    }

    public static void attach(GameController gameController, List<RoomPlayer> roomPlayers) {
        if (gameController == null || roomPlayers == null || roomPlayers.isEmpty()) {
            enabled = false;
            return;
        }

        controller = gameController;
        enabled = true;
        localPlayerId = RoomService.getPlayerId();
        playerIndexById.clear();

        List<String> turnOrder = startTurnOrder != null && !startTurnOrder.isEmpty()
            ? startTurnOrder
            : roomPlayers.stream().map(RoomPlayer::getId).toList();

        for (int i = 0; i < turnOrder.size(); i++) {
            playerIndexById.put(turnOrder.get(i), i);
        }

        currentPlayerId = startCurrentPlayerId != null ? startCurrentPlayerId : (turnOrder.isEmpty() ? null : turnOrder.getFirst());
        if (currentPlayerId != null) {
            Integer index = playerIndexById.get(currentPlayerId);
            if (index != null) {
                controller.setCurrentPlayerIndexFromNetwork(index);
            }
        }

        controller.setPlayerCount(roomPlayers.size());
    }

    public static void detach() {
        enabled = false;
        controller = null;
        localPlayerId = null;
        currentPlayerId = null;
        playerIndexById.clear();
        startTurnOrder = null;
        startCurrentPlayerId = null;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isLocalTurn() {
        return enabled && localPlayerId != null && localPlayerId.equals(currentPlayerId);
    }

    public static void sendAnswerResult(int difficulty, boolean correct) {
        if (!enabled) return;

        RoomService.sendGameAction(
            ACTION_ANSWER_RESULT,
            Map.of(
                "difficulty", difficulty,
                "correct", correct
            )
        );
    }

    public static void sendZoneAction(String action, String zoneName) {
        if (!enabled) return;

        RoomService.sendGameAction(
            action,
            Map.of("zone", zoneName)
        );
    }

    public static void handleGameAction(String action, String zoneName, Integer difficulty, Boolean correct) {
        if (!enabled || controller == null || action == null) return;

        switch (action) {
            case ACTION_ANSWER_RESULT -> {
                if (difficulty == null || correct == null) return;
                controller.applyQuestionResult(difficulty, correct, false);
            }
            case ACTION_TRAVEL -> applyZoneAction(zoneName, controller::applyTravel);
            case ACTION_ATTACK -> applyZoneAction(zoneName, controller::applyAttackZone);
            case ACTION_POWER_UP -> applyZoneAction(zoneName, controller::applyPowerUp);
            default -> {
            }
        }
    }

    public static void handleTurnChanged(String nextPlayerId, Integer nextPlayerIndex) {
        if (!enabled || controller == null || nextPlayerId == null) return;

        currentPlayerId = nextPlayerId;
        Integer index = playerIndexById.get(nextPlayerId);
        if (index == null && nextPlayerIndex != null) {
            index = nextPlayerIndex;
        }

        if (index != null) {
            controller.setCurrentPlayerIndexFromNetwork(index);
        }
    }

    private static void applyZoneAction(String zoneName, ZoneAction actionHandler) {
        if (zoneName == null || zoneName.isBlank()) return;

        Zone zone = controller.findZoneByName(zoneName);
        if (zone == null) return;

        actionHandler.apply(zone, false);
    }

    @FunctionalInterface
    private interface ZoneAction {
        void apply(Zone zone, boolean advanceTurn);
    }
}
