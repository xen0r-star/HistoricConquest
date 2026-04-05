package com.historicconquest.historicconquest.view;

import com.historicconquest.historicconquest.app.App;
import com.historicconquest.historicconquest.app.AppPage;
import com.historicconquest.historicconquest.controller.GameController;
import com.historicconquest.historicconquest.controller.MapBackgroundController;
import com.historicconquest.historicconquest.controller.PawnController;
import com.historicconquest.historicconquest.model.game.Game;
import com.historicconquest.historicconquest.model.map.WorldMap;
import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.model.player.PlayerColor;
import com.historicconquest.historicconquest.service.map.MapNavigationService;
import com.historicconquest.historicconquest.util.NameGenerator;
import com.historicconquest.historicconquest.view.map.MapView;
import com.historicconquest.historicconquest.view.map.MapViewFactory;
import com.historicconquest.historicconquest.view.map.ZoneView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.List;

public class NewGame {
    private static final int NB_PLAYERS = 4;

    @FXML private StackPane root;
    @FXML private Pane mapViewport;
    @FXML private GridPane ColorContainer;

    @FXML private Label StepLabel, NameError, PawnError, TeamColorLabel;
    @FXML private TextField NameTextField;
    @FXML private StackPane HorseDrawnImage, ShipImage;

    @FXML private Button BackBtn, NextPlayer, RandomName;
    @FXML private Circle Color1, Color2, Color3, Color4, Color5, Color6, Color7, Color8, Color9;

    private final List<Player> listPlayer = new ArrayList<>();
    private final HashSet<PlayerColor> usedPawnColors = new HashSet<>();
    private final Map<Circle, PlayerColor> circleColorMap = new LinkedHashMap<>();
    private final Map<PlayerColor, Circle> colorCircleMap = new LinkedHashMap<>();
    private PlayerColor selectedPawnColor;


    @FXML
    public void initialize() {
        NameError.setVisible(false);
        NameError.setManaged(false);
        PawnError.setVisible(false);
        PawnError.setManaged(false);

        BackBtn.setOnAction(e -> App.getInstance().showPage(AppPage.HOME));
        RandomName.setOnAction(e -> NameTextField.setText(NameGenerator.get()));
        NextPlayer.setOnAction(e -> handleNextPlayer());


        configurePreviewContainer(HorseDrawnImage, 130);
        configurePreviewContainer(ShipImage, 100.0);

        MapBackgroundController.show(root, mapViewport, -55, -30, -0.03);
        initColorSelection();
        updateStepLabel();
    }

    private void initColorSelection() {
        if (ColorContainer == null) return;

        circleColorMap.clear();
        colorCircleMap.clear();

        bindCircle(Color1, PlayerColor.RED);
        bindCircle(Color2, PlayerColor.ORANGE);
        bindCircle(Color3, PlayerColor.YELLOW);
        bindCircle(Color4, PlayerColor.LIME);
        bindCircle(Color5, PlayerColor.GREEN);
        bindCircle(Color6, PlayerColor.LIGHT_BLUE);
        bindCircle(Color7, PlayerColor.BLUE);
        bindCircle(Color8, PlayerColor.PINK);
        bindCircle(Color9, PlayerColor.PURPLE);

        circleColorMap.keySet().stream().findFirst()
            .ifPresent(firstCircle -> selectColorCircle(firstCircle, circleColorMap.get(firstCircle)));
    }

    private void bindCircle(Circle circle, PlayerColor color) {
        if (circle == null) return;

        circleColorMap.put(circle, color);
        colorCircleMap.put(color, circle);

        circle.setStyle("-fx-cursor: hand;");
        circle.setStrokeWidth(0.0);
        circle.setOnMouseClicked(e -> {
            if (circle.isDisable()) return;
            selectColorCircle(circle, color);
        });
    }

    private void selectColorCircle(Circle selectedCircle, PlayerColor color) {
        selectedPawnColor = color;
        hidePawnError();

        for (Circle circle : circleColorMap.keySet()) {
            if (!circle.isDisable()) {
                circle.setStrokeWidth(0.0);
                circle.setStroke(Color.TRANSPARENT);
                circle.setStyle("-fx-cursor: hand;");
            }
        }

        if (!selectedCircle.isDisable()) {
            selectedCircle.setStroke(Color.web("#636363"));
            selectedCircle.setStrokeWidth(3.0);
        }

        if (TeamColorLabel != null) {
            TeamColorLabel.setText(formatColorName(color) + " Team");
        }

        refreshPawnPreviews();
    }

    private void refreshPawnPreviews() {
        if (selectedPawnColor == null) return;

        setPreview(HorseDrawnImage, PawnController.createHorsePawn(selectedPawnColor, 122.0));
        setPreview(ShipImage, PawnController.createShipPawn(selectedPawnColor, 95.0));
    }


    private void setPreview(StackPane container, Group pawnPreview) {
        if (container == null) return;

        container.getChildren().clear();

        if (pawnPreview == null) return;

        Bounds b = pawnPreview.getLayoutBounds();
        pawnPreview.setTranslateX(-b.getMinX() - b.getWidth() / 2.0);
        pawnPreview.setTranslateY(-b.getMinY() - b.getHeight() / 2.0);

        pawnPreview.setMouseTransparent(true);
        Group centeredPreview = new Group(pawnPreview);

        StackPane.setAlignment(centeredPreview, Pos.CENTER);
        container.getChildren().add(centeredPreview);
    }

    private void configurePreviewContainer(StackPane container, double width) {
        container.setMinSize(width, 70.0);
        container.setPrefSize(width, 70.0);
        container.setMaxSize(width, 70.0);

        Rectangle clip = new Rectangle(width, 70.0);
        clip.widthProperty().bind(container.widthProperty());
        clip.heightProperty().bind(container.heightProperty());
        container.setClip(clip);
    }


    private void handleNextPlayer() {
        String name = NameTextField.getText().trim();

        if (name.isEmpty()) {
            showNameError("Name is required.");
            return;
        }

        boolean nameExists = listPlayer.stream().anyMatch(p -> p.getPseudo().equalsIgnoreCase(name));

        if (nameExists) {
            showNameError("This name is already taken.");
            return;
        }

        if (usedPawnColors.contains(selectedPawnColor)) {
            showPawnError();
            return;
        }

        hideNameError();
        hidePawnError();

        listPlayer.add(new Player(listPlayer.size(), name, selectedPawnColor));
        usedPawnColors.add(selectedPawnColor);
        disableSelectedColorCircle(selectedPawnColor);

        if (listPlayer.size() < NB_PLAYERS) {
            NameTextField.clear();
            autoSelectNextAvailableColor();
            updateStepLabel();
            return;
        }

        launchGame();
    }

    private void disableSelectedColorCircle(PlayerColor color) {
        Circle circle = colorCircleMap.get(color);
        if (circle == null) return;

        circle.setDisable(true);
        circle.setStrokeWidth(0.0);
        circle.setStroke(Color.TRANSPARENT);
        circle.setStyle("-fx-opacity: 0.35;");
    }

    private void autoSelectNextAvailableColor() {
        for (Circle circle : circleColorMap.keySet()) {
            if (!circle.isDisable()) {
                selectColorCircle(circle, circleColorMap.get(circle));
                return;
            }
        }
    }

    private void updateStepLabel() {
        int nextIndex = listPlayer.size() + 1;
        StepLabel.setText("Player (" + nextIndex + " / " + NB_PLAYERS + ")");

        if (listPlayer.size() == NB_PLAYERS - 1) {
            NextPlayer.setText("Start Game");

        } else {
            NextPlayer.setText("Next Player");
        }
    }

    private void launchGame() {
        Platform.runLater(() -> {
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
                for (int i = 0; i < listPlayer.size(); i++) {
                    Player player = listPlayer.get(i);
                    Zone startZone = allZones.get(i * 10);
                    ZoneView startZoneView = mapView.getViewFor(startZone);
                    if (startZoneView == null) continue;

                    Group pawnGroup = PawnController.createPawn(player.getColor(), 40.0);
                    pawnGroup.setMouseTransparent(true);

                    Bounds zoneBounds = startZoneView.getZoneSVGGroup().getBoundsInParent();
                    Bounds pawnBounds = pawnGroup.getBoundsInParent();
                    double pawnCenterX = pawnBounds.getMinX() + pawnBounds.getWidth() / 2.0;
                    double pawnCenterY = pawnBounds.getMinY() + pawnBounds.getHeight() / 2.0;

                    pawnGroup.setTranslateX(zoneBounds.getCenterX() - pawnCenterX);
                    pawnGroup.setTranslateY(zoneBounds.getCenterY() - pawnCenterY);
                    mapInterface.getChildren().add(pawnGroup);

                    player.setCurrentZone(startZone);
                    player.setPawnNode(pawnGroup);
                }

                Game gameEngine = new Game(listPlayer, worldMap, gameController);
                worldMap.getAllZones().forEach(zone -> {
                    ZoneView zoneView = mapView.getViewFor(zone);
                    if (zoneView == null) return;

                    zoneView.setPickOnBounds(true);
                    zoneView.setOnMouseClicked(event -> {
                        gameEngine.handleZoneSelection(zone);
                        event.consume();
                    });
                });

            } catch (Exception exception) {
                System.err.println("Error New Game");
            }
        });
    }

    private void showNameError(String message) {
        NameError.setText(message);
        NameError.setVisible(true);
        NameError.setManaged(true);
    }

    private void hideNameError() {
        NameError.setVisible(false);
        NameError.setManaged(false);
    }

    private void showPawnError() {
        PawnError.setText("This color is already taken.");
        PawnError.setVisible(true);
        PawnError.setManaged(true);
    }

    private void hidePawnError() {
        PawnError.setVisible(false);
        PawnError.setManaged(false);
    }

    private String formatColorName(PlayerColor color) {
        return switch (color) {
            case RED -> "Red";
            case ORANGE -> "Orange";
            case YELLOW -> "Yellow";
            case GREEN -> "Green";
            case LIME -> "Lime";
            case CYAN -> "Cyan";
            case BLUE -> "Blue";
            case LIGHT_BLUE -> "Light Blue";
            case PURPLE -> "Purple";
            case PINK -> "Pink";
        };
    }
}
