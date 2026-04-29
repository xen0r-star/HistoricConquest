package com.historicconquest.historicconquest.model.game;

import com.historicconquest.historicconquest.controller.game.GameController;
import com.historicconquest.historicconquest.controller.game.ZoneInfoPanel;
import com.historicconquest.historicconquest.controller.overlay.Notification;
import com.historicconquest.historicconquest.controller.overlay.NotificationController;
import com.historicconquest.historicconquest.model.map.WorldMap;
import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.map.ZonePathfinder;
import com.historicconquest.historicconquest.model.player.Player;

import java.util.List;

public class Game {
    private List<Player> players ;
    private GameAnimationPort gameAnimationPort;
    private int currentPlayerIndex = 0 ;
    private WorldMap worldMap ;
    private GameController gameController ;
    private ZoneInfoPanel zoneInfoPanel ;


    public Game(List<Player> players , WorldMap worldMap , GameAnimationPort gameAnimationPort, ZoneInfoPanel zoneInfoPanel) {
        this.players = players ;
        this.worldMap = worldMap;
        this.gameAnimationPort = gameAnimationPort;
        this.gameController = GameController.getInstance();
        this.zoneInfoPanel = zoneInfoPanel ;
    }


    public void handleZoneSelection(Zone targetZone) {

        if (!gameController.isHasAnsweredCorrectly()) {
           gameController.showZoneInfo(targetZone);
            return;
        }

        switch(gameController.getSelectedAction())
        {
            case TRAVEL :
                gameController.travel(targetZone);
                break ;
            case ATTACK:
                gameController.attackZone(targetZone);
                break ;
            case POWER_UP:
                gameController.powerUp(targetZone);
                break ;
            case NONE:
            default:
                gameController.showZoneInfo(targetZone);
                break ;
        }
    }


    public Player getCurrentPlayer() {
        return gameController.getCurrentPlayer();
    }
}