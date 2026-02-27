package com.historicconquest.historicconquest;

import com.historicconquest.historicconquest.map.Map;
import com.historicconquest.historicconquest.controller.MapNavigationService;
import com.historicconquest.historicconquest.ui.ZoneInfoPanel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainApp extends Application {
    private final Group mapInterface = new Group();
    private Map map;

    @Override
    public void start(Stage stage) {
        // 0. Chargement du panneau d'informations de zone
        ZoneInfoPanel zoneInfoPanel = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constant.PATH + "ui/zoneInfoPanel.fxml"));
            loader.load();
            zoneInfoPanel = loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 1. Chargement de la Map via le service dédié
        map = new Map(zoneInfoPanel);
        mapInterface.getChildren().addAll(map.getBlocs());


        // 2. Configuration du BorderPane principal
        BorderPane mainLayout = new BorderPane();

        // Centre : Map avec la grille et les overlays
        StackPane mapContainer = new StackPane();
        mapContainer.setAlignment(Pos.CENTER);

        Region grid = new Region();
        grid.getStyleClass().add("map-grid");
        StackPane.setMargin(grid, new Insets(28, 28, 28, 28));
        mapContainer.getChildren().add(grid);

        // Viewport avec clipping
        Pane viewport = new Pane();
        StackPane.setMargin(viewport, new Insets(28, 28, 28, 28));
        viewport.getChildren().add(mapInterface);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(viewport.widthProperty());
        clip.heightProperty().bind(viewport.heightProperty());
        viewport.setClip(clip);
        mapContainer.getChildren().add(viewport);

        // Overlays
        Region overlayInside = new Region();
        overlayInside.getStyleClass().add("map-overlay-inside");
        overlayInside.setMouseTransparent(true);
        StackPane.setMargin(overlayInside, new Insets(20, 20, 20, 20));

        Region overlayMiddle = new Region();
        overlayMiddle.getStyleClass().add("map-overlay-middle");
        overlayMiddle.setMouseTransparent(true);
        StackPane.setMargin(overlayMiddle, new Insets(20, 20, 20, 20));

        Region overlayOutside = new Region();
        overlayOutside.getStyleClass().add("map-overlay-outside");
        overlayOutside.setMouseTransparent(true);
        StackPane.setMargin(overlayOutside, new Insets(28, 28, 28, 28));

        mapContainer.getChildren().addAll(overlayInside, overlayMiddle, overlayOutside);

        mainLayout.setCenter(mapContainer);

        // Droite : Panneau d'informations
        if (zoneInfoPanel != null && zoneInfoPanel.getRoot() != null) {
            mainLayout.setRight(zoneInfoPanel.getRoot());
            BorderPane.setMargin(zoneInfoPanel.getRoot(), new Insets(28, 28, 28, 0));
        }

        // 3. Activation de la navigation (Zoom & Pan)
        MapNavigationService navService = new MapNavigationService();
        navService.attachNavigation(mapContainer, mapInterface);


        // 4. Lancement de la fenêtre
        Scene scene = new Scene(mainLayout, 800, 800);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(
            Constant.PATH + "styles/style.css"
        )).toExternalForm());

        stage.getIcons().addAll(
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(Constant.PATH + "images/icon512.png"))),
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(Constant.PATH + "images/icon256.png"))),
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(Constant.PATH + "images/icon128.png"))),
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(Constant.PATH + "images/icon64.png"))),
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(Constant.PATH + "images/icon32.png")))
        );
        stage.setTitle("Historic Conquest");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
