package com.historicconquest.historicconquest.ui;

import com.historicconquest.historicconquest.Constant;
import com.historicconquest.historicconquest.MainApp;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.Objects;

public class HomePage {

    public StackPane createView(MainApp mainApp) {
        // --- TITRE ---
        Label title = new Label("HISTORIC");
        Label title2 = new Label("CONQUEST");
        title.getStyleClass().add("title-label");
        title2.getStyleClass().add("title-label");

        // --- BOUTONS PRINCIPAUX ---
        Button newGame = createImageButton(Constant.PATH + "images/new.png");
        Button loadGame = createImageButton(Constant.PATH + "images/load.png");
        Button multiplayer = createImageButton(Constant.PATH + "images/multiplayer.png");

        newGame.setOnAction(e -> {
            newGame.setDisable(true);
            mainApp.startGame();
        });

        VBox mainBox = new VBox(-5, title,title2, newGame, loadGame, multiplayer);
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setPickOnBounds(false);
        mainBox.setPadding(new Insets(0, 0, 30, 0));

        // --- BARRE ICONES DROITE ---
        Button settingsBtn = createIconButton(Constant.PATH + "images/settings.png");
        Button helpBtn = createIconButton(Constant.PATH + "images/help.png");
        Button exitBtn = createIconButton(Constant.PATH + "images/sortie.png");
        exitBtn.setOnAction(e -> mainApp.getStage().close());

        VBox iconBar = new VBox(-5, settingsBtn, helpBtn, exitBtn);
        iconBar.setAlignment(Pos.BOTTOM_CENTER);
        iconBar.setPadding(new Insets(0, 15, 15, 0));
        iconBar.setPickOnBounds(false);
        iconBar.setMaxWidth(130);
        StackPane.setAlignment(iconBar, Pos.BOTTOM_RIGHT);

        // --- "BACKGROUND" = GameHUD.fxml à la place de l'image ---
        Parent hudBackground = loadGameHudBackground();

        // --- ROOT ---
        StackPane root = new StackPane();

        if (hudBackground != null) {
            // Important: le fond ne doit pas manger les clics
            hudBackground.setMouseTransparent(true);

            // (optionnel) tu peux aussi désactiver le focus:
            hudBackground.setFocusTraversable(false);

            root.getChildren().add(hudBackground);
        }

        // Menu au-dessus
        root.getChildren().addAll(mainBox, iconBar);

        return root;
    }

    private Parent loadGameHudBackground() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource(Constant.PATH + "ui/GameHUD.fxml"))
            );
            return loader.load();
        } catch (Exception e) {
            System.out.println("Erreur: impossible de charger " + Constant.PATH + "ui/GameHUD.fxml");
            e.printStackTrace();
            return null;
        }
    }

    private Button createImageButton(String imagePath) {
        try {
            Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
            ImageView iv = new ImageView(img);
            iv.setFitWidth(450);
            iv.setFitHeight(110);
            iv.setPreserveRatio(true);

            Button btn = new Button();
            btn.setGraphic(iv);
            btn.setPrefSize(450, 110);
            btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

            DropShadow hoverShadow = new DropShadow(15, Color.rgb(0, 0, 0, 0.6));
            btn.setOnMouseEntered(e -> {
                iv.setScaleX(1.1);
                iv.setScaleY(1.1);
                iv.setEffect(hoverShadow);
            });
            btn.setOnMouseExited(e -> {
                iv.setScaleX(1.0);
                iv.setScaleY(1.0);
                iv.setEffect(null);
            });

            return btn;
        } catch (Exception e) {
            return new Button("Image manquante");
        }
    }

    private Button createIconButton(String imagePath) {
        try {
            Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
            ImageView iv = new ImageView(img);
            iv.setFitWidth(110);
            iv.setFitHeight(110);
            iv.setPreserveRatio(true);

            Button btn = new Button();
            btn.setGraphic(iv);
            btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

            return btn;
        } catch (Exception e) {
            return new Button("Icon");
        }
    }
}