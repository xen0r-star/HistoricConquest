package com.historicconquest.historicconquest.controller.game;

import com.historicconquest.historicconquest.model.game.Game;
import com.historicconquest.historicconquest.model.map.WorldMap;
import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.network.model.RoomPlayer;
import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.model.player.PlayerColor;
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
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public final class GameBootstrapper {
    private static final Logger logger = LoggerFactory.getLogger(GameBootstrapper.class);

    private GameBootstrapper() {
    }

    public static void launchGame(StackPane root, List<Player> players) {
        launchGame(root, players, null, null);
    }

    public static void launchGame(StackPane root, List<RoomPlayer> roomPlayers, Map<String, String> selectedZonesByPlayerId) {
        if (roomPlayers == null || roomPlayers.isEmpty()) {
            return;
        }

        List<Player> players = new ArrayList<>();
        for (int i = 0; i < roomPlayers.size(); i++) {
            RoomPlayer roomPlayer = roomPlayers.get(i);
            PlayerColor color = parsePlayerColor(roomPlayer.getColor());
            if (color == null) {
                color = PlayerColor.RED;
            }

            players.add(new Player(i, roomPlayer.getName(), color));
        }

        launchGame(root, players, roomPlayers, selectedZonesByPlayerId);
    }

    private static void launchGame(StackPane root, List<Player> players, List<RoomPlayer> roomPlayers, Map<String, String> selectedZonesByPlayerId) {
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
            Map<String, Zone> zonesByName = allZones.stream()
                .collect(Collectors.toMap(Zone::getName, zone -> zone, (left, right) -> left, HashMap::new));

            List<Player> playersSnapshot = new ArrayList<>(players);
            for (int i = 0; i < playersSnapshot.size() && i * 10 < allZones.size(); i++) {
                Player player = playersSnapshot.get(i);
                Zone startZone = null;

                if (roomPlayers != null && selectedZonesByPlayerId != null && i < roomPlayers.size()) {
                    String selectedZoneName = selectedZonesByPlayerId.get(roomPlayers.get(i).getId());
                    if (selectedZoneName != null) {
                        startZone = zonesByName.get(selectedZoneName);
                    }
                }

                if (startZone == null) {
                    startZone = allZones.get(i * 10);
                }

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

    private static PlayerColor parsePlayerColor(String rawColor) {
        if (rawColor == null || rawColor.isBlank()) {
            return null;
        }

        try {
            return PlayerColor.valueOf(rawColor.trim().toUpperCase());

        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}


