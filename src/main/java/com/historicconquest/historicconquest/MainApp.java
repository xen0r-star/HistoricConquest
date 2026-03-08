package com.historicconquest.historicconquest;

import com.historicconquest.historicconquest.map.WorldMap;
import com.historicconquest.historicconquest.controller.GameController;
import com.historicconquest.historicconquest.controller.MapNavigationService;
import com.historicconquest.historicconquest.ui.GameHUD;
import com.historicconquest.historicconquest.ui.ZoneInfoPanel;
import com.historicconquest.historicconquest.ui.HomePage;
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

    private Stage stage;
    private StackPane appRoot;      // <-- root unique de l'app sinon en cree plusieurs

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        configureStageIcons(stage);

        // 1) Root et Scene créés UNE SEULE FOIS
        appRoot = new StackPane();
        // <-- scene unique de l'app
        Scene scene = new Scene(appRoot, 1280, 720);

        // CSS UNE SEULE FOIS
        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource(Constant.PATH + "styles/style.css")
        ).toExternalForm());

        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();

        // 2) Afficher le menu en remplaçant le contenu du root
        showMenu();
    }

    public void showMenu() {
        HomePage menu = new HomePage();
        StackPane menuRoot = menu.createView(this);   // <-- plus de stage ici
        appRoot.getChildren().setAll(menuRoot);
    }

    public void startGame() {
        try {
            FXMLLoader loaderZoneInfo = new FXMLLoader(getClass().getResource(Constant.PATH + "ui/zoneInfoPanel.fxml"));
            loaderZoneInfo.load();
            ZoneInfoPanel zoneInfoPanel = loaderZoneInfo.getController();

            FXMLLoader loaderGameHUD = new FXMLLoader(getClass().getResource(Constant.PATH + "ui/GameHUD.fxml"));
            StackPane gameHUDRoot = loaderGameHUD.load();
            GameHUD gameHUD = loaderGameHUD.getController();

            WorldMap worldMap = new WorldMap();
            new GameController(worldMap, zoneInfoPanel, gameHUD, mapInterface);

            StackPane rootLayout = createLayout(gameHUDRoot, zoneInfoPanel);

            MapNavigationService navService = new MapNavigationService();
            navService.attachNavigation(gameHUDRoot, mapInterface);

            // IMPORTANT : on ne touche plus à stage.setScene(...)
            appRoot.getChildren().setAll(rootLayout);

            // optionnel
            stage.setFullScreen(true);
            stage.setMaximized(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configureStageIcons(Stage stage) {
        stage.getIcons().addAll(
                loadImage("images/icon512.png"),
                loadImage("images/icon256.png"),
                loadImage("images/icon128.png"),
                loadImage("images/icon64.png"),
                loadImage("images/icon32.png")
        );
        stage.setTitle("Historic Conquest");
    }

    private StackPane createLayout(StackPane gameHUDRoot, ZoneInfoPanel zoneInfoPanel) {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setCenter(gameHUDRoot);

        StackPane rootLayout = new StackPane(mainLayout);

        if (zoneInfoPanel != null && zoneInfoPanel.getRoot() != null) {
            StackPane.setAlignment(zoneInfoPanel.getRoot(), Pos.TOP_RIGHT);
            StackPane.setMargin(zoneInfoPanel.getRoot(), new Insets(28, 28, 28, 0));
            rootLayout.getChildren().add(zoneInfoPanel.getRoot());
        }

        return rootLayout;
    }

    private Image loadImage(String path) {
        return new Image(Objects.requireNonNull(
                getClass().getResourceAsStream(Constant.PATH + path)
        ));
    }

    public Stage getStage() {
        return stage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}