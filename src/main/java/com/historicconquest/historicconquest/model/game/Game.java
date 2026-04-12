package com.historicconquest.historicconquest.model.game;

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


    public Game(List<Player> players , WorldMap worldMap , GameAnimationPort gameAnimationPort) {
        this.players = players ;
        this.worldMap = worldMap;
        this.gameAnimationPort = gameAnimationPort;
    }


    public void handleZoneSelection(Zone targetZone) {
        Player current = players.get(currentPlayerIndex);

        ZonePathfinder.PathResult result = ZonePathfinder.findPath(current.getCurrentZone(), targetZone);

        if (result.type() == ZonePathfinder.PathType.DIRECT) {
            current.setCurrentZone(targetZone);

            if (current.getPawnNode() == null) return;

            gameAnimationPort.animatePawnMove(
                current.getPawnNode(),
                result.zones(),
                this::nextTurn
            );
        }
    }

    private void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex +1 )% players.size();
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }
}