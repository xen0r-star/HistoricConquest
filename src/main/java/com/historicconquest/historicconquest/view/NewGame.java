package com.historicconquest.historicconquest.view;

import com.historicconquest.historicconquest.app.App;
import com.historicconquest.historicconquest.controller.GameController;
import com.historicconquest.historicconquest.controller.MapBackgroundController;
import com.historicconquest.historicconquest.controller.PawnController;
import com.historicconquest.historicconquest.model.map.WorldMap;
import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.model.player.PlayerColor;
import com.historicconquest.historicconquest.service.map.MapNavigationService;
import com.historicconquest.historicconquest.view.map.MapView;
import com.historicconquest.historicconquest.view.map.MapViewFactory;
import com.historicconquest.historicconquest.view.map.ZoneView;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class NewGame {
    private static final double PAWN_SIZE_PX = 50.0;
    private static final double PAWN_BUTTON_SIZE_PX = 72.0;
    private final List<Player> ListPlayer = new ArrayList<>();
    private static final int Nb_PLAYERS = 4;
    private final Set<PlayerColor> usedPawnColors = new HashSet<>();

    public StackPane createView(App app) {
        StackPane root = new StackPane();
        VBox cornerButtons = new VBox(15);

        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource("/view/fxml/HomePage.fxml")
            ));
            Parent homeRoot = loader.load();

            Button btnExit = (Button) homeRoot.lookup("#exitBtn");
            Button btnHelp = (Button) homeRoot.lookup("#helpBtn");
            Button btnSettings = (Button) homeRoot.lookup("#settingsBtn");

            if (btnExit != null) btnExit.setOnAction(e -> app.exit());
            if (btnHelp != null) btnHelp.setOnAction(e -> app.showHelp(true));
            if (btnSettings != null) btnSettings.setOnAction(e -> app.showSettings(true));

            if (btnSettings != null) cornerButtons.getChildren().add(btnSettings);
            if (btnHelp != null) cornerButtons.getChildren().add(btnHelp);
            if (btnExit != null) cornerButtons.getChildren().add(btnExit);

            cornerButtons.setAlignment(Pos.BOTTOM_RIGHT);
            cornerButtons.setPickOnBounds(false);
            StackPane.setMargin(cornerButtons, new Insets(50));
        } catch (Exception e) {
            System.err.println("Erreur chargement boutons : " + e.getMessage());
        }

        Parent hudBackground = loadGameHudBackground();
        if (hudBackground != null) {
            hudBackground.setFocusTraversable(false);
            root.getChildren().add(hudBackground);
        }

        root.getStylesheets().add(Objects.requireNonNull(
            getClass().getResource("/view/styles/newgame.css")
        ).toExternalForm());

        Pane mapViewport = new Pane();
        mapViewport.setMouseTransparent(true);
        StackPane.setMargin(mapViewport, new Insets(30));
        root.getChildren().add(mapViewport);

        MapBackgroundController.show(
            root, mapViewport,
            -55, -30, -0.03
        );

        // ===== UI New Game =====
        List<PlayerColor> availableColors = List.of(
            PlayerColor.RED,
            PlayerColor.BLUE,
            PlayerColor.GREEN,
            PlayerColor.YELLOW
        );
        final PlayerColor[] selectedPawnColor = {availableColors.getFirst()};

        Label title = new Label("NEW");
        Label title2 = new Label("GAME");
        title.getStyleClass().add("title-label");
        title2.getStyleClass().add("title-label");

        String bgPath = Objects.requireNonNull(getClass().getResource("/view/images/textFieldNewGame.png")).toExternalForm();

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
        fieldContainer.setStyle("-fx-background-image: url('" + bgPath + "'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-background-position: center;");

        Label chooseLabel = new Label("SELECT YOUR PAWN");
        chooseLabel.getStyleClass().add("subtitle-label");

        HBox pawnBox = new HBox(20);
        pawnBox.setAlignment(Pos.CENTER);

        List<Button> pawnButtons = new ArrayList<>();
        for (PlayerColor color : availableColors) {
            Group pawnPreview = PawnController.createPawn(color, PAWN_SIZE_PX);
            pawnPreview.setMouseTransparent(true);

            Button pawnBtn = new Button();
            pawnBtn.setGraphic(pawnPreview);
            pawnBtn.setUserData(color);
            pawnBtn.setMinSize(PAWN_BUTTON_SIZE_PX, PAWN_BUTTON_SIZE_PX);
            pawnBtn.setPrefSize(PAWN_BUTTON_SIZE_PX, PAWN_BUTTON_SIZE_PX);
            pawnBtn.setMaxSize(PAWN_BUTTON_SIZE_PX, PAWN_BUTTON_SIZE_PX);
            pawnBtn.setPadding(new Insets(4));
            pawnBtn.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-cursor: hand; -fx-background-radius: 10;");

            pawnBtn.setOnAction(e -> {
                selectedPawnColor[0] = color;
                for (Button btn : pawnButtons) {
                    if(!btn.isDisable()) {
                        btn.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-cursor: hand; -fx-background-radius: 10;");
                    }
                }
                pawnBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #FFD700; -fx-border-width: 3; -fx-border-radius: 10;");
            });

            pawnButtons.add(pawnBtn);
            pawnBox.getChildren().add(pawnBtn);
        }

        Button buttonStartGame = new Button("next player (1/4)");
        buttonStartGame.getStyleClass().add("bouton_game");

        buttonStartGame.setOnAction(e -> {
            String name = playerName.getText().trim();
            PlayerColor selectedColor = selectedPawnColor[0];

            boolean nameExists = ListPlayer.stream().anyMatch(p -> p.getPseudo().equalsIgnoreCase(name));

            if(name.isEmpty()) {
                System.out.println("Le nom est vide");
                return;
            }
            if(nameExists) {
                System.out.println("Ce nom est déjà pris");
                return;
            }

            if (usedPawnColors.contains(selectedColor)) {
                System.out.println("Ce pion est déjà pris");
                return;
            }

            // Ajout du joueur
            ListPlayer.add(new Player(ListPlayer.size(), name, selectedColor));
            usedPawnColors.add(selectedColor);

            // Désactivation du pion choisi
            for(Button btn : pawnButtons) {
                if(btn.getUserData().equals(selectedColor)) {
                    btn.setDisable(true);
                    btn.setOpacity(0.3);
                    btn.setStyle("-fx-opacity: 0.6;");
                }
            }

            if (ListPlayer.size() < Nb_PLAYERS) {
                playerName.clear();

                // Sélection automatique du prochain pion disponible
                for(Button btn : pawnButtons) {
                    if(!btn.isDisable()) {
                        selectedPawnColor[0] = (PlayerColor) btn.getUserData();
                        btn.setStyle("-fx-background-color: transparent; -fx-border-color: #FFD700; -fx-border-width: 3; -fx-border-radius: 10;");
                        break;
                    }
                }

                if (ListPlayer.size() == Nb_PLAYERS - 1) {
                    buttonStartGame.setText("Start Game");
                } else {
                    buttonStartGame.setText("Next Player (" + (ListPlayer.size() + 1) + "/4)");
                }

            } else {
                // --- Transition vers le jeu ---
                javafx.application.Platform.runLater(() -> {
                    try {
                        root.getChildren().clear();

                        WorldMap worldMap = new WorldMap(true, true, true, Color.web("#f2e1bf"), Color.web("#C5A682"));
                        MapView mapView = MapViewFactory.build(worldMap, true);
                        Group mapInterface = mapView.getRoot();

                        FXMLLoader hudLoader = new FXMLLoader(getClass().getResource("/view/fxml/GameHUD.fxml"));
                        Parent hudVisual = hudLoader.load();
                        GameHUD gameHUD = hudLoader.getController();
                        hudVisual.setPickOnBounds(false);
                        root.getChildren().add(hudVisual);

                        FXMLLoader infoLoader = new FXMLLoader(getClass().getResource("/view/fxml/zoneInfoPanel.fxml"));
                        Parent infoVisual = infoLoader.load();
                        ZoneInfoPanel zoneInfoPanel = infoLoader.getController();
                        infoVisual.setPickOnBounds(false);
                        root.getChildren().add(infoVisual);
                        zoneInfoPanel.hide();

                        GameController gameController = new GameController(zoneInfoPanel, gameHUD, mapView);

                        MapNavigationService mapNavigationService = new MapNavigationService();
                        mapNavigationService.attachNavigation(root, mapInterface);

                        List<Zone> allZones = worldMap.getAllZones();
                        for (int i = 0; i < ListPlayer.size(); i++) {
                            Player p = ListPlayer.get(i);
                            Zone startZone = allZones.get(i * 10);
                            ZoneView startZoneView = mapView.getViewFor(startZone);
                            if (startZoneView == null) continue;

                            Group pawnGroup = PawnController.createPawn(p.getColor(), PAWN_SIZE_PX);
                            pawnGroup.setMouseTransparent(true);

                            Bounds b = startZoneView.getZoneSVGGroup().getBoundsInParent();
                            Bounds pawnBounds = pawnGroup.getBoundsInParent();
                            double pawnCenterX = pawnBounds.getMinX() + pawnBounds.getWidth() / 2.0;
                            double pawnCenterY = pawnBounds.getMinY() + pawnBounds.getHeight() / 2.0;

                            pawnGroup.setTranslateX(b.getCenterX() - pawnCenterX);
                            pawnGroup.setTranslateY(b.getCenterY() - pawnCenterY);
                            mapInterface.getChildren().add(pawnGroup);
                        }

                        worldMap.getAllZones().forEach(zone -> {
                            ZoneView zoneView = mapView.getViewFor(zone);
                            if (zoneView == null) return;

                            zoneView.setPickOnBounds(true);
                            zoneView.setOnMouseClicked(event -> {
                                gameController.showZoneInfo(zone);
                                event.consume();
                            });
                        });

                    } catch (Exception exception) {
                        System.err.println("Error New Game");
                    }
                });
            }
        });

        VBox mainBox = new VBox(0, title, title2, fieldContainer, chooseLabel, pawnBox, buttonStartGame);
        VBox.setMargin(title2, new Insets(-5, 0, 0, 0));
        VBox.setMargin(fieldContainer, new Insets(30, 0, 0, 0));
        VBox.setMargin(chooseLabel, new Insets(40, 0, 10, 0));
        VBox.setMargin(pawnBox, new Insets(10, 0, 0, 0));
        VBox.setMargin(buttonStartGame, new Insets(50, 0, 0, 0));
        mainBox.setAlignment(Pos.TOP_CENTER);
        mainBox.setPickOnBounds(false);
        mainBox.setPadding(new Insets(100, 0, 0, 0));

        root.getChildren().addAll(mainBox, cornerButtons);
        return root;
    }

    private Parent loadGameHudBackground() {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/view/fxml/GameHUD.fxml")));
            return loader.load();

        } catch (Exception e) {
            return null;
        }
    }
}
