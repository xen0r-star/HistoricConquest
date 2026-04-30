package com.historicconquest.historicconquest.controller.game;

import com.historicconquest.historicconquest.controller.page.QuestionController;
import com.historicconquest.historicconquest.controller.page.SelectAction;
import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.util.Texture;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import java.io.IOException;

public class GameHUD {
    @FXML private StackPane root;
    @FXML private Pane mapViewport;


    private DisplayInfoPlayer cachedInfoPlayer ;
    private CoallitionController cachedCoalition;
    private Parent coalitionNode;
    private Button playTurnBtn ;

    @FXML
    public void initialize() {
        ImageView noiseLayer = Texture.generatePaperGrain(1920, 1080, 0.1);

        noiseLayer.fitWidthProperty().bind(root.widthProperty());
        noiseLayer.fitHeightProperty().bind(root.heightProperty());

        root.getChildren().add(noiseLayer);
        noiseLayer.setViewOrder(-1.0);
        setupPlayturnButton();
    }

    public void initializeMap(Group mapInterface) {
        if (mapViewport != null) {
            mapViewport.getChildren().add(mapInterface);

            // Map cutting
            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(mapViewport.widthProperty());
            clip.heightProperty().bind(mapViewport.heightProperty());
            mapViewport.setClip(clip);
        }
    }


    private void setupPlayturnButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/PrincipalButton.fxml"));
            Parent buttonPanel = loader.load();

            PrincipalButton controller = loader.getController();
            controller.setGameHUD(this);

            StackPane.setAlignment(buttonPanel, Pos.BOTTOM_CENTER);
            StackPane.setMargin(buttonPanel, new javafx.geometry.Insets(0, 0, 40, 0));

            root.getChildren().add(buttonPanel);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement du FXML des boutons.");
        }
    }



    public void showSelectAction()
    {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/SelectAction.fxml"));
            Parent selectActionNode = loader.load();

            SelectAction controller = loader.getController();
            controller.setParentRoot(root);
            root.getChildren().add(selectActionNode);

            StackPane.setAlignment(selectActionNode, Pos.CENTER);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void togglePlayerInfo() {
        if (cachedInfoPlayer == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/DisplayInfoPlayer.fxml"));
                Parent playerInfoNode = loader.load();
                cachedInfoPlayer = loader.getController();

                root.getChildren().add(playerInfoNode);
                StackPane.setAlignment(playerInfoNode, Pos.TOP_LEFT);
                StackPane.setMargin(playerInfoNode, new javafx.geometry.Insets(30, 0, 0, 30));
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }


        Player current = GameController.getInstance().getCurrentPlayer();


        cachedInfoPlayer.show(current);
    }

    public void showCoalitionMenu() {

        Player current = GameController.getInstance().getCurrentPlayer();

        if (current != null && current.hasAlly()) {
            com.historicconquest.historicconquest.controller.overlay.NotificationController.show(
                    "Alliance",
                    "You are already in an alliance!",
                    com.historicconquest.historicconquest.controller.overlay.Notification.Type.INFORMATION,
                    3000
            );
            return;
        }

        if (cachedCoalition == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/coalitionnew.fxml"));
                coalitionNode = loader.load();
                cachedCoalition = loader.getController();

                root.getChildren().add(coalitionNode);
                StackPane.setAlignment(coalitionNode, Pos.CENTER);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        coalitionNode.setVisible(true);
        coalitionNode.toFront();
        cachedCoalition.refreshPlayerList();
    }

}

