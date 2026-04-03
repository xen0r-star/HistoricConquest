package com.historicconquest.historicconquest.model.game;

import com.historicconquest.historicconquest.controller.GameController;
import com.historicconquest.historicconquest.model.map.WorldMap;
import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.map.ZonePathfinder;
import com.historicconquest.historicconquest.model.player.Player;

import java.util.List;

public class Game {
    private List<Player> players ;
    private GameController gameController ;
    private int currentPlayerIndex = 0 ;
    private WorldMap worldMap ;


    public Game(List<Player> players , WorldMap worldMap , GameController gameController) {
        this.players = players ;
        this.worldMap = worldMap;
        this.gameController = gameController ;
    }


    public void handleZoneSelection(Zone targetZone) {
        Player current = players.get(currentPlayerIndex);

        // DIAGNOSTIC 1 : Est-ce qu'on récupère bien le joueur et sa zone ?
        System.out.println("Joueur: " + current.getPseudo() + " tente d'aller vers: " + targetZone.getName());
        System.out.println("Position actuelle: " + (current.getCurrentZone() != null ? current.getCurrentZone().getName() : "NULL"));

        ZonePathfinder.PathResult result = ZonePathfinder.findPath(current.getCurrentZone(), targetZone);

        // DIAGNOSTIC 2 : Quel est le type de chemin trouvé ?
        System.out.println("Résultat du Pathfinder: " + result.type());

        if (result.type() == ZonePathfinder.PathType.DIRECT) {
            // DIAGNOSTIC 3 : Le chemin est validé
            System.out.println("Mouvement validé ! Lancement de l'animation...");

            current.setCurrentZone(targetZone);

            // Vérification du Node (l'image)
            if (current.getPawnNode() == null) {
                System.err.println("ERREUR : Le pion (Node) du joueur est NULL !");
                return;
            }

            gameController.animatePawnMove(current.getPawnNode(), result.zones(), () -> {
                System.out.println("Animation finie, changement de tour.");
                nextTurn();
            });

        } else {
            // DIAGNOSTIC 4 : Pourquoi ça a échoué ?
            System.out.println("Mouvement refusé : La zone est trop loin ou invalide.");
        }
    }

    private void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex +1 )% players.size();
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }
}