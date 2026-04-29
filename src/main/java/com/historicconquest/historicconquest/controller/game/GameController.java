package com.historicconquest.historicconquest.controller.game;

import com.historicconquest.historicconquest.controller.overlay.Notification;
import com.historicconquest.historicconquest.controller.overlay.NotificationController;
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
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class GameController implements GameAnimationPort {
    private final ZoneInfoPanel zoneInfoPanel;
    private final MapView mapView;
    private int currentPlayerIndex = 0 ;
    private int currentDifficulty = 0 ;
    private boolean hasAnsweredCorrectly ;
    private static GameController instance ;
    private int nbPlayer = 4 ;
    private List<Player> players ;
    public static final Color ALLIANCE_1_COLOR = Color.web("#F2F2F2");
    public static final Color ALLIANCE_2_COLOR = Color.web("#A9A9A9");

    private int allianceCount = 0;

    private PendingAction selectedAction = PendingAction.NONE ;

    public enum PendingAction{
        NONE , TRAVEL ,ATTACK ,POWER_UP
    }


    public List<Player> getPlayers() {
        return players;
    }

    public void initializeGameState(List<Player> playersData , WorldMap worldmap , MapView mapView , Group mapInterface)
    {
        List<Zone> allZones = worldmap.getAllZones();

        for (int i = 0; i < playersData.size(); i++) {
            Player player = playersData.get(i);

            boolean added = this.addPlayer(player);

            Zone startZone = allZones.get(i * 10);

            //startZone.setPlayer(player);

            startZone.setColor(player.getColor().getJavafxColor());
            startZone.setPower(4);
            startZone.setNameOwner(player.getPseudo());

            player.addZone(startZone);


            ZoneView startZoneView = mapView.getViewFor(startZone);
            if (startZoneView == null) continue;

            Group pawnGroup = com.historicconquest.historicconquest.controller.game.PawnController.createPawn(player.getColor(), 40.0);
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

    }


    public GameController(ZoneInfoPanel zoneInfoPanel, GameHUD gameHUD, MapView mapView) {
        this.zoneInfoPanel = zoneInfoPanel;
        this.mapView = mapView;

        gameHUD.initializeMap(mapView.getRoot());
        this.zoneInfoPanel.hide();

        instance = this ;

        players = new ArrayList<>();

    }


    public boolean addPlayer(Player player)
    {
        if(players.contains(player))
        {
            return false ;
        }
        return players.add(player);
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
        zoneInfoPanel.setData(zone.getName());
        zoneInfoPanel.setThemeLabel(zone.getThemes().getLabel());
        zoneInfoPanel.setBlockLabel(zone.getBlocName());
        zoneInfoPanel.setPowerLabel(zone.getPowertext());
        zoneInfoPanel.setOwnerLabel(zone.getNameOwner());

        zoneInfoPanel.show();
    }


    public void handleQuestionResult(int level , boolean correct)
    {
        this.currentDifficulty = level ;
        this.hasAnsweredCorrectly= correct ;
        Player current = players.get(currentPlayerIndex);

        if(!correct)
        {
            NotificationController.show(
                    "Wrong Answer",
                    "Incorrect! Your turn is over.",
                    Notification.Type.ERROR,
                    5000
            );

            current.setConsecutiveFailures(current.getConsecutiveFailures()+1);
            current.setConsecutiveSuccesses(0);
            ZonePathfinder.findPath(current.getCurrentZone() , current.getCurrentZone());

            if(current.getConsecutiveFailures()>=3)
            {
                NotificationController.show(
                        "Critical Failure",
                        "3 consecutive incorrect answers! You have received a penalty.",
                        Notification.Type.ERROR,
                        6000
                );
                current.setConsecutiveFailures(3);
            }
            nextPlayer();
        }
        else
        {
            current.setConsecutiveSuccesses(current.getConsecutiveSuccesses()+1);
            current.setConsecutiveFailures(0);
            ZonePathfinder.setMaxPathLength(level+1);
            if(current.getConsecutiveSuccesses()>=3)
            {
                NotificationController.show(
                        "Win Streak!",
                        "3 correct answers in a row! You've earned a special bonus.",
                        Notification.Type.SUCCESS,
                        6000
                );
                current.setConsecutiveSuccesses(0);
            }

        }

    }


    public void travel(Zone targetZone) {
        Player current = getCurrentPlayer();

        ZonePathfinder.PathResult result = ZonePathfinder.findPath(current.getCurrentZone(), targetZone);

        if (result.type() == ZonePathfinder.PathType.DIRECT) {
            int distance = result.zones().size() - 1;

            if (distance <= currentDifficulty) {
                NotificationController.show(
                        "Traveling",
                        "Moving to " + targetZone.getName() + " (" + distance + " zones).",
                        Notification.Type.INFORMATION,
                        3000
                );

                this.selectedAction = PendingAction.NONE;

                animatePawnMove(
                        current.getPawnNode(),
                        result.zones(),
                        this::nextPlayer
                );

                current.setCurrentZone(targetZone);
            }
            else {
                NotificationController.show(
                        "Too Far",
                        "Target is " + distance + " zones away, but your answer only allows " + currentDifficulty + " steps!",
                        Notification.Type.ERROR,
                        6000
                );
            }
        }
    }


    public int getCurrentDifficulty() {
        return currentDifficulty;
    }

    public void attackZone(Zone targetZone) {
        Player current = getCurrentPlayer();
        Zone currentZone = current.getCurrentZone();
        String oldOwnername = targetZone.getNameOwner();

        if (currentZone != targetZone) {
            NotificationController.show(
                    "Movement Required",
                    "You must be in the zone to attack. Distance to target: " + currentDifficulty + " steps.",
                    Notification.Type.ERROR, // Changé de SUCCESS à WARNING
                    5000
            );
            this.selectedAction = PendingAction.TRAVEL ;
            return;
        }

        if (current.getPseudo().equalsIgnoreCase(targetZone.getNameOwner())) {
            NotificationController.show(
                    "Invalid Target",
                    "You cannot attack your own territory. Please select a different action (Power Up or Travel).",
                    Notification.Type.SUCCESS,
                    8000
            );
            this.selectedAction = PendingAction.NONE ;
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
        }
        else if(result == 0)
        {


            for(Player p : players )
            {
                if(p.getPseudo().equalsIgnoreCase(oldOwnername))
                {
                    p.getZones().remove(targetZone);
                    break ;
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
        }
        else {
            int finalPower = Math.abs(result);


            for(Player p : players )
            {
                if(p.getPseudo().equalsIgnoreCase(oldOwnername))
                {
                    p.getZones().remove(targetZone);
                    break ;
                }
            }


            targetZone.setPower(finalPower);
            targetZone.setNameOwner(current.getPseudo());

            if(current.hasAlly())
            {
                targetZone.setColor(current.getCurrentAllianceColor());
            }
            else
            {
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

        // 4. Fin du tour
        this.selectedAction = PendingAction.NONE;
        nextPlayer();
    }

    private void checkWinCondition() {
        Player current = getCurrentPlayer();
        int totalZones = 0;
        String winnerName = "";

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

        if (totalZones >= 21)
        {
            triggerEndGame(winnerName);
        }
    }

    private void triggerEndGame(String winnerName) {
        this.selectedAction = PendingAction.NONE;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/victory.fxml"));
            StackPane endPage = loader.load();


            endGame controller = loader.getController();
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


    public void powerUp(Zone targetZone) {
        Player current = getCurrentPlayer();

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
            this.selectedAction = PendingAction.NONE;
            nextPlayer();
        } else {
            NotificationController.show(
                    "Maximum Power",
                    "This zone is already at maximum capacity.",
                    Notification.Type.ERROR,
                    3000
            );
        }
    }


    public void nextPlayer()
    {
        currentPlayerIndex =(currentPlayerIndex +1) % nbPlayer ;

        this.hasAnsweredCorrectly = false ;
        this.currentDifficulty =0 ;

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

    public Player getCurrentPlayer()
    {
        if(players.isEmpty())
        {
            return null ;
        }

        return players.get(currentPlayerIndex);
    }


    public void setPendingAction(PendingAction action)
    {
        this.selectedAction = action ;
    }


    public PendingAction getSelectedAction()
    {
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

}

