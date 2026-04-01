package com.historicconquest.historicconquest.view;

import com.historicconquest.historicconquest.app.App;
import com.historicconquest.historicconquest.controller.MapBackgroundController;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class NewGame {
    private static final Logger logger = LoggerFactory.getLogger(NewGame.class);

    private Pane mapViewport;

    public StackPane createView(App app) {
        StackPane root = new StackPane();
        VBox cornerButtons = new VBox(15);
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource("/view/fxml/HomePage.fxml")
            ));
            Parent homeRoot = loader.load();

            // Récupération des boutons du FXML
            Button btnExit = (Button) homeRoot.lookup("#exitBtn");
            Button btnHelp = (Button) homeRoot.lookup("#helpBtn");
            Button btnSettings = (Button) homeRoot.lookup("#settingsBtn");

            if (btnExit != null) {
                btnExit.setOnAction(e -> App.getInstance().exit());
            }

            if (btnHelp != null) {
                btnHelp.setOnAction(e -> {
                    logger.debug("Help button clicked in NewGame view");
                    // On passe 'root' (le StackPane de NewGame) à la méthode
                    App.getInstance().showHelp(true);
                });
            }


            if (btnSettings != null) {
                btnSettings.setOnAction(e -> {
                    App.getInstance().showSettings(true);

                }); }

            if (btnSettings != null) cornerButtons.getChildren().add(btnSettings);
            if (btnHelp != null) cornerButtons.getChildren().add(btnHelp);
            if (btnExit != null) cornerButtons.getChildren().add(btnExit);

            cornerButtons.setAlignment(Pos.BOTTOM_RIGHT);
            cornerButtons.setPickOnBounds(false);
            StackPane.setMargin(cornerButtons, new Insets(50));
        } catch (Exception e) {
            logger.error("Failed to load HomePage buttons in NewGame view", e);
        }

        Parent hudBackground = loadGameHudBackground();
        if (hudBackground != null) {
            // IMPORTANT : Ne pas mettre MouseTransparent sur TOUT le hud,
            // sinon on ne peut plus cliquer sur les boutons de sortie/aide !
            hudBackground.setFocusTraversable(false);
            root.getChildren().add(hudBackground);
        }

        root.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/view/styles/newgame.css")
        ).toExternalForm());

        mapViewport = new Pane();
        mapViewport.setMouseTransparent(true);
        StackPane.setMargin(mapViewport, new Insets(30));
        root.getChildren().add(mapViewport);



        // Initialise la map derrière (centrée + visible)
        MapBackgroundController.show(
            root, mapViewport,
            -55 ,-30, -0.03
        );

        // ===== UI New Game =====
        String[] pawnImages = {
            "/pawn/pawn_black.png",
            "/pawn/pawn_grey.png",
            "/pawn/pawn_white.png",
            "/pawn/pawn_beige.png",
        };
        final String[] selectedPawn = { pawnImages[0] };

        Label title = new Label("NEW");
        Label title2 = new Label("GAME");
        title.getStyleClass().add("title-label");
        title2.getStyleClass().add("title-label");

        String bgPath = Objects.requireNonNull(
                getClass().getResource("/view/images/textFieldNewGame.png")
        ).toExternalForm();

        Label nameLabel = new Label("Name : ");
        nameLabel.setStyle("-fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 18px; -fx-font-family: Georgia");

        TextField playerName = new TextField();
        playerName.setPromptText("Enter your name...");
        playerName.setPrefWidth(500);
        playerName.setStyle("-fx-background-color: transparent; -fx-text-fill: black; -fx-padding:0 10; ");

        HBox fieldContainer = new HBox(10, nameLabel, playerName);
        fieldContainer.setAlignment(Pos.CENTER);
        fieldContainer.setMaxWidth(700);
        fieldContainer.setPrefHeight(100);
        fieldContainer.setStyle(
                "-fx-background-image: url('" + bgPath + "');" +
                        "-fx-background-size: 100% 100%;" +
                        "-fx-background-repeat: no-repeat;" +
                        "-fx-background-position: center;" +
                        "-fx-background-color: transparent;"
        );

        Label chooseLabel = new Label("SELECT YOUR PAWN");
        chooseLabel.getStyleClass().add("subtitle-label");

        HBox pawnBox = new HBox(20);
        pawnBox.setAlignment(Pos.CENTER);

        for (String path : pawnImages) {
            ImageView iv = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(path))));
            iv.setFitWidth(150);
            iv.setPreserveRatio(true);

            Button pawnBtn = new Button();
            pawnBtn.setGraphic(iv);
            pawnBtn.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-cursor: hand; -fx-background-radius: 10;");

            pawnBtn.setOnAction(e -> {
                selectedPawn[0] = path;
                pawnBox.getChildren().forEach(node ->
                        node.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-cursor: hand; -fx-background-radius: 10;")
                );
                pawnBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #FFD700; -fx-border-width: 3; -fx-border-radius: 10;");
            });

            pawnBox.getChildren().add(pawnBtn);
        }

        VBox mainBox = new VBox(0, title, title2, fieldContainer, chooseLabel, pawnBox);
        VBox.setMargin(title2, new Insets(-5, 0, 0, 0));
        VBox.setMargin(fieldContainer, new Insets(30, 0, 0, 0));
        VBox.setMargin(chooseLabel, new Insets(40, 0, 10, 0));
        VBox.setMargin(pawnBox, new Insets(10, 0, 0, 0));
        mainBox.setAlignment(Pos.TOP_CENTER);
        mainBox.setPickOnBounds(false);
        mainBox.setPadding(new Insets(100, 0, 0, 0));

        root.getChildren().add(mainBox);
        root.getChildren().add(cornerButtons);

        return root;
    }



    private Parent loadGameHudBackground() {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource("/view/fxml/GameHUD.fxml"),
                    "FXML introuvable: " + "/view/fxml/GameHUD.fxml"
            ));
            return loader.load();

        } catch (Exception e) {
            logger.error("Erreur: impossible de charger " + "/view/fxml/GameHUD.fxml", e);
            return null;
        }
    }
}
