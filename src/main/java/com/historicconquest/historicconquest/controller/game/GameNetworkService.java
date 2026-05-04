package com.historicconquest.historicconquest.controller.game;

import com.historicconquest.historicconquest.controller.overlay.Notification;
import com.historicconquest.historicconquest.controller.overlay.NotificationController;
import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.network.model.RoomPlayer;
import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.service.network.RoomService;
import javafx.scene.paint.Color;

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
    private static final Map<Integer, String> playerIdByIndex = new HashMap<>();

    private GameNetworkService() { }

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
        playerIdByIndex.clear();

        List<String> turnOrder = startTurnOrder != null && !startTurnOrder.isEmpty()
            ? startTurnOrder
            : roomPlayers.stream().map(RoomPlayer::getId).toList();

        for (int i = 0; i < turnOrder.size(); i++) {
            playerIndexById.put(turnOrder.get(i), i);
        }

        for (int i = 0; i < roomPlayers.size(); i++) {
            playerIdByIndex.put(i, roomPlayers.get(i).getId());
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
        playerIdByIndex.clear();
        startTurnOrder = null;
        startCurrentPlayerId = null;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isLocalTurn() {
        return enabled && localPlayerId != null && localPlayerId.equals(currentPlayerId);
    }

    public static void sendZoneAction(String action, String zoneName, int difficulty) {
        if (!enabled) return;

        RoomService.sendGameAction(
            action,
            Map.of("zone", zoneName,  "difficulty", difficulty)
        );
    }

    public static void sendTravelAction(String zoneName, int difficulty) {
        sendZoneAction(ACTION_TRAVEL, zoneName, difficulty);
    }

    public static void sendAttackAction(String zoneName, int difficulty) {
        sendZoneAction(ACTION_ATTACK, zoneName, difficulty);
    }

    public static void sendPowerUpAction(String zoneName, int difficulty) {
        sendZoneAction(ACTION_POWER_UP, zoneName, difficulty);
    }

    public static void sendSkipAction() {
        RoomService.sendGameAction(
            "SKIP",
            Map.of()
        );
    }

    public static void sendCoalitionRequest(Player target) {
        if (!enabled || target == null) return;

        String targetId = getNetworkPlayerId(target);
        if (targetId == null) return;

        RoomService.sendCoalitionRequest(targetId);
    }

    public static void sendCoalitionAccept(Player requester) {
        if (!enabled || requester == null) return;

        String requesterId = getNetworkPlayerId(requester);
        if (requesterId == null) return;

        RoomService.sendCoalitionAccept(requesterId);
    }

    public static void sendCoalitionDecline(Player requester) {
        if (!enabled || requester == null) return;

        String requesterId = getNetworkPlayerId(requester);
        if (requesterId == null) return;

        RoomService.sendCoalitionDecline(requesterId);
    }

    public static void handleGameAction(String action, String zoneName, Integer difficulty, Boolean correct) {
        if (!enabled || action == null) return;

        if (MultiplayerGameOverlay.isActive()) {
            MultiplayerGameOverlay.applyNetworkAction(action, zoneName, difficulty, correct);
            return;
        }

        if (controller == null) return;

        if (difficulty != null) {
            controller.setCurrentDifficulty(difficulty);
        }

        switch (action) {
            case ACTION_ANSWER_RESULT -> {
                if (difficulty == null || correct == null) return;
                controller.applyQuestionResult(difficulty, correct);
            }
            case ACTION_TRAVEL -> applyZoneAction(zoneName, (targetZone, advanceTurn) -> controller.applyTravel(targetZone));
            case ACTION_ATTACK -> applyZoneAction(zoneName, (targetZone, advanceTurn) -> controller.applyAttackZone(targetZone));
            case ACTION_POWER_UP -> applyZoneAction(zoneName, (targetZone, advanceTurn) -> controller.applyPowerUp(targetZone));
            default -> { }
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

    public static void handleCoalitionRequested(String requesterId, String targetId) {
        if (!enabled || controller == null || requesterId == null || targetId == null) return;

        if (!targetId.equals(localPlayerId)) {
            return;
        }

        Player requester = getPlayerByNetworkId(requesterId);
        Player target = getPlayerByNetworkId(targetId);
        if (requester == null || target == null) return;

        target.setPendingAllianceRequest(requester);
        NotificationController.show(
            "Alliance Request",
            requester.getPseudo() + " wants to form an alliance with you!",
            Notification.Type.INFORMATION,
            7000
        );
    }

    public static void handleCoalitionAccepted(String playerAId, String playerBId, String allianceColor) {
        if (!enabled || controller == null || playerAId == null || playerBId == null) return;

        Player playerA = getPlayerByNetworkId(playerAId);
        Player playerB = getPlayerByNetworkId(playerBId);
        if (playerA == null || playerB == null) return;

        Color color = parseAllianceColor(allianceColor);

        applyAlliance(playerA, playerB, color);

        if (playerAId.equals(localPlayerId) || playerBId.equals(localPlayerId)) {
            NotificationController.show(
                "Alliance",
                "You are now allied with " + (playerAId.equals(localPlayerId) ? playerB.getPseudo() : playerA.getPseudo()),
                Notification.Type.SUCCESS,
                5000
            );
        }
    }

    public static void handleCoalitionDeclined(String requesterId, String targetId) {
        if (!enabled || controller == null || requesterId == null || targetId == null) return;

        Player target = getPlayerByNetworkId(targetId);
        if (target != null) {
            target.clearPendingRequest();
        }

        if (requesterId.equals(localPlayerId)) {
            NotificationController.show(
                "Alliance",
                "Your alliance request was declined.",
                Notification.Type.INFORMATION,
                4000
            );
        }
    }

    public static void handleCoalitionBroken(String playerAId, String playerBId) {
        if (!enabled || controller == null || playerAId == null || playerBId == null) return;

        Player playerA = getPlayerByNetworkId(playerAId);
        Player playerB = getPlayerByNetworkId(playerBId);
        if (playerA == null || playerB == null) return;

        clearAlliance(playerA, playerB);

        if (playerAId.equals(localPlayerId) || playerBId.equals(localPlayerId)) {
            NotificationController.show(
                "Alliance",
                "Your alliance has ended.",
                Notification.Type.INFORMATION,
                4000
            );
        }
    }

    private static void applyZoneAction(String zoneName, ZoneAction actionHandler) {
        if (zoneName == null || zoneName.isBlank()) return;

        Zone zone = controller.findZoneByName(zoneName);
        if (zone == null) return;

        actionHandler.apply(zone, false);
    }

    private static String getNetworkPlayerId(Player player) {
        if (player == null) return null;
        return playerIdByIndex.get(player.getId());
    }

    private static Player getPlayerByNetworkId(String playerId) {
        if (controller == null || playerId == null) return null;

        Integer index = playerIndexById.get(playerId);
        if (index == null) return null;

        List<Player> players = controller.getPlayers();
        if (index < 0 || index >= players.size()) return null;

        return players.get(index);
    }

    private static void applyAlliance(Player playerA, Player playerB, Color allianceColor) {
        playerA.setAlly(playerB);
        playerB.setAlly(playerA);
        playerA.setCurrentAllianceColor(allianceColor);
        playerB.setCurrentAllianceColor(allianceColor);
        playerA.clearPendingRequest();
        playerB.clearPendingRequest();

        for (Zone zone : playerA.getZones()) {
            zone.setColor(allianceColor);
        }
        for (Zone zone : playerB.getZones()) {
            zone.setColor(allianceColor);
        }
    }

    private static void clearAlliance(Player playerA, Player playerB) {
        playerA.setAlly(null);
        playerB.setAlly(null);
        playerA.setCurrentAllianceColor(null);
        playerB.setCurrentAllianceColor(null);
        playerA.clearPendingRequest();
        playerB.clearPendingRequest();

        for (Zone zone : playerA.getZones()) {
            zone.setColor(playerA.getColor().getJavafxColor());
        }
        for (Zone zone : playerB.getZones()) {
            zone.setColor(playerB.getColor().getJavafxColor());
        }
    }

    private static Color parseAllianceColor(String allianceColor) {
        if (allianceColor == null || allianceColor.isBlank()) {
            return GameController.ALLIANCE_1_COLOR;
        }

        try {
            return Color.web(allianceColor.trim());

        } catch (IllegalArgumentException e) {
            return GameController.ALLIANCE_1_COLOR;
        }
    }

    @FunctionalInterface
    private interface ZoneAction {
        void apply(Zone zone, boolean advanceTurn);
    }
}
