package com.historicconquest.historicconquest.controller.game;

import com.historicconquest.historicconquest.model.game.GameAnimationPort;
import com.historicconquest.historicconquest.model.map.WorldMap;
import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.map.ZonePathfinder;
import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.view.map.MapView;
import com.historicconquest.historicconquest.view.map.ZoneView;
import javafx.animation.PathTransition;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

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
    public ZonePathfinder zonePathfinder;

    private PendingAction selectedAction = PendingAction.NONE ;

    public enum PendingAction{
        NONE , TRAVEL ,ATTACK ,POWER_UP
    }


    public void initializeGameState(List<Player> playersData , WorldMap worldmap ,MapView mapView , Group mapInterface)
    {
        List<Zone> allZones = worldmap.getAllZones();

        for (int i = 0; i < playersData.size(); i++) {
            Player player = playersData.get(i);

            boolean added = this.addPlayer(player);
            if (added) {
                System.out.println("Joueur " + player.getPseudo() + " ajouté au controller." + player.getColor());
            }

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


    /*la logique du jeu ici */

    public void handleQuestionResult(int level , boolean correct)
    {
        this.currentDifficulty = level ;
        this.hasAnsweredCorrectly= correct ;
        Player current = players.get(currentPlayerIndex);

        if(!correct)
        {
            System.out.println("Echec ! tour fini pour le joueur  "+ players.get(currentPlayerIndex).getPseudo());

            current.setConsecutiveFailures(current.getConsecutiveFailures()+1);
            current.setConsecutiveSuccesses(0);
            ZonePathfinder.findPath(current.getCurrentZone() , current.getCurrentZone());

            if(current.getConsecutiveFailures()>=3)
            {
                System.out.println("Malus : 3 echecs consecutives ");
                //TODO : ici faire les malus
                current.setConsecutiveFailures(3);
            }
            nextPlayer();
        }
        else
        {
            System.out.println("Succes ! choisissez une zone pour appliquer vos "+ level + " points pour le joueur" + players.get(currentPlayerIndex).getPseudo());
            current.setConsecutiveSuccesses(current.getConsecutiveSuccesses()+1);
            current.setConsecutiveFailures(0);
            ZonePathfinder.setMaxPathLength(level+1);
            if(current.getConsecutiveSuccesses()>=3)
            {
                System.out.println("Bonus : 3 éme succes consécutifs !");
                //TODO : ici faire les bonus
                current.setConsecutiveSuccesses(0);
            }

        }

    }


    public void travel(Zone targetZone)
    {
        Player current = getCurrentPlayer() ;

        ZonePathfinder.PathResult result = ZonePathfinder.findPath(current.getCurrentZone() , targetZone);

        if (result.type() == ZonePathfinder.PathType.DIRECT)
        {
            if((result.zones().size() -1)<= currentDifficulty)
            {
                this.selectedAction = PendingAction.NONE ;

                animatePawnMove(
                        current.getPawnNode(),
                        result.zones(),
                        this::nextPlayer
                );

                current.setCurrentZone(targetZone);
            }
        }

    }


    public void attackZone(Zone targetZone) {
        Player current = getCurrentPlayer();
        Zone currentZone = current.getCurrentZone();

        if (currentZone != targetZone) {
            System.out.println("Vous devez être sur la zone pour l'attaquer.");
            return;
        }

        if (current.getPseudo().equalsIgnoreCase(targetZone.getNameOwner())) {
            System.out.println("C'est déjà votre zone.");
            return;
        }

        int result = targetZone.getPower() - currentDifficulty;

        if (result > 0) {
            targetZone.setPower(result);
            System.out.println("Attaque réussie, mais la zone résiste encore (Power: " + result + ")");
        }
        else if(result == 0)
        {
            System.out.println("La zone tombe à zéro.");
            targetZone.setPower(0);
            targetZone.setNameOwner("Nobody");
            targetZone.setColor(targetZone.getBaseColor());
        }
        else {
            System.out.println("Zone capturée !");
            int finalPower = Math.abs(result);

            targetZone.setPower(finalPower);
            targetZone.setNameOwner(current.getPseudo());
            targetZone.setColor(current.getColor().getJavafxColor());
            current.addZone(targetZone);
        }

        // 4. Fin du tour
        this.selectedAction = PendingAction.NONE;
        nextPlayer();
    }


    public void powerUp(Zone targetZone) {
        Player current = getCurrentPlayer();

        if (!current.getPseudo().equalsIgnoreCase(targetZone.getNameOwner())) {
            System.out.println("Cliquez sur une zone qui vous appartient !");
            return;
        }


        int currentPower = targetZone.getPower();
        int maxPower = targetZone.getMAX_POWER_ZONE();

        if (currentPower < maxPower) {
            int newPower = Math.min(currentPower + currentDifficulty, maxPower);
            targetZone.setPower(newPower);

            this.selectedAction = PendingAction.NONE;
            nextPlayer();
        } else {
            System.out.println("Cette zone est déjà au maximum de sa puissance.");
        }
    }


    public void nextPlayer()
    {
        currentPlayerIndex =(currentPlayerIndex +1) % nbPlayer ;

        this.hasAnsweredCorrectly = false ;
        this.currentDifficulty =0 ;
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
}

