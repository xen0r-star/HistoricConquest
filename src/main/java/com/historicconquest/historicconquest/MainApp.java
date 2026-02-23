package com.historicconquest.historicconquest;

import com.historicconquest.historicconquest.map.Map;
import com.historicconquest.historicconquest.controller.MapNavigationService;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Objects;

public class MainApp extends Application {
    private final Group mapInterface = new Group();
    private Map map;

    @Override
    public void start(Stage stage) {
        // 1. Chargement de la Map via le service dédié
        map = new Map();
        mapInterface.getChildren().addAll(map.getBlocs());


        // 2. Configuration du conteneur racine
        StackPane root = new StackPane();
        root.setAlignment(Pos.CENTER);

        Region grid = new Region();
        grid.getStyleClass().add("map-grid");
        StackPane.setMargin(grid, new Insets(28, 28, 28, 28));
        root.getChildren().add(grid);

        root.getChildren().add(mapInterface);

        // Border overlay
        Region overlayInside = new Region();
        overlayInside.getStyleClass().add("map-overlay-inside");
        StackPane.setMargin(overlayInside, new Insets(20, 20, 20, 20));

        Region overlayMiddle = new Region();
        overlayMiddle.getStyleClass().add("map-overlay-middle");
        StackPane.setMargin(overlayMiddle, new Insets(20, 20, 20, 20));

        Region overlayOutside = new Region();
        overlayOutside.getStyleClass().add("map-overlay-outside");
        StackPane.setMargin(overlayOutside, new Insets(28, 28, 28, 28));

        root.getChildren().add(overlayInside);
        root.getChildren().add(overlayMiddle);
        root.getChildren().add(overlayOutside);


        // 3. Activation de la navigation (Zoom & Pan)
        MapNavigationService navService = new MapNavigationService();
        navService.attachNavigation(root, mapInterface);


        // 4. Lancement de la fenêtre
        Scene scene = new Scene(root, 800, 800);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(
            "/com/historicconquest/historicconquest/styles/style.css"
        )).toExternalForm());

        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream(
            "/com/historicconquest/historicconquest/images/icon.png"
        )));

        stage.getIcons().add(icon);
        stage.setTitle("Historic Conquest");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
