package com.historicconquest.historicconquest.controller;

import com.historicconquest.historicconquest.map.Map;
import com.historicconquest.historicconquest.map.Zone;
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

    public GameController(Map map, ZoneInfoPanel zoneInfoPanel, GameHUD gameHUD, Group mapInterface) {
        this.map = map;
        this.zoneInfoPanel = zoneInfoPanel;
        this.gameHUD = gameHUD;
        this.mapInterface = mapInterface;
        this.selectedZone = null;

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
        if (selectedZone == zone) {
            deselectZone();

        } else {
            if (selectedZone != null) {
                deselectZone();
            }
            selectZone(zone);
        }
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


