package com.historicconquest.historicconquest.controller.page.game;

import com.historicconquest.historicconquest.controller.game.GameController;
import com.historicconquest.historicconquest.controller.overlay.Notification;
import com.historicconquest.historicconquest.controller.overlay.NotificationController;
import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.util.Texture;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.util.List;

public class GameHUD {
    @FXML private StackPane root;
    @FXML private Pane mapViewport;

    private CoalitionController cachedCoalition;
    private Parent coalitionNode;
    private DisplayInfoPlayer cachedPlayerInfo;
    private DisplayInfoGame cachedGameInfo;
    private PrincipalButton principalButtonController;
    private Parent principalButtonPanel;

    private List<Player> pendingPlayers;
    private Integer pendingCurrentIndex;


    @FXML
    public void initialize() {
        ImageView noiseLayer = Texture.generatePaperGrain(1920, 1080, 0.1);

        noiseLayer.fitWidthProperty().bind(root.widthProperty());
        noiseLayer.fitHeightProperty().bind(root.heightProperty());

        root.getChildren().add(noiseLayer);
        noiseLayer.setViewOrder(-1.0);
        setupPrincipalButton();
        setupDisplayInfoPlayer();
        setupDisplayInfoGame();

        showLegend();
    }

    public void initializeMap(Group mapInterface) {
        if (mapViewport != null) {
            mapViewport.getChildren().add(mapInterface);

            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(mapViewport.widthProperty());
            clip.heightProperty().bind(mapViewport.heightProperty());
            mapViewport.setClip(clip);
        }
    }


    private void setupPrincipalButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/game/PrincipalButton.fxml"));
            principalButtonPanel = loader.load();

            PrincipalButton controller = loader.getController();
            principalButtonController = controller;
            controller.setGameHUD(this);
            controller.setParentRoot(root);

            StackPane.setAlignment(principalButtonPanel, Pos.BOTTOM_CENTER);
            StackPane.setMargin(principalButtonPanel, new Insets(0, 0, 30, 0));

            root.getChildren().add(principalButtonPanel);

            updateActionButtonVisibility(true);

        } catch (IOException e) {
            System.err.println("Error loading PrincipalButton.fxml");
        }
    }

    private void setupDisplayInfoPlayer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/game/DisplayInfoPlayer.fxml"));
            Parent playerInfoNode = loader.load();

            cachedPlayerInfo = loader.getController();

            root.getChildren().add(playerInfoNode);
            StackPane.setAlignment(playerInfoNode, Pos.TOP_LEFT);
            StackPane.setMargin(playerInfoNode, new Insets(45, 45, 45, 45));

        } catch (IOException e) {
            System.err.println("Error loading DisplayInfoPlayer.fxml");
        }
    }

    private void setupDisplayInfoGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/game/DisplayInfoGame.fxml"));
            Parent gameInfoNode = loader.load();

            cachedGameInfo = loader.getController();
            if (pendingPlayers != null) {
                int index = pendingCurrentIndex != null ? pendingCurrentIndex : 0;
                cachedGameInfo.show(pendingPlayers, index);
                pendingPlayers = null;
                pendingCurrentIndex = null;
            }

            root.getChildren().add(gameInfoNode);
            StackPane.setAlignment(gameInfoNode, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(gameInfoNode, new Insets(45, 45, 45, 45));

        } catch (IOException e) {
            System.err.println("Error loading DisplayInfoGame.fxml");
        }
    }

    private void showLegend() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/game/MapLegend.fxml"));
            Parent mapLegendNode = loader.load();

            root.getChildren().add(mapLegendNode);
            StackPane.setAlignment(mapLegendNode, Pos.BOTTOM_LEFT);
            StackPane.setMargin(mapLegendNode, new Insets(45, 45, 45, 45));

        } catch (IOException e) {
            System.err.println("Error loading MapLegend.fxml");
        }
    }


    public void showCoalitionMenu() {
        Player current = GameController.getInstance().getCurrentPlayer();

        if (current != null && current.hasAlly()) {
            NotificationController.show(
                "Alliance",
                "You are already in an alliance!",
                Notification.Type.INFORMATION,
                3000
            );
            return;
        }

        if (cachedCoalition == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/game/coalitionnew.fxml"));
                coalitionNode = loader.load();
                cachedCoalition = loader.getController();

                root.getChildren().add(coalitionNode);
                StackPane.setAlignment(coalitionNode, Pos.CENTER);

            } catch (IOException e) {
                System.err.println("Error loading coalitionnew.fxml");
                return;
            }
        }

        coalitionNode.setVisible(true);
        coalitionNode.toFront();
        cachedCoalition.refreshPlayerList();
    }

    public void refreshGameInfo(List<Player> players, int currentPlayerIndex) {
        if (cachedGameInfo != null) {
            cachedGameInfo.show(players, currentPlayerIndex);
        } else {
            pendingPlayers = players;
            pendingCurrentIndex = currentPlayerIndex;
        }
    }

    public void refreshPlayerInfo(Player player) {
        if (cachedPlayerInfo != null) {
            cachedPlayerInfo.updatePlayerData(player);
        }
        if (principalButtonController != null) {
            principalButtonController.updateAttackAvailability(player);
            principalButtonController.resetActionSelection();
        }
    }

    public void updateTravelTargetPrompt(String zoneName, int distance, boolean isBoat) {
        if (principalButtonController != null) {
            principalButtonController.updateTravelTarget(zoneName, distance, isBoat);
        }
    }

    public void updateActionButtonVisibility(boolean isVisible) {
        if (principalButtonController != null) {
            principalButtonController.setActionButtonsVisible(isVisible);
        }
    }

    public void updateTurnStatus(String message) {
        if (principalButtonController == null) {
            return;
        }

        if (message == null || message.isBlank()) {
            principalButtonController.clearTurnStatus();
            return;
        }

        principalButtonController.showTurnStatus(message);
    }
}
