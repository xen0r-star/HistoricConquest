package com.historicconquest.historicconquest.controller.page.local;

import com.historicconquest.historicconquest.controller.game.GameBootstrapper;
import com.historicconquest.historicconquest.controller.game.MapBackgroundController;
import com.historicconquest.historicconquest.model.map.WorldMap;
import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.util.MapPlayerColor;
import com.historicconquest.historicconquest.view.map.MapView;
import com.historicconquest.historicconquest.view.map.MapViewFactory;
import com.historicconquest.historicconquest.view.map.ZoneView;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalZoneSelectionController {
    private static final String DEFAULT_ZONE_COLOR = "#f2e1bf";
    private static final String DEFAULT_ZONE_BORDER_COLOR = "#C5A682";

    @FXML private StackPane root;
    @FXML private Pane mapViewport;
    @FXML private Label StepLabel;
    @FXML private Label CurrentPlayerLabel;
    @FXML private VBox PlayersBox;
    @FXML private Button BackBtn;
    @FXML private Button ConfirmBtn;

    private final List<Player> players = new ArrayList<>();
    private final List<String> selectedZoneNames = new ArrayList<>();
    private final Map<String, ZoneView> zoneViewByName = new HashMap<>();
    private final Map<String, Zone> zoneByName = new HashMap<>();

    private StackPane hostRoot;
    private boolean attached;

    private int currentPlayerIndex;
    private String pendingZoneName;

    private static List<Player> pendingPlayers;

    public void attach(StackPane hostRoot, List<Player> initialPlayers) {
        this.hostRoot = hostRoot;

        if (!attached && hostRoot != null && root != null && !hostRoot.getChildren().contains(root)) {
            hostRoot.getChildren().add(root);
        }

        attached = true;
        if (root != null) root.toFront();

        loadPlayers(initialPlayers);
    }

    @FXML
    public void initialize() {
        if (BackBtn != null) {
            BackBtn.setOnAction(event -> detachOverlay());
        }
        if (ConfirmBtn != null) {
            ConfirmBtn.setOnAction(event -> confirmSelection());
            ConfirmBtn.setDisable(true);
        }

        if (pendingPlayers != null) {
            loadPlayers(pendingPlayers);
            pendingPlayers = null;
        }
    }

    private void loadPlayers(List<Player> incomingPlayers) {
        players.clear();
        if (incomingPlayers != null) {
            players.addAll(incomingPlayers);
        }

        selectedZoneNames.clear();
        for (int i = 0; i < players.size(); i++) {
            selectedZoneNames.add(null);
        }

        currentPlayerIndex = 0;
        pendingZoneName = null;
        if (ConfirmBtn != null) {
            ConfirmBtn.setDisable(true);
        }
        ensureMapInitialized();
        refreshAllZones();
        refreshPlayersPanel();
        updateStepLabel();
        updateCurrentPlayerLabel();
    }

    public void selectZone(Zone zone) {
        if (zone == null || players.isEmpty() || currentPlayerIndex >= players.size()) {
            return;
        }

        if (isZoneAlreadySelected(zone.getName())) return;

        pendingZoneName = zone.getName();
        if (ConfirmBtn != null) {
            ConfirmBtn.setDisable(false);
        }
        refreshAllZones();
    }

    private void confirmSelection() {
        if (players.isEmpty() || currentPlayerIndex >= players.size()) return;
        if (pendingZoneName == null || pendingZoneName.isBlank()) return;
        if (isZoneAlreadySelected(pendingZoneName)) return;

        selectedZoneNames.set(currentPlayerIndex, pendingZoneName);
        pendingZoneName = null;
        if (ConfirmBtn != null) {
            ConfirmBtn.setDisable(true);
        }

        refreshAllZones();
        refreshPlayersPanel();

        if (currentPlayerIndex < players.size() - 1) {
            currentPlayerIndex++;
            updateStepLabel();
            updateCurrentPlayerLabel();
            return;
        }

        launchGame();
    }

    private boolean isZoneAlreadySelected(String zoneName) {
        if (zoneName == null) return false;

        for (String selectedZoneName : selectedZoneNames) {
            if (selectedZoneName != null && selectedZoneName.equalsIgnoreCase(zoneName)) {
                return true;
            }
        }

        return false;
    }

    private void ensureMapInitialized() {
        if (!zoneViewByName.isEmpty()) {
            return;
        }

        WorldMap worldMap = new WorldMap(true, false, false, Color.web(DEFAULT_ZONE_COLOR), Color.web(DEFAULT_ZONE_BORDER_COLOR));
        MapView mapView = MapViewFactory.build(worldMap, true);
        Group mapInterface = mapView.getRoot();

        MapBackgroundController.show(root, mapViewport, -400, -150, 0);

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
        for (int i = 0; i < selectedZoneNames.size(); i++) {
            String zoneName = selectedZoneNames.get(i);
            Player player = i < players.size() ? players.get(i) : null;
            if (zoneName == null || player == null) continue;

            Zone zone = zoneByName.get(zoneName);
            ZoneView zoneView = zoneViewByName.get(zoneName);
            if (zone == null || zoneView == null) continue;

            zone.setColor(player.getColor().getJavafxColor());
            zoneView.setBlockHover(true);
        }

        if (pendingZoneName != null) {
            Zone pendingZone = zoneByName.get(pendingZoneName);
            ZoneView pendingView = zoneViewByName.get(pendingZoneName);
            if (pendingZone != null && pendingView != null) {
                Player currentPlayer = currentPlayerIndex < players.size() ? players.get(currentPlayerIndex) : null;
                if (currentPlayer != null) {
                    pendingZone.setColor(currentPlayer.getColor().getJavafxColor());
                }
                pendingView.setFocusedZone(true);
            }
        }
    }

    private void refreshPlayersPanel() {
        if (PlayersBox == null) {
            return;
        }

        PlayersBox.getChildren().clear();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            String selectedZoneName = selectedZoneNames.get(i);
            String zoneName = selectedZoneName == null ? "en attente" : selectedZoneName;

            Label playerLabel = new Label(player.getPseudo() + "  •  " + zoneName);
            playerLabel.setWrapText(true);
            playerLabel.setMaxWidth(Double.MAX_VALUE);
            playerLabel.setStyle(
                "-fx-background-color: " + MapPlayerColor.hex(player.getColor().name()) + ";" +
                "-fx-text-fill: #FFF8ED;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 8 12;"
            );
            PlayersBox.getChildren().add(playerLabel);
        }
    }

    private void updateStepLabel() {
        if (StepLabel == null) return;

        int step = Math.min(currentPlayerIndex + 1, players.size());
        StepLabel.setText("Player (" + step + " / " + players.size() + ")");
    }

    private void updateCurrentPlayerLabel() {
        if (CurrentPlayerLabel == null) return;

        if (players.isEmpty() || currentPlayerIndex >= players.size()) {
            CurrentPlayerLabel.setText("Aucun joueur disponible.");
            return;
        }

        Player currentPlayer = players.get(currentPlayerIndex);
        CurrentPlayerLabel.setText(currentPlayer.getPseudo() + " choisit une zone de depart");
    }

    private void launchGame() {
        GameBootstrapper.launchSoloGame(root, new ArrayList<>(players), new ArrayList<>(selectedZoneNames));
    }

    private void detachOverlay() {
        if (hostRoot != null && root != null) {
            hostRoot.getChildren().remove(root);
        }
    }
}

