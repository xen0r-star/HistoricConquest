package com.historicconquest.historicconquest.controller.game;

import com.historicconquest.historicconquest.controller.page.QuestionController;
import com.historicconquest.historicconquest.controller.page.game.GameHUD;
import com.historicconquest.historicconquest.controller.page.game.ZoneInfoPanel;
import com.historicconquest.historicconquest.model.game.Game;
import com.historicconquest.historicconquest.model.map.WorldMap;
import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.network.model.RoomPlayer;
import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.model.player.PlayerColor;
import com.historicconquest.historicconquest.model.questions.Theme;
import com.historicconquest.historicconquest.model.questions.TypeThemes;
import com.historicconquest.historicconquest.service.map.MapNavigationService;
import com.historicconquest.historicconquest.view.map.MapView;
import com.historicconquest.historicconquest.view.map.MapViewFactory;
import com.historicconquest.historicconquest.view.map.ZoneView;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class GameBootstrapper {
    private static final Logger logger = LoggerFactory.getLogger(GameBootstrapper.class);

    private GameBootstrapper() {  }

    private record GameUIContext(
        WorldMap worldMap, MapView mapView,
        Group mapInterface, GameController gameController,
        ZoneInfoPanel zoneInfoPanel, GameHUD gameHUD
    ) { }




    public static void launchMultiGame(StackPane root, List<RoomPlayer> roomPlayers, Map<String, String> selectedZonesByPlayerId) {
        if (roomPlayers == null || roomPlayers.isEmpty()) return;

        List<Player> players = new ArrayList<>();
        for (int i = 0; i < roomPlayers.size(); i++) {
            RoomPlayer roomPlayer = roomPlayers.get(i);
            PlayerColor color = parsePlayerColor(roomPlayer.getColor());

            if (color == null) color = PlayerColor.RED;
            players.add(new Player(i, roomPlayer.getName(), color));
        }

        if (root == null || players.isEmpty()) return;

        try {
            GameUIContext ctx = setupGameUI(root);
            List<Player> playersSnapshot = new ArrayList<>(players);
            List<Zone> preferredStartZones = buildPreferredStartZones(playersSnapshot, roomPlayers, selectedZonesByPlayerId, ctx.worldMap.getAllZones());
            ctx.gameController.initializeGameState(playersSnapshot, ctx.worldMap, ctx.mapView, ctx.mapInterface, preferredStartZones);

            MultiplayerGameOverlay.attach(ctx.gameController, ctx.worldMap);
            GameNetworkService.attach(ctx.gameController, roomPlayers);
            QuestionController.setThemes(Theme.loadThemesFromResource("/datas/Questions.json"));
            Game gameEngine = Game.init(true);

            setupZoneEvent(ctx, gameEngine);

        } catch (Exception exception) {
            logger.error("Error launching game", exception);
        }
    }

    public static void launchSoloGame(StackPane root, List<Player> players) {
        launchSoloGame(root, players, (List<String>) null);
    }

    public static void launchSoloGame(StackPane root, List<Player> players, List<String> preferredStartZoneNames) {
        if (root == null || players == null || players.isEmpty()) return;

        try {
            GameUIContext ctx = setupGameUI(root);
            List<Zone> resolvedStartZones = buildPreferredStartZones(preferredStartZoneNames, ctx.worldMap.getAllZones());
            launchSoloGameInternal(ctx, players, resolvedStartZones);

        } catch (Exception exception) {
            logger.error("Error launching solo game", exception);
        }
    }

    private static void launchSoloGameInternal(GameUIContext ctx, List<Player> players, List<Zone> preferredStartZones) {
        ctx.gameController.initializeGameState(players, ctx.worldMap, ctx.mapView, ctx.mapInterface, preferredStartZones);
        QuestionController.setThemes(Theme.loadThemesFromResource("/datas/Questions.json"));
        Game gameEngine = Game.init(false);

        ctx.worldMap.getAllZones().forEach(zone ->
            zone.setThemes(TypeThemes.getRandom())
        );
        setupZoneEvent(ctx, gameEngine);
    }


    private static GameUIContext setupGameUI(StackPane root) throws Exception {
        root.getChildren().clear();
        WorldMap worldMap = new WorldMap(true, true, true, Color.web("#f2e1bf"), Color.web("#C5A682"));
        MapView mapView = MapViewFactory.build(worldMap, true);
        Group mapInterface = mapView.getRoot();

        FXMLLoader hudLoader = new FXMLLoader(GameBootstrapper.class.getResource("/view/fxml/game/GameHUD.fxml"));
        Parent hudVisual = hudLoader.load();
        GameHUD gameHUD = hudLoader.getController();
        hudVisual.setPickOnBounds(false);
        root.getChildren().add(hudVisual);

        FXMLLoader infoLoader = new FXMLLoader(GameBootstrapper.class.getResource("/view/fxml/game/ZoneInfoPanel.fxml"));
        Parent infoVisual = infoLoader.load();
        ZoneInfoPanel zoneInfoPanel = infoLoader.getController();
        ZoneInfoPanel.setInstance(zoneInfoPanel);
        infoVisual.setPickOnBounds(false);
        root.getChildren().add(infoVisual);
        StackPane.setAlignment(infoVisual, Pos.TOP_CENTER);
        StackPane.setMargin(infoVisual, new Insets(45, 45, 45, 45));
        zoneInfoPanel.hide();

        GameController gameController = new GameController(zoneInfoPanel, gameHUD, mapView);
        MapNavigationService mapNavigationService = new MapNavigationService();
        mapNavigationService.attachNavigation(root, mapInterface);

        return new GameUIContext(worldMap, mapView, mapInterface, gameController, zoneInfoPanel, gameHUD);
    }

    private static void setupZoneEvent(GameUIContext ctx, Game gameEngine) {
        ctx.worldMap.getAllZones().forEach(zone -> {
            ZoneView zoneView = ctx.mapView.getViewFor(zone);
            if (zoneView == null) return;
            zoneView.setPickOnBounds(false);
            zoneView.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> ctx.gameController.showZoneInfo(zone));
            zoneView.addEventHandler(MouseEvent.MOUSE_MOVED, event -> ctx.gameController.showZoneInfo(zone));
            zoneView.addEventHandler(MouseEvent.MOUSE_EXITED, event -> ctx.zoneInfoPanel.hide());
            zoneView.setOnMouseClicked(event -> {
                gameEngine.handleZoneSelection(zone);
                event.consume();
            });
        });
    }


    private static PlayerColor parsePlayerColor(String rawColor) {
        if (rawColor == null || rawColor.isBlank()) return null;

        try {
            return PlayerColor.valueOf(rawColor.trim().toUpperCase());

        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static List<Zone> buildPreferredStartZones(List<String> preferredStartZoneNames, List<Zone> allZones) {
        if (preferredStartZoneNames == null || preferredStartZoneNames.isEmpty() || allZones == null || allZones.isEmpty()) {
            return null;
        }

        Map<String, Zone> zonesByName = allZones.stream()
            .collect(Collectors.toMap(Zone::getName, zone -> zone, (left, right) -> left, HashMap::new));

        List<Zone> preferredStartZones = new ArrayList<>();
        for (String zoneName : preferredStartZoneNames) {
            Zone preferredStartZone = zoneName == null ? null : zonesByName.get(zoneName);
            preferredStartZones.add(preferredStartZone);
        }

        return preferredStartZones;
    }

    private static List<Zone> buildPreferredStartZones(List<Player> playersSnapshot, List<RoomPlayer> roomPlayers, Map<String, String> selectedZonesByPlayerId, List<Zone> allZones) {
        if (playersSnapshot == null || playersSnapshot.isEmpty() || roomPlayers == null || roomPlayers.isEmpty() || selectedZonesByPlayerId == null || selectedZonesByPlayerId.isEmpty()) {
            return null;
        }

        Map<String, Zone> zonesByName = allZones.stream()
            .collect(Collectors.toMap(Zone::getName, zone -> zone, (left, right) -> left, HashMap::new));

        List<Zone> preferredStartZones = new ArrayList<>();
        for (int i = 0; i < playersSnapshot.size(); i++) {
            Zone preferredStartZone = null;
            if (i < roomPlayers.size()) {
                String selectedZoneName = selectedZonesByPlayerId.get(roomPlayers.get(i).getId());
                if (selectedZoneName != null) {
                    preferredStartZone = zonesByName.get(selectedZoneName);
                }
            }
            preferredStartZones.add(preferredStartZone);
        }

        return preferredStartZones;
    }
}
