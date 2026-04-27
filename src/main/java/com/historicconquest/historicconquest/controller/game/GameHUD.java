package com.historicconquest.historicconquest.controller.game;

import com.historicconquest.historicconquest.controller.page.QuestionController;
import com.historicconquest.historicconquest.controller.page.SelectAction;
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



    private Button playTurnBtn ;

    @FXML
    public void initialize() {
        ImageView noiseLayer = Texture.generatePaperGrain(1920, 1080, 0.1);

        noiseLayer.fitWidthProperty().bind(root.widthProperty());
        noiseLayer.fitHeightProperty().bind(root.heightProperty());

        root.getChildren().add(noiseLayer);
        noiseLayer.setViewOrder(-1.0);
        setupPlayturnButton();
        //showPlayerInfo(); test
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


    private void setupPlayturnButton()
    {
        playTurnBtn = new Button("Lancer la question");

        // Style (tu peux adapter avec ton CSS)
        playTurnBtn.setStyle("-fx-background-color: #EEDCBE;; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 10 20;");

        // Positionnement en bas à droite
        StackPane.setAlignment(playTurnBtn, Pos.BOTTOM_CENTER);
        StackPane.setMargin(playTurnBtn, new javafx.geometry.Insets(0, 40, 40, 0));

        root.getChildren().add(playTurnBtn);

        playTurnBtn.setOnAction(e -> {
            System.out.println("Bouton cliqué !");
            showSelectAction();
            //QuestionController.showChoiceDifficultPage(root);
            // GameController.getInstance().startTurnSequence();
        });
    }



    private void showSelectAction()
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


    private void showPlayerInfo() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/DisplayInfoPlayer.fxml"));
            Parent playerInfoNode = loader.load();

            DisplayInfoPlayer controller = loader.getController();


            root.getChildren().add(playerInfoNode);

            StackPane.setAlignment(playerInfoNode, Pos.TOP_RIGHT);
            StackPane.setMargin(playerInfoNode, new javafx.geometry.Insets(20, 0, 0, 20));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

