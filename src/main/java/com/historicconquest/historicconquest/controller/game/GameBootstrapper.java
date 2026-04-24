package com.historicconquest.historicconquest.controller.game;

import com.historicconquest.historicconquest.model.game.Game;
import com.historicconquest.historicconquest.model.map.WorldMap;
import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.service.map.MapNavigationService;
import com.historicconquest.historicconquest.view.map.MapView;
import com.historicconquest.historicconquest.view.map.MapViewFactory;
import com.historicconquest.historicconquest.view.map.ZoneView;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class GameBootstrapper {
    private static final Logger logger = LoggerFactory.getLogger(GameBootstrapper.class);

    private GameBootstrapper() {
    }

    public static void launchGame(StackPane root, List<Player> players) {
        if (root == null || players == null || players.isEmpty()) {
            return;
        }

        try {
            root.getChildren().clear();

            WorldMap worldMap = new WorldMap(true, true, true, Color.web("#f2e1bf"), Color.web("#C5A682"));
            MapView mapView = MapViewFactory.build(worldMap, true);
            Group mapInterface = mapView.getRoot();

            FXMLLoader hudLoader = new FXMLLoader(GameBootstrapper.class.getResource("/view/fxml/GameHUD.fxml"));
            Parent hudVisual = hudLoader.load();
            GameHUD gameHUD = hudLoader.getController();
            hudVisual.setPickOnBounds(false);
            root.getChildren().add(hudVisual);

            FXMLLoader infoLoader = new FXMLLoader(GameBootstrapper.class.getResource("/view/fxml/zoneInfoPanel.fxml"));
            Parent infoVisual = infoLoader.load();
            ZoneInfoPanel zoneInfoPanel = infoLoader.getController();
            infoVisual.setPickOnBounds(false);
            root.getChildren().add(infoVisual);
            zoneInfoPanel.hide();

            GameController gameController = new GameController(zoneInfoPanel, gameHUD, mapView);

            MapNavigationService mapNavigationService = new MapNavigationService();
            mapNavigationService.attachNavigation(root, mapInterface);

            List<Zone> allZones = worldMap.getAllZones();
            List<Player> playersSnapshot = new ArrayList<>(players);
            for (int i = 0; i < playersSnapshot.size() && i * 10 < allZones.size(); i++) {
                Player player = playersSnapshot.get(i);
                Zone startZone = allZones.get(i * 10);
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

            Game gameEngine = new Game(playersSnapshot, worldMap, gameController);
            worldMap.getAllZones().forEach(zone -> {
                ZoneView zoneView = mapView.getViewFor(zone);
                if (zoneView == null) return;

                zoneView.setPickOnBounds(true);
                zoneView.setOnMouseClicked(event -> {
                    gameEngine.handleZoneSelection(zone);
                    event.consume();
                });
            });

        } catch (Exception exception) {
            logger.error("Error launching game", exception);
        }
    }
}


