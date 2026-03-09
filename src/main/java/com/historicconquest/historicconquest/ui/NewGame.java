package com.historicconquest.historicconquest.ui;


import com.historicconquest.historicconquest.Constant;
import com.historicconquest.historicconquest.MainApp;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.Objects;

public class NewGame {

    @SuppressWarnings("SpellCheckingInspection")
    public StackPane createView(MainApp app) {
        // 1. On crée le conteneur principal d'abord
        StackPane root = new StackPane();

        // --- LE FOND (HUD) ---
        Parent hudBackground = loadGameHudBackground();
        if (hudBackground != null) {
            hudBackground.setMouseTransparent(true);
            hudBackground.setFocusTraversable(false);
            root.getChildren().add(hudBackground); // Maintenant 'root' existe !
        }

        // --- TITRES ET SAISIE ---
        Label title = new Label("NEW");
        Label title2 = new Label("GAME");
        title.getStyleClass().add("title-label");
        title2.getStyleClass().add("title-label");

// 1. Récupérer l'URL de ton image de fond (ex : une barre de saisie stylisée)
        String bgPath = Objects.requireNonNull(getClass().getResource("/com/historicconquest/historicconquest/images/textFieldNewGame.png")).toExternalForm();

// 2. Créer le TextField (on le laisse totalement transparent)
        TextField playerName = new TextField();
        playerName.setPromptText("NAME : ");
        playerName.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-padding: 10;");

// 3. Créer le conteneur qui portera l'image
        HBox fieldContainer = new HBox(playerName);
        fieldContainer.setAlignment(Pos.CENTER);
        fieldContainer.setMaxWidth(700); // Ajuste selon la taille de ton image
        fieldContainer.setPrefHeight(100); // Ajuste selon la hauteur de ton image

// 4. Appliquer l'image au fond du conteneur
        fieldContainer.setStyle(
                "-fx-background-image: url('" + bgPath + "'); " +
                        "-fx-background-size: 100% 100%; " + // L'image remplit tout le rectangle
                        "-fx-background-repeat: no-repeat; " +
                        "-fx-background-position: center; " +
                        "-fx-background-color: transparent;"
        );

        VBox mainBox = new VBox(-5, title, title2, fieldContainer); // Ajout de playerName ici

        mainBox.setAlignment(Pos.CENTER);
        mainBox.setPickOnBounds(false);
        mainBox.setPadding(new Insets(0, 0, 30, 0));

        // --- BARRE ICONS DROITE ---
        Button settingsBtn = createIconButton(Constant.PATH + "images/settings.png");
        Button helpBtn = createIconButton(Constant.PATH + "images/help.png");
        Button exitBtn = createIconButton(Constant.PATH + "images/sortie.png");
        exitBtn.setOnAction(e -> MainApp.getInstance().exit());

        VBox iconBar = new VBox(-5, settingsBtn, helpBtn, exitBtn);
        iconBar.setAlignment(Pos.BOTTOM_RIGHT); // Alignement interne à la VBox
        iconBar.setPadding(new Insets(0, 15, 15, 0));
        iconBar.setPickOnBounds(false);
        iconBar.setMaxWidth(130);

        // Positionnement de l'iconBar dans le StackPane
        StackPane.setAlignment(iconBar, Pos.BOTTOM_RIGHT);

        // 2. On ajoute tout au root
        root.getChildren().addAll(mainBox, iconBar);

        return root;
    }

    private Parent loadGameHudBackground() {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource(Constant.PATH + "ui/GameHUD.fxml"),
                    "FXML introuvable: " + Constant.PATH + "ui/GameHUD.fxml"
            ));
            return loader.load();
        } catch (Exception e) {
            System.err.println("Erreur: impossible de charger " + Constant.PATH + "ui/GameHUD.fxml");
            return null;
        }
    }

    private Button createIconButton(String s) {

        try {
            Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream(s)));
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
}
