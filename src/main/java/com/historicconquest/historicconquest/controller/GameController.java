package com.historicconquest.historicconquest.controller;

import com.historicconquest.historicconquest.map.Map;
import com.historicconquest.historicconquest.map.Zone;
import com.historicconquest.historicconquest.map.ZonePathfinder;
import com.historicconquest.historicconquest.ui.GameHUD;
import com.historicconquest.historicconquest.ui.ZoneInfoPanel;
import javafx.scene.Group;
import javafx.scene.paint.Color;

public class GameController {
    private final Map map;
    private final ZoneInfoPanel zoneInfoPanel;
    private final GameHUD gameHUD;
    private final Group mapInterface;

    private Zone selectedZone;
    private Zone startZoneForPath;  // Zone de départ pour le pathfinding

    public GameController(Map map, ZoneInfoPanel zoneInfoPanel, GameHUD gameHUD, Group mapInterface) {
        this.map = map;
        this.zoneInfoPanel = zoneInfoPanel;
        this.gameHUD = gameHUD;
        this.mapInterface = mapInterface;
        this.selectedZone = null;
        this.startZoneForPath = null;

        initialize();
    }

    private void initialize() {
        mapInterface.getChildren().addAll(map.getBlocs());
        gameHUD.initializeMap(mapInterface);

        map.getBlocs().forEach(bloc ->
            bloc.getZones().forEach(zone ->
                zone.setOnMouseClicked(event -> handleZoneClick(zone))
            )
        );

        zoneInfoPanel.hide();
    }



    private void handleZoneClick(Zone zone) {
        // TESTE D'AFFICHAGE DU PATHFINDING -----------------------------------------------------------
        if (startZoneForPath == null) {
            startZoneForPath = zone;
            System.out.println("\n🎯 [CLIC 1] Zone de départ sélectionnée: " + zone.getName());
            selectZone(zone);
            return;
        }

        if (startZoneForPath == zone) {
            deselectZone();
            startZoneForPath = null;
            System.out.println("❌ Clic annulé - même zone");
            return;
        }

        System.out.println("🎯 [CLIC 2] Zone d'arrivée sélectionnée: " + zone.getName());

        var pathResult = map.findPath(startZoneForPath, zone);

        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║     RÉSULTAT DU PATHFINDING    ║");
        System.out.println("╠════════════════════════════════╣");
        System.out.println("║ De: " + String.format("%-27s", startZoneForPath.getName()) + "║");
        System.out.println("║ À:  " + String.format("%-27s", zone.getName()) + "║");
        System.out.println("║ Type: " + String.format("%-25s", pathResult.type()) + "║");

        if (pathResult.zones() != null) {
            System.out.println("║ Chemin (" + pathResult.zones().size() + " zones):              ║");
            for (int i = 0; i < pathResult.zones().size(); i++) {
                String zoneName = (i + 1) + ". " + pathResult.zones().get(i).getName();
                System.out.println("║   " + String.format("%-26s", zoneName) + "║");
            }
            System.out.println("╠════════════════════════════════╣");
            if (pathResult.type() == ZonePathfinder.PathType.DIRECT) {
                System.out.println("║ ✓ Trajet possible!                 ║");
            } else {
                System.out.println("║ ✗ Trajet trop long (> 4 zones)     ║");
            }
        } else {
            System.out.println("║ Aucun chemin trouvé!              ║");
        }
        System.out.println("╚════════════════════════════════╝\n");

        // --------------------------------------------------------------------------------------------

        deselectZone();
        startZoneForPath = null;
    }



    private void selectZone(Zone zone) {
        selectedZone = zone;
        zone.setFocusedZone(true);
        zone.updateColor(Color.web("#D4AF37"));

        zoneInfoPanel.setData(zone.getName());
        zoneInfoPanel.show();
    }

    private void deselectZone() {
        if (selectedZone != null) {
            selectedZone.setFocusedZone(false);
            selectedZone.updateColor(selectedZone.getColor());
            selectedZone = null;
        }

        zoneInfoPanel.hide();
    }
}


