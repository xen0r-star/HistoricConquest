package com.historicconquest.historicconquest;

import com.historicconquest.historicconquest.map.Map;
import com.historicconquest.historicconquest.controller.GameController;
import com.historicconquest.historicconquest.controller.MapNavigationService;
import com.historicconquest.historicconquest.ui.GameHUD;
import com.historicconquest.historicconquest.ui.ZoneInfoPanel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Objects;

public class MainApp extends Application {
    private final Group mapInterface = new Group();

    @Override
    public void start(Stage stage) throws Exception {
        // 1. Charger les FXML
        FXMLLoader loaderZoneInfo = new FXMLLoader(getClass().getResource(Constant.PATH + "ui/zoneInfoPanel.fxml"));
        loaderZoneInfo.load();
        ZoneInfoPanel zoneInfoPanel = loaderZoneInfo.getController();

        FXMLLoader loaderGameHUD = new FXMLLoader(getClass().getResource(Constant.PATH + "ui/GameHUD.fxml"));
        StackPane gameHUDRoot = loaderGameHUD.load();
        GameHUD gameHUD = loaderGameHUD.getController();

        // 2. Créer la logique métier
        Map map = new Map();

        // 3. Créer le contrôleur pour orchestrer tout
        new GameController(
            map,
            zoneInfoPanel,
            gameHUD,
            mapInterface
        );

        // 4. Construire l'interface
        StackPane rootLayout = createLayout(gameHUDRoot, zoneInfoPanel);

        // 5. Activer la navigation (zoom & pan)
        MapNavigationService navService = new MapNavigationService();
        navService.attachNavigation(gameHUDRoot, mapInterface);

        // 6. Créer la scène et afficher
        Scene scene = createScene(rootLayout);
        configureStage(stage, scene);
        stage.show();
    }


    private StackPane createLayout(StackPane gameHUDRoot, ZoneInfoPanel zoneInfoPanel) {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setCenter(gameHUDRoot);

        StackPane rootLayout = new StackPane();
        rootLayout.getChildren().add(mainLayout);

        if (zoneInfoPanel != null && zoneInfoPanel.getRoot() != null) {
            StackPane.setAlignment(zoneInfoPanel.getRoot(), Pos.TOP_RIGHT);
            StackPane.setMargin(zoneInfoPanel.getRoot(), new Insets(28, 28, 28, 0));
            rootLayout.getChildren().add(zoneInfoPanel.getRoot());
        }

        return rootLayout;
    }


    private Scene createScene(StackPane rootLayout) {
        Scene scene = new Scene(rootLayout, 800, 800);
        scene.getStylesheets().add(Objects.requireNonNull(
            getClass().getResource(Constant.PATH + "styles/style.css")
        ).toExternalForm());
        return scene;
    }

    private void configureStage(Stage stage, Scene scene) {
        stage.getIcons().addAll(
            loadImage("images/icon512.png"),
            loadImage("images/icon256.png"),
            loadImage("images/icon128.png"),
            loadImage("images/icon64.png"),
            loadImage("images/icon32.png")
        );
        stage.setTitle("Historic Conquest");
        stage.setMaximized(true);
        stage.setScene(scene);
    }

    private Image loadImage(String path) {
        return new Image(Objects.requireNonNull(
            getClass().getResourceAsStream(Constant.PATH + path)
        ));
    }


    public static void main(String[] args) {
        launch(args);
    }
}
