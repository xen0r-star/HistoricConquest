package com.historicconquest.historicconquest.controller.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.historicconquest.historicconquest.controller.overlay.Notification;
import com.historicconquest.historicconquest.controller.overlay.NotificationController;
import com.historicconquest.historicconquest.controller.page.game.EndGame;
import com.historicconquest.historicconquest.controller.page.game.GameHUD;
import com.historicconquest.historicconquest.controller.page.game.ZoneInfoPanel;
import com.historicconquest.historicconquest.model.game.GameAnimationPort;
import com.historicconquest.historicconquest.model.map.WorldMap;
import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.map.ZonePathfinder;
import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.view.map.MapView;
import com.historicconquest.historicconquest.view.map.ZoneView;

import javafx.animation.PathTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;


public class GameController implements GameAnimationPort {
    private final ZoneInfoPanel zoneInfoPanel;
    private final MapView mapView;
    private final GameHUD gameHUD;
    private int currentPlayerIndex = 0;
    private int currentDifficulty = 0;
    private int currentDistance = 0;
    private Zone targetZone;
    private boolean hasAnsweredCorrectly;
    private static GameController instance;
    private int nbPlayer = 4;
    private final List<Player> players;
    private WorldMap worldMap;
    public static final Color ALLIANCE_1_COLOR = Color.web("#F2F2F2");
    public static final Color ALLIANCE_2_COLOR = Color.web("#A9A9A9");

    private static final int TRAVEL_HINT_MAX_DISTANCE = 4;
    private final Set<Zone> travelHintZones = new HashSet<>();

    private int allianceCount = 0;

    private PendingAction selectedAction = PendingAction.NONE;

    public enum PendingAction {
        NONE, TRAVEL, ATTACK, POWER_UP
    }


    public List<Player> getPlayers() {
        return players;
    }

    public void initializeGameState(List<Player> playersData , WorldMap worldmap , MapView mapView , Group mapInterface) {
        initializeGameState(playersData, worldmap, mapView, mapInterface, null);
    }

    public void initializeGameState(List<Player> playersData, WorldMap worldmap, MapView mapView, Group mapInterface, List<Zone> preferredStartZones) {
        this.worldMap = worldmap;
        List<Zone> allZones = worldmap.getAllZones();

        for (int i = 0; i < playersData.size(); i++) {
            Player player = playersData.get(i);

            this.addPlayer(player);

            Zone startZone = null;
            if (preferredStartZones != null && i < preferredStartZones.size()) {
                startZone = preferredStartZones.get(i);
            }
            if (startZone == null) {
                int fallbackIndex = Math.min(i * 10, allZones.size() - 1);
                startZone = allZones.get(fallbackIndex);
            }

            startZone.setColor(player.getColor().getJavafxColor());
            startZone.setPower(4);
            startZone.setNameOwner(player.getPseudo());

            player.addZone(startZone);


            ZoneView startZoneView = mapView.getViewFor(startZone);
            if (startZoneView == null) continue;

            Group pawnGroup = PawnController.createPawn(player.getColor(), 40.0);
            pawnGroup.setMouseTransparent(true);

            Bounds zoneBounds = startZoneView.getZoneSVGGroup().getBoundsInParent();
            Bounds pawnBounds = pawnGroup.getBoundsInParent();
            double pawnCenterX = pawnBounds.getMinX() + pawnBounds.getWidth() / 2.0;
            double pawnCenterY = pawnBounds.getMinY() + pawnBounds.getHeight() / 2.0;

            pawnGroup.setTranslateX(zoneBounds.getCenterX() - pawnCenterX);
            pawnGroup.setTranslateY(zoneBounds.getCenterY() - pawnCenterY);


            mapInterface.getChildren().add(pawnGroup);

            player.setCurrentZone(startZone);
            player.setPawnNode(pawnGroup);
        }

        gameHUD.refreshGameInfo(players, currentPlayerIndex);
        gameHUD.refreshPlayerInfo(getCurrentPlayer());
    }


    public GameController(ZoneInfoPanel zoneInfoPanel, GameHUD gameHUD, MapView mapView) {
        this.zoneInfoPanel = zoneInfoPanel;
        this.mapView = mapView;
        this.gameHUD = gameHUD;

        players = new ArrayList<>();

        gameHUD.initializeMap(mapView.getRoot());
        this.zoneInfoPanel.hide();

        instance = this;
    }


    public void addPlayer(Player player) {
        if (!players.contains(player)) {
            players.add(player);
        }
    }

    public void animatePawnMove(Node pawn, List<Zone> pathListe, Runnable onFinished) {
        if (pathListe == null || pathListe.isEmpty() || pawn == null) {
            if (onFinished != null) onFinished.run();
            return;
        }

        Path transitionPath = new Path();

        Bounds startBounds = getZoneBounds(pathListe.getFirst());
        if (startBounds == null) {
            if (onFinished != null) onFinished.run();
            return;
        }
        transitionPath.getElements().add(new MoveTo(startBounds.getCenterX(), startBounds.getCenterY()));

        for (int i = 1; i < pathListe.size(); i++) {
            Bounds b = getZoneBounds(pathListe.get(i));
            if (b == null) continue;
            transitionPath.getElements().add(new LineTo(b.getCenterX(), b.getCenterY()));
        }

        PathTransition pt = new PathTransition();
        pt.setNode(pawn);
        pt.setPath(transitionPath);
        pt.setDuration(Duration.seconds(pathListe.size() * 0.4));
        pt.setCycleCount(1);

        pt.setOnFinished(e -> {
            if (onFinished != null) onFinished.run();
        });

        pt.play();

    }

    private Bounds getZoneBounds(Zone zone) {
        ZoneView zoneView = mapView.getViewFor(zone);
        if (zoneView == null) return null;
        return zoneView.getZoneSVGGroup().getBoundsInParent();
    }

    public void showZoneInfo(Zone zone) {
        zoneInfoPanel.setTitleLabel(zone.getName());
//        zoneInfoPanel.setSubTitleLabel(zone.getThemes().getLabel());
        zoneInfoPanel.setSubTitleLabel("(" + zone.getBlocName() + ")");
        zoneInfoPanel.setDescriptionLabel(zone.getNameOwner() + " (" + zone.getPowertext() + ")");
        zoneInfoPanel.show();
        if (zoneInfoPanel.getRoot() != null) {
            zoneInfoPanel.getRoot().toFront();
        }
    }

    public void previewTravelTarget() {
        Player current = getCurrentPlayer();
        if (current == null || targetZone == null) {
            return;
        }

        Zone currentZone = current.getCurrentZone();
        if (currentZone == null || targetZone == currentZone) {
            return;
        }

        currentDistance = ZonePathfinder.getShortestDistance(currentZone, targetZone, TRAVEL_HINT_MAX_DISTANCE);
        if (currentDistance > 0 && currentDistance <= TRAVEL_HINT_MAX_DISTANCE && gameHUD != null) {
            gameHUD.updateTravelTargetPrompt(targetZone.getName(), currentDistance);
        }
    }


    public void applyQuestionResult(int level, boolean correct) {
        this.currentDifficulty = level;
        this.hasAnsweredCorrectly = correct;
        Player current = players.get(currentPlayerIndex);

        if (!correct) {
            NotificationController.show(
                "Wrong Answer",
                "Incorrect! Your turn is over.",
                Notification.Type.ERROR,
                5000
            );

            current.setConsecutiveFailures(current.getConsecutiveFailures() + 1);
            current.setConsecutiveSuccesses(0);

            if (current.getConsecutiveFailures() >= 3) {
                NotificationController.show(
                    "Critical Failure",
                    "3 consecutive incorrect answers! You have received a penalty.",
                    Notification.Type.ERROR,
                    6000
                );
                current.setConsecutiveFailures(3);
            }

        } else {
            switch (GameController.getInstance().getPendingAction()) {
                case TRAVEL -> applyTravel();
                case ATTACK -> applyAttackZone();
                case POWER_UP -> applyPowerUp();
                default -> { }
            }

            current.setConsecutiveSuccesses(current.getConsecutiveSuccesses() + 1);
            current.setConsecutiveFailures(0);
            if (current.getConsecutiveSuccesses() >= 3) {
                NotificationController.show(
                    "Win Streak!",
                    "3 correct answers in a row! You've earned a special bonus.",
                    Notification.Type.SUCCESS,
                    6000
                );
                current.setConsecutiveSuccesses(0);
            }
        }


        nextPlayer();
    }


    public void applyTravel(Zone targetZone) {
        setTargetZone(targetZone);
        applyTravel();
    }

    public void applyTravel() {
        Player current = getCurrentPlayer();
        if (current == null || targetZone == null) return;

        ZonePathfinder.PathResult result = ZonePathfinder.findPath(current.getCurrentZone(), targetZone);

        if (result.type() == ZonePathfinder.PathType.DIRECT) {
            int distance = result.zones().size() - 1;

            if (distance > 0 && distance <= TRAVEL_HINT_MAX_DISTANCE) {
                NotificationController.show(
                    "Traveling",
                    "Moving to " + targetZone.getName() + " (" + distance + " zones).",
                    Notification.Type.INFORMATION,
                    3000
                );

                setPendingAction(PendingAction.NONE);

                animatePawnMove(
                    current.getPawnNode(),
                    result.zones(),
                    null
                );

                current.setCurrentZone(targetZone);
                gameHUD.refreshGameInfo(players, currentPlayerIndex);
                gameHUD.refreshPlayerInfo(getCurrentPlayer());

            } else {
                NotificationController.show(
                    "Too Far",
                    "Target is " + distance + " zones away, but your answer only allows " + TRAVEL_HINT_MAX_DISTANCE + " steps!",
                    Notification.Type.ERROR,
                    6000
                );
            }
        }
    }


    public void applyAttackZone(Zone targetZone) {
        setTargetZone(targetZone);
        applyAttackZone();
    }

    public void applyAttackZone() {
        Player current = getCurrentPlayer();
        if (current == null || targetZone == null) return;
        Zone currentZone = current.getCurrentZone();
        String oldOwnerName = targetZone.getNameOwner();

        if (currentZone != targetZone) {
            NotificationController.show(
                "Movement Required",
                "You must be in the zone to attack. Distance to target: " + currentDifficulty + " steps.",
                Notification.Type.ERROR,
                5000
            );

            setPendingAction(PendingAction.TRAVEL);
            return;
        }

        if (current.getPseudo().equalsIgnoreCase(targetZone.getNameOwner())) {
            NotificationController.show(
                "Invalid Target",
                "You cannot attack your own territory. Please select a different action (Power Up or Travel).",
                Notification.Type.SUCCESS,
                8000
            );

            setPendingAction(PendingAction.NONE);
            return;
        }

        int result = targetZone.getPower() - currentDifficulty;

        if (result > 0) {
            targetZone.setPower(result);
            NotificationController.show(
                "Attack Progress",
                "Attack successful! The enemy is weakened (Power remaining: " + result + ")",
                Notification.Type.INFORMATION,
                5000
            );

        } else if(result == 0) {
            for(Player p : players ) {
                if(p.getPseudo().equalsIgnoreCase(oldOwnerName)) {
                    p.getZones().remove(targetZone);
                    break;
                }
            }

            NotificationController.show(
                "Zone Neutralized",
                "The defenses have fallen! The zone is now neutral.",
                Notification.Type.SUCCESS,
                5000
            );
            targetZone.setPower(0);
            targetZone.setNameOwner("Nobody");
            targetZone.setColor(targetZone.getBaseColor());

        } else {
            int finalPower = Math.abs(result);

            for(Player p : players ) {
                if(p.getPseudo().equalsIgnoreCase(oldOwnerName)) {
                    p.getZones().remove(targetZone);
                    break;
                }
            }


            targetZone.setPower(finalPower);
            targetZone.setNameOwner(current.getPseudo());

            if(current.hasAlly()) {
                targetZone.setColor(current.getCurrentAllianceColor());

            } else {
                targetZone.setColor(current.getColor().getJavafxColor());
            }

            current.addZone(targetZone);
            NotificationController.show(
                "Victory!",
                "You have captured " + targetZone.getName() + " with " + finalPower + " power!",
                Notification.Type.SUCCESS,
                6000
            );

            checkWinCondition();
        }

        setPendingAction(PendingAction.NONE);
        gameHUD.refreshGameInfo(players, currentPlayerIndex);
        gameHUD.refreshPlayerInfo(getCurrentPlayer());
    }


    public void applyPowerUp(Zone targetZone) {
        setTargetZone(targetZone);
        applyPowerUp();
    }

    public void applyPowerUp() {
        Player current = getCurrentPlayer();
        if (current == null || targetZone == null) {
            return;
        }

        if (!current.getPseudo().equalsIgnoreCase(targetZone.getNameOwner())) {
            NotificationController.show(
                "Action Denied",
                "You can only power up your own zones!",
                Notification.Type.ERROR,
                3000
            );
            return;
        }


        int currentPower = targetZone.getPower();
        int maxPower = targetZone.getMAX_POWER_ZONE();

        if (currentPower < maxPower) {
            int newPower = Math.min(currentPower + currentDifficulty, maxPower);
            targetZone.setPower(newPower);
            NotificationController.show(
                "Zone Upgraded",
                targetZone.getName() + " power increased to " + newPower + "!",
                Notification.Type.SUCCESS,
                3000
            );
            setPendingAction(PendingAction.NONE);
            gameHUD.refreshGameInfo(players, currentPlayerIndex);
            gameHUD.refreshPlayerInfo(getCurrentPlayer());

        } else {
            NotificationController.show(
                "Maximum Power",
                "This zone is already at maximum capacity.",
                Notification.Type.ERROR,
                3000
            );
        }
    }

    public void setCurrentPlayerIndexFromNetwork(int index) {
        if (index < 0 || index >= players.size()) {
            return;
        }

        currentPlayerIndex = index;
        hasAnsweredCorrectly = false;
        currentDifficulty = 0;
        selectedAction = PendingAction.NONE;
        clearTravelHint();

        Player nextP = getCurrentPlayer();
        NotificationController.show(
            "Next Turn",
            "It's now " + nextP.getPseudo() + "'s turn!",
            Notification.Type.INFORMATION,
            3000
        );

        if (nextP.getPendingAllianceRequest() != null) {
            showAllianceDecisionMenu(nextP);
        }

        gameHUD.refreshGameInfo(players, currentPlayerIndex);
        gameHUD.refreshPlayerInfo(getCurrentPlayer());
    }

    public void setPlayerCount(int count) {
        if (count > 0) {
            nbPlayer = count;
        }
    }

    public Zone findZoneByName(String zoneName) {
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

    private void checkWinCondition() {
        Player current = getCurrentPlayer();
        int totalZones;
        String winnerName;

        if (current.hasAlly()) {
            Player ally = current.getAlly();
            totalZones = current.getZones().size() + ally.getZones().size();
            System.out.println("total zone alliance :"+totalZones);
            winnerName = "The Alliance (" + current.getPseudo() + " & " + ally.getPseudo() + ")";

        } else {
            totalZones = current.getZones().size();
            winnerName = current.getPseudo();
            System.out.println("total zone solo :"+totalZones);
        }

        if (totalZones >= 21) {
            triggerEndGame(winnerName);
        }
    }

    private void triggerEndGame(String winnerName) {
        this.selectedAction = PendingAction.NONE;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/game/Victory.fxml"));
            StackPane endPage = loader.load();


            EndGame controller = loader.getController();
            if (controller != null) {
                controller.lblWinnerName.setText(winnerName.toUpperCase());
            }


            javafx.scene.layout.Pane rootNode = (javafx.scene.layout.Pane) mapView.getRoot().getScene().getRoot();


            rootNode.getChildren().add(endPage);
            endPage.prefWidthProperty().bind(rootNode.widthProperty());
            endPage.prefHeightProperty().bind(rootNode.heightProperty());

        } catch (IOException e) {
            System.err.println("Erreur de chargement du FXML : " + e.getMessage());
        }
    }


    public void nextPlayer() {
        currentPlayerIndex =(currentPlayerIndex +1) % nbPlayer;

        this.hasAnsweredCorrectly = false;
        this.currentDifficulty = 0;
        clearTravelHint();

        Player nextP = getCurrentPlayer();

        NotificationController.show(
            "Next Turn",
            "It's now " + nextP.getPseudo() + "'s turn!",
            Notification.Type.INFORMATION,
            3000
        );

        if (nextP.getPendingAllianceRequest() != null) {
            showAllianceDecisionMenu(nextP);
        }

        gameHUD.refreshGameInfo(players, currentPlayerIndex);
        gameHUD.refreshPlayerInfo(getCurrentPlayer());
    }

    private void showAllianceDecisionMenu(Player receiver) {
        Player requester = receiver.getPendingAllianceRequest();
        NotificationController.show(
            "Alliance Request",
            requester.getPseudo() + " wants to form an alliance with you!",
            Notification.Type.INFORMATION,
            7000
        );
    }

    public Player getCurrentPlayer() {
        if(players.isEmpty()) {
            return null;
        }

        return players.get(currentPlayerIndex);
    }


    public void setPendingAction(PendingAction action) {
        this.selectedAction = action;
        updateActionHint(action);
    }

    public PendingAction getPendingAction() {
        return selectedAction;
    }

    public boolean canSelectZone(Zone targetZone) {
        if (targetZone == null) {
            return false;
        }

        Player current = getCurrentPlayer();
        if (current == null) {
            return false;
        }

        Zone currentZone = current.getCurrentZone();
        return switch (selectedAction) {
            case TRAVEL -> isTravelTargetValid(currentZone, targetZone);
            case ATTACK -> isAttackTargetValid(current, currentZone, targetZone);
            case POWER_UP -> isPowerUpTargetValid(current, targetZone);
            case NONE -> false;
        };
    }

    private void updateActionHint(PendingAction action) {
        clearTravelHint();

        if (action == PendingAction.NONE) {
            return;
        }

        Player current = getCurrentPlayer();
        if (current == null || current.getCurrentZone() == null || worldMap == null) {
            return;
        }

        Set<Zone> selectableZones = new HashSet<>();
        switch (action) {
            case ATTACK -> {
                if (!current.getCurrentZone().getNameOwner().equalsIgnoreCase(current.getPseudo())) {
                    selectableZones.add(current.getCurrentZone());
                }
            }
            case POWER_UP -> selectableZones.addAll(current.getZones());
            case TRAVEL -> {
                Zone currentZone = current.getCurrentZone();
                Set<Zone> inRange = ZonePathfinder.getZonesWithinRange(currentZone, TRAVEL_HINT_MAX_DISTANCE);
                inRange.remove(currentZone);
                selectableZones.addAll(inRange);
            }
        }

        for (Zone zone : worldMap.getAllZones()) {
            ZoneView zoneView = mapView.getViewFor(zone);
            if (zoneView == null) continue;

            if (selectableZones.contains(zone)) {
                continue;
            }

            zoneView.setDimmed(true);
            zoneView.setCursor(Cursor.DEFAULT);
            travelHintZones.add(zone);
        }
    }


    private boolean isTravelTargetValid(Zone currentZone, Zone targetZone) {
        if (currentZone == null || TRAVEL_HINT_MAX_DISTANCE <= 0 || targetZone == currentZone) {
            return false;
        }

        int distance = ZonePathfinder.getShortestDistance(currentZone, targetZone, TRAVEL_HINT_MAX_DISTANCE);
        return distance > 0 && distance <= TRAVEL_HINT_MAX_DISTANCE;
    }

    private boolean isAttackTargetValid(Player current, Zone currentZone, Zone targetZone) {
        return currentZone != null
            && currentZone == targetZone
            && !current.getPseudo().equalsIgnoreCase(targetZone.getNameOwner());
    }

    private boolean isPowerUpTargetValid(Player current, Zone targetZone) {
        return current.getPseudo().equalsIgnoreCase(targetZone.getNameOwner());
    }

    private void clearTravelHint() {
        if (travelHintZones.isEmpty()) return;

        for (Zone zone : travelHintZones) {
            ZoneView zoneView = mapView.getViewFor(zone);
            if (zoneView != null) {
                zoneView.setDimmed(false);
            }
        }
        travelHintZones.clear();
    }

    public int getCurrentDistance() {
        return currentDistance;
    }

    public PendingAction getSelectedAction() {
        return selectedAction;
    }

    public static GameController getInstance() {
        return instance;
    }

    public boolean isHasAnsweredCorrectly() {
        return hasAnsweredCorrectly;
    }

    public Color getNextAllianceColor() {
        allianceCount++;
        if (allianceCount == 1) return ALLIANCE_1_COLOR;
        return ALLIANCE_2_COLOR;
    }

    public void setTargetZone(Zone targetZone) {
        this.targetZone = targetZone;
    }
}
