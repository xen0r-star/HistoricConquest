package com.historicconquest.historicconquest;

import com.historicconquest.historicconquest.map.WorldMap;
import com.historicconquest.historicconquest.controller.GameController;
import com.historicconquest.historicconquest.controller.MapNavigationService;
import com.historicconquest.historicconquest.controller.NotificationController;
import com.historicconquest.historicconquest.game.NewGameConfig;
import com.historicconquest.historicconquest.ui.GameHUD;
import com.historicconquest.historicconquest.ui.NewGame;
import com.historicconquest.historicconquest.ui.ZoneInfoPanel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Objects;

public class MainApp extends Application {

    private final Group mapInterface = new Group();

    private Stage stage;
    private StackPane appRoot;
    private Parent helpPageRoot;
    private static MainApp instance;


    public MainApp() {
        instance = this;
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        stage.setTitle("Historic Conquest");
        stage.getIcons().addAll(
            loadImage("images/icon512.png"),
            loadImage("images/icon256.png"),
            loadImage("images/icon128.png"),
            loadImage("images/icon64.png"),
            loadImage("images/icon32.png")
        );

        appRoot = new StackPane();
        Scene scene = new Scene(appRoot, 1280, 720);

        scene.getStylesheets().add(Objects.requireNonNull(
            getClass().getResource(Constant.PATH + "styles/style.css")
        ).toExternalForm());


        loadHelpPage();
        NotificationController.initialize();

        showMenu();


        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public void showMenu() {
        try {
            FXMLLoader loaderHomePage = new FXMLLoader(getClass().getResource(Constant.PATH + "ui/HomePage.fxml"));
            StackPane homePageRoot = loaderHomePage.load();

            appRoot.getChildren().setAll(homePageRoot);
            showNotification();

        } catch (Exception e) {
            System.err.println("Error loading home page");
        }
    }

    public void showNewGame() {
        NewGame page = new NewGame();
        appRoot.getChildren().setAll(page.createView(this));
        showNotification();
    }

    public void showMultiplayer() {
        try {
            FXMLLoader loaderMultiplayerPage = new FXMLLoader(getClass().getResource(Constant.PATH + "ui/MultiplayerPage.fxml"));
            StackPane multiplayerPageRoot = loaderMultiplayerPage.load();

            appRoot.getChildren().setAll(multiplayerPageRoot);
            showNotification();

        } catch (Exception e) {
            System.err.println("Error loading multiplayer page");
        }
    }

    public void startGame(NewGameConfig config) {
        try {
            FXMLLoader loaderZoneInfo = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource(Constant.PATH + "ui/zoneInfoPanel.fxml")
            ));
            loaderZoneInfo.load();
            ZoneInfoPanel zoneInfoPanel = loaderZoneInfo.getController();

            FXMLLoader loaderGameHUD = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource(Constant.PATH + "ui/GameHUD.fxml")
            ));
            StackPane gameHUDRoot = loaderGameHUD.load();
            GameHUD gameHUD = loaderGameHUD.getController();

            // Tu peux utiliser config ici si tu veux
            // ex: gameHUD.setPlayerName(config.getPlayerName());

            WorldMap worldMap = new WorldMap();
            new GameController(worldMap, zoneInfoPanel, gameHUD, mapInterface);

            StackPane rootLayout = createLayout(gameHUDRoot, zoneInfoPanel);

            MapNavigationService navService = new MapNavigationService();
            navService.attachNavigation(gameHUDRoot, mapInterface);

            appRoot.getChildren().setAll(rootLayout);
            showNotification();

        } catch (Exception e) {
            System.err.println("Error start game");
            showMenu();
        }
    }

    // si tu veux garder un appel sans config
    public void startGame() {
        startGame(new NewGameConfig("Player", 1));
    }

    public void exit() {
        stage.close();
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

    private void loadHelpPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constant.PATH + "ui/HelpPage.fxml"));
            helpPageRoot = loader.load();

            helpPageRoot.setVisible(false);
            helpPageRoot.setManaged(false);

            appRoot.getChildren().add(helpPageRoot);

        } catch (Exception e) {
            System.err.println("Error loading help page: " + e.getMessage());
        }
    }

    public void showHelp(boolean show) {
        if (helpPageRoot == null) loadHelpPage();
        helpPageRoot.setVisible(show);
        helpPageRoot.setManaged(show);
    }

    public void showNotification() {
        appRoot.getChildren().add(NotificationController.getNotificationsHost());
    }

    public Stage getStage() {
        return stage;
    }

    public static MainApp getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        launch(args);
    }
}