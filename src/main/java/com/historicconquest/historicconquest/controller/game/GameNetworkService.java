package com.historicconquest.historicconquest.controller.game;

import com.historicconquest.historicconquest.controller.overlay.Notification;
import com.historicconquest.historicconquest.controller.overlay.NotificationController;
import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.network.model.RoomPlayer;
import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.model.specialCard.SpecialCard;
import com.historicconquest.historicconquest.model.specialCard.SpecialCardFactory;
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

    public static void handleGameAction(String action, String playerId, String zoneName, Integer difficulty, Boolean correct) {
        if (!enabled || action == null) return;

        if (MultiplayerGameOverlay.isActive()) {
            MultiplayerGameOverlay.applyNetworkAction(action, zoneName, difficulty, correct);
            return;
        }

        if (controller == null) return;

        if (difficulty != null) {
            controller.setCurrentDifficulty(difficulty);
        }

        if (!isLocalTurn()) {
            String statusMessage = buildActionStatusMessage(action, playerId, zoneName, correct);
            if (statusMessage != null) {
                controller.setTurnStatusMessage(statusMessage);
            }
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
            controller.applyPlayerChange();
        }
    }

    public static void handleAnswerResult(String playerId, Boolean correct) {
        if (!enabled || controller == null || playerId == null || correct == null) return;
        if (isLocalTurn()) return;

        Player player = getPlayerByNetworkId(playerId);
        String name = player == null ? "Player" : player.getPseudo();
        String message = correct
            ? name + " answered the question correctly."
            : name + " answered the question incorrectly.";

        controller.setTurnStatusMessage(message);
    }

    public static void handleBonusMalus(String playerId, String kind, String nameKind, Map<String, Object> resultSpecialCard) {
        if (!enabled || playerId == null || kind == null || nameKind == null) return;

        Player player = getPlayerByNetworkId(playerId);
        if (player == null) return;

        if (kind.trim().equalsIgnoreCase("BONUS")) {
            SpecialCard specialCard = SpecialCardFactory.getBonus(nameKind);
            if (specialCard == null) return;
            specialCard.apply(player, resultSpecialCard);

            NotificationController.show(
                "Epic Series for " + player.getPseudo(),
                specialCard.getName() + ": " + specialCard.getDescription(),
                Notification.Type.SUCCESS,
                10000
            );
            player.setConsecutiveSuccesses(0);

        } else {
            SpecialCard specialCard = SpecialCardFactory.getMalus(nameKind);
            if (specialCard == null) return;
            specialCard.apply(player, resultSpecialCard);

            NotificationController.show(
                "Critical Failure for " + player.getPseudo(),
                specialCard.getName() + ": " + specialCard.getDescription(),
                Notification.Type.ERROR,
                10000
            );

            player.setConsecutiveFailures(0);
        }
    }

    public static void handleActionSelected(String action, String playerId, String zoneName, Integer difficulty) {
        if (!enabled || controller == null || playerId == null) return;
        if (isLocalTurn()) return;

        String message = buildActionSelectedMessage(action, playerId, zoneName, difficulty);
        controller.setTurnStatusMessage(message);
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

    public static void handleGameWon(String winnerName) {
        if (!enabled || controller == null || winnerName == null || winnerName.isBlank()) return;
        controller.showEndGame(winnerName);
    }

    private static void applyZoneAction(String zoneName, ZoneAction actionHandler) {
        if (zoneName == null || zoneName.isBlank()) return;

        Zone zone = controller.getWorldMap().findZoneByName(zoneName);
        if (zone == null) return;

        actionHandler.apply(zone, false);
    }

    private static String buildActionStatusMessage(String action, String playerId, String zoneName, Boolean correct) {
        Player player = getPlayerByNetworkId(playerId);
        String name = player == null ? "Player" : player.getPseudo();

        return switch (action) {
            case ACTION_TRAVEL -> name + " wants to travel to " + safeZone(zoneName) + ".";
            case ACTION_ATTACK -> name + " wants to attack " + safeZone(zoneName) + ".";
            case ACTION_POWER_UP -> name + " wants to power up " + safeZone(zoneName) + ".";
            case ACTION_ANSWER_RESULT -> {
                if (correct == null) yield name + " is answering the question...";
                yield correct
                    ? name + " answered the question correctly."
                    : name + " answered the question incorrectly.";
            }
            default -> null;
        };
    }

    private static String buildActionSelectedMessage(String action, String playerId, String zoneName, Integer difficulty) {
        Player player = getPlayerByNetworkId(playerId);
        String name = player == null ? "Player" : player.getPseudo();
        String level = difficulty == null ? "a" : "level " + difficulty;

        return switch (action) {
            case ACTION_TRAVEL -> name + " chose to travel to " + safeZone(zoneName) + " and is answering a " + level + " question.";
            case ACTION_ATTACK -> name + " chose to attack " + safeZone(zoneName) + " and is answering a " + level + " question.";
            case ACTION_POWER_UP -> name + " chose to power up " + safeZone(zoneName) + " and is answering a " + level + " question.";
            default -> name + " chose an action and is answering a " + level + " question.";
        };
    }

    private static String safeZone(String zoneName) {
        return zoneName == null || zoneName.isBlank() ? "a zone" : zoneName;
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
