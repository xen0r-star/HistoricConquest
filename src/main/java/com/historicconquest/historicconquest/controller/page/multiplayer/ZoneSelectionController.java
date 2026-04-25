package com.historicconquest.historicconquest.controller.page.multiplayer;

import com.historicconquest.historicconquest.controller.core.AppController;
import com.historicconquest.historicconquest.controller.core.AppPage;
import com.historicconquest.historicconquest.controller.game.GameBootstrapper;
import com.historicconquest.historicconquest.controller.game.MapBackgroundController;
import com.historicconquest.historicconquest.controller.overlay.Notification;
import com.historicconquest.historicconquest.controller.overlay.NotificationController;
import com.historicconquest.historicconquest.model.map.WorldMap;
import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.network.model.RoomPlayer;
import com.historicconquest.historicconquest.model.player.PlayerColor;
import com.historicconquest.historicconquest.service.network.RoomService;
import com.historicconquest.historicconquest.view.map.MapView;
import com.historicconquest.historicconquest.view.map.MapViewFactory;
import com.historicconquest.historicconquest.view.map.ZoneView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZoneSelectionController {
    private static final String DEFAULT_ZONE_COLOR = "#f2e1bf";
    private static final String DEFAULT_ZONE_BORDER_COLOR = "#C5A682";

    @FXML private StackPane root;
    @FXML private Pane mapViewport;
    @FXML private Label CountdownLabel;
    @FXML private Label StatusLabel;
    @FXML private VBox PlayersBox;

    private final List<RoomPlayer> roomPlayers = new ArrayList<>();
    private final Map<String, String> selectedZonesByPlayerId = new HashMap<>();
    private final Map<String, ZoneView> zoneViewByName = new HashMap<>();
    private final Map<String, Zone> zoneByName = new HashMap<>();

    private StackPane hostRoot;
    private Timeline countdownTimeline;
    private long selectionEndsAt;
    private boolean selectionActive;
    private boolean gameLaunched;
    private boolean attached;

    public void attach(StackPane hostRoot, List<RoomPlayer> initialPlayers) {
        this.hostRoot = hostRoot;
        roomPlayers.clear();
        if (initialPlayers != null) {
            roomPlayers.addAll(initialPlayers);
        }

        if (!attached && hostRoot != null && root != null && !hostRoot.getChildren().contains(root)) {
            hostRoot.getChildren().add(root);
        }

        attached = true;
        if (root != null) root.toFront();
        refreshPlayersPanel();
    }

    public void handleZoneSelectionStarted(int seconds, long startAt, Map<String, String> selectedZones) {
        selectionActive = true;
        gameLaunched = false;
        selectionEndsAt = startAt;

        selectedZonesByPlayerId.clear();
        if (selectedZones != null) {
            selectedZonesByPlayerId.putAll(selectedZones);
        }

        ensureMapInitialized();
        refreshAllZones();
        refreshPlayersPanel();
        updateStatusText("Choose a starting zone before the timer reaches zero.");
        startCountdown(seconds);
    }

    public void handleZoneSelectionUpdated(Map<String, String> selectedZones) {
        selectedZonesByPlayerId.clear();
        if (selectedZones != null) {
            selectedZonesByPlayerId.putAll(selectedZones);
        }

        refreshAllZones();
        refreshPlayersPanel();
    }

    public void handlePlayerJoin(RoomPlayer player) {
        if (player == null) return;
        roomPlayers.add(player);
        refreshPlayersPanel();
    }

    public void handlePlayerQuit(String playerId) {
        removePlayer(playerId);
        refreshAllZones();
        refreshPlayersPanel();
    }

    public void handlePlayerKick(String playerId) {
        removePlayer(playerId);
        refreshAllZones();
        refreshPlayersPanel();
    }

    public void handlePlayerColorChange(String playerId, String newColor) {
        RoomPlayer player = findPlayer(playerId);
        if (player != null) {
            player.setColor(newColor);
        }

        refreshAllZones();
        refreshPlayersPanel();
    }

    public void handlePlayerPseudoChange(String playerId, String newPseudo) {
        RoomPlayer player = findPlayer(playerId);
        if (player != null) {
            player.setName(newPseudo);
        }

        refreshPlayersPanel();
    }

    public void handlePlayerStatusChange(String playerId, String newStatus) {
        RoomPlayer player = findPlayer(playerId);
        if (player != null) {
            player.setStatus(newStatus);
        }

        refreshPlayersPanel();
    }

    public void handlePlayerPings(Map<String, Integer> pings) {
        if (pings == null) return;

        for (RoomPlayer player : roomPlayers) {
            Integer ping = pings.get(player.getId());
            if (ping != null) {
                player.setPing(ping);
            }
        }

        refreshPlayersPanel();
    }

    public void handleGameStartCancelled(String reason) {
        if (gameLaunched) return;

        selectionActive = false;
        stopCountdown();
        detachOverlay();
        String messageReason = reason == null || reason.isBlank() ? "The game could not start." : reason;
        updateStatusText(messageReason);

        NotificationController.show(
            "Start cancelled",
            messageReason,
            Notification.Type.INFORMATION
        );
    }

    public void handleRoomDeleted() {
        if (gameLaunched) return;

        selectionActive = false;
        stopCountdown();
        detachOverlay();
        RoomService.reset();
        AppController.getInstance().showPage(AppPage.MULTIPLAYER);
    }

    public void handleGameStarted(Map<String, String> selectedZones) {
        if (gameLaunched) return;

        selectionActive = false;
        gameLaunched = true;
        stopCountdown();
        detachOverlay();

        if (selectedZones != null && !selectedZones.isEmpty()) {
            selectedZonesByPlayerId.clear();
            selectedZonesByPlayerId.putAll(selectedZones);
        }

        GameBootstrapper.launchGame(hostRoot, new ArrayList<>(roomPlayers), selectedZonesByPlayerId);
    }

    public void selectZone(Zone zone) {
        if (!selectionActive || gameLaunched || zone == null) {
            return;
        }

        RoomService.selectZone(zone.getName());
    }

    private void ensureMapInitialized() {
        if (!zoneViewByName.isEmpty()) {
            return;
        }

        WorldMap worldMap = new WorldMap(true, false, false, Color.web(DEFAULT_ZONE_COLOR), Color.web(DEFAULT_ZONE_BORDER_COLOR));
        MapView mapView = MapViewFactory.build(worldMap, true);
        Group mapInterface = mapView.getRoot();

        MapBackgroundController.show(root, mapViewport, -55, -30, -0.03);

        Group backgroundMapInterface = null;
        if (!mapViewport.getChildren().isEmpty() && mapViewport.getChildren().getFirst() instanceof Group group) {
            backgroundMapInterface = group;
        }

        if (backgroundMapInterface != null) {
            mapInterface.scaleXProperty().bind(backgroundMapInterface.scaleXProperty());
            mapInterface.scaleYProperty().bind(backgroundMapInterface.scaleYProperty());
            mapInterface.translateXProperty().bind(backgroundMapInterface.translateXProperty());
            mapInterface.translateYProperty().bind(backgroundMapInterface.translateYProperty());
        }

        if (!mapViewport.getChildren().contains(mapInterface)) {
            mapViewport.getChildren().add(mapInterface);
        }

        mapInterface.toFront();

        zoneByName.clear();
        zoneViewByName.clear();
        for (Zone zone : worldMap.getAllZones()) {
            zoneByName.put(zone.getName(), zone);
            ZoneView zoneView = mapView.getViewFor(zone);
            if (zoneView == null) continue;

            zoneView.setOnMouseClicked(event -> {
                selectZone(zone);
                event.consume();
            });
            zoneViewByName.put(zone.getName(), zoneView);
        }
    }

    private void refreshAllZones() {
        if (zoneViewByName.isEmpty()) {
            return;
        }

        resetAllZoneVisuals();
        applySelectedZoneVisuals();
    }

    private void resetAllZoneVisuals() {
        for (Map.Entry<String, ZoneView> entry : zoneViewByName.entrySet()) {
            Zone zone = zoneByName.get(entry.getKey());
            ZoneView zoneView = entry.getValue();
            if (zone == null || zoneView == null) continue;

            zone.setColor(zone.getBaseColor());
            zoneView.setBlockHover(false);
            zoneView.setFocusedZone(false);
        }
    }

    private void applySelectedZoneVisuals() {
        for (Map.Entry<String, String> entry : selectedZonesByPlayerId.entrySet()) {
            Zone zone = zoneByName.get(entry.getValue());
            ZoneView zoneView = zoneViewByName.get(entry.getValue());
            if (zone == null || zoneView == null) continue;

            RoomPlayer player = findPlayer(entry.getKey());
            zone.setColor(mapPlayerColor(player == null ? null : player.getColor()));
            zoneView.setBlockHover(true);
        }
    }

    private void refreshPlayersPanel() {
        if (PlayersBox == null) {
            return;
        }

        PlayersBox.getChildren().clear();
        for (RoomPlayer player : roomPlayers) {
            String zone = selectedZonesByPlayerId.get(player.getId());
            Label playerLabel = new Label(player.getName() + "  •  " + (zone == null ? "waiting" : zone));
            playerLabel.setWrapText(true);
            playerLabel.setMaxWidth(Double.MAX_VALUE);
            playerLabel.setStyle(
                "-fx-background-color: rgba(238,220,190,0.85);" +
                "-fx-text-fill: #463d2a;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 8 12;"
            );
            PlayersBox.getChildren().add(playerLabel);
        }
    }

    private void updateStatusText(String status) {
        if (StatusLabel != null) {
            StatusLabel.setText(status == null || status.isBlank() ? "Select a zone" : status);
        }
    }

    private void startCountdown(int seconds) {
        stopCountdown();
        updateCountdownLabel(seconds);

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            long remaining = Math.max(0L, (selectionEndsAt - System.currentTimeMillis() + 999L) / 1000L);
            updateCountdownLabel((int) remaining);

            if (remaining == 0L) {
                stopCountdown();
                updateStatusText("Waiting for server confirmation...");
            }
        }));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.playFromStart();
    }

    private void updateCountdownLabel(int seconds) {
        if (CountdownLabel != null) {
            CountdownLabel.setText(String.valueOf(Math.max(seconds, 0)));
        }
    }

    private void stopCountdown() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
            countdownTimeline = null;
        }
    }

    private void detachOverlay() {
        if (hostRoot != null && root != null) {
            hostRoot.getChildren().remove(root);
        }
    }

    private void removePlayer(String playerId) {
        if (playerId == null) return;

        roomPlayers.removeIf(player -> playerId.equals(player.getId()));
        selectedZonesByPlayerId.remove(playerId);
    }

    private RoomPlayer findPlayer(String playerId) {
        if (playerId == null) return null;

        for (RoomPlayer player : roomPlayers) {
            if (playerId.equals(player.getId())) {
                return player;
            }
        }

        return null;
    }

    private Color mapPlayerColor(String rawColor) {
        if (rawColor == null || rawColor.isBlank()) {
            return Color.web(DEFAULT_ZONE_COLOR);
        }

        try {
            return switch (PlayerColor.valueOf(rawColor.trim().toUpperCase())) {
                case RED -> Color.web("#A2383A");
                case ORANGE -> Color.web("#B9693E");
                case YELLOW -> Color.web("#B68D3B");
                case GREEN -> Color.web("#61712A");
                case LIME -> Color.web("#89A238");
                case CYAN -> Color.web("#38A270");
                case BLUE -> Color.web("#389BA2");
                case LIGHT_BLUE -> Color.web("#385BA2");
                case PURPLE -> Color.web("#6838A2");
                case PINK -> Color.web("#A23887");
            };

        } catch (Exception e) {
            return Color.web(DEFAULT_ZONE_COLOR);
        }
    }
}



