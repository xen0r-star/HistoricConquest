package com.historicconquest.historicconquest;

import interfaceService.MapNavigationService;
import interfaceService.MapLoaderService; // Nouveau service
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {

    private final Group plateauGlobal = new Group();
    private final List<Zone> toutesLesZones = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        // 1. Chargement de la Map via le service dédié
        MapLoaderService loader = new MapLoaderService();
        loader.chargerMap("/com/historicconquest/historicconquest/zones/map_config.json", plateauGlobal, toutesLesZones);

        // 2. Configuration du conteneur racine
        StackPane root = new StackPane(plateauGlobal);
        root.setAlignment(Pos.TOP_LEFT);
        root.setStyle("-fx-background-color: #2b2b2b;");

        // 3. Activation de la navigation (Zoom & Pan)
        MapNavigationService navService = new MapNavigationService();
        navService.attachNavigation(root, plateauGlobal);

        // 4. Lancement de la fenêtre
        Scene scene = new Scene(root, 1300, 850);
        stage.setTitle("23/02/2026");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
