package com.historicconquest.historicconquest;

import com.historicconquest.historicconquest.controller.NotificationController;
import com.historicconquest.historicconquest.map.WorldMap;
import com.historicconquest.historicconquest.controller.GameController;
import com.historicconquest.historicconquest.controller.MapNavigationService;
import com.historicconquest.historicconquest.game.NewGameConfig;
import com.historicconquest.historicconquest.ui.GameHUD;
import com.historicconquest.historicconquest.controller.HelpController;
import com.historicconquest.historicconquest.ui.NewGame;
import com.historicconquest.historicconquest.ui.ZoneInfoPanel;
import com.historicconquest.historicconquest.controller.SettingsController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Objects;

public class MainApp extends Application {
    private final Group mapInterface = new Group();

    private Stage stage;
    private static StackPane appRoot;
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


        HelpController.initialize();
        SettingsController.initialize();
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

            setAppContent(homePageRoot);

        } catch (Exception e) {
            System.err.println("Error loading home page");
        }
    }

    public void showNewGame() {
        NewGame page = new NewGame();
        setAppContent(page.createView(this));
    }

    public void showMultiplayer() {
        try {
            FXMLLoader loaderMultiplayerPage = new FXMLLoader(getClass().getResource(Constant.PATH + "ui/MultiplayerPage.fxml"));
            StackPane multiplayerPageRoot = loaderMultiplayerPage.load();

            setAppContent(multiplayerPageRoot);

        } catch (Exception e) {
            System.err.println("Error loading multiplayer page");
        }
    }



    public void showSettings(boolean show) {
        StackPane settings = SettingsController.getSettings();
        if (settings == null) return;

        addOverlay(settings);
        if (show) {
            SettingsController.show();
            settings.toFront();
            return;
        }

        SettingsController.close();
    }

    public void showHelp(boolean show) {
        StackPane help = HelpController.getHelp();
        if (help == null) return;

        addOverlay(help);
        if (show) {
            HelpController.show();
            help.toFront();
            return;
        }

        HelpController.close();
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

            setAppContent(rootLayout);

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

    private void setAppContent(Node content) {
        appRoot.getChildren().setAll(content);

        addOverlay(SettingsController.getSettings());
        addOverlay(HelpController.getHelp());
        addOverlay(NotificationController.getNotifications());
    }

    private void addOverlay(Node overlay) {
        if (overlay == null) return;

        if (overlay.getParent() instanceof Pane parent && parent != appRoot) {
            parent.getChildren().remove(overlay);
        }

        if (!appRoot.getChildren().contains(overlay)) {
            appRoot.getChildren().add(overlay);
        }
    }



    public static MainApp getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        launch(args);
    }
}