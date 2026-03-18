package com.historicconquest.historicconquest;

import com.historicconquest.historicconquest.map.WorldMap;
import com.historicconquest.historicconquest.controller.GameController;
import com.historicconquest.historicconquest.controller.MapNavigationService;
import com.historicconquest.historicconquest.game.NewGameConfig;
import com.historicconquest.historicconquest.ui.GameHUD;
import com.historicconquest.historicconquest.ui.HelpPage;
import com.historicconquest.historicconquest.ui.NewGame;
import com.historicconquest.historicconquest.ui.ZoneInfoPanel;
import com.historicconquest.historicconquest.ui.PageSettings;
import com.historicconquest.historicconquest.ui.PageSettingsController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Objects;

public class MainApp extends Application {

    private final Group mapInterface = new Group();

    private Stage stage;
    private static StackPane appRoot;
    private Parent helpPageRoot;
    private Parent settingsPageRoot ;
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

        stage.setScene(scene);
        // stage.setFullScreen(true);
        stage.setMaximized(true);
        stage.show();

        showMenu();

        loadHelpPage();
    }

    public static void showMenu() {
        try {
            FXMLLoader loaderHomePage = new FXMLLoader(MainApp.class.getResource(Constant.PATH + "ui/HomePage.fxml"));
            StackPane homePageRoot = loaderHomePage.load();

            appRoot.getChildren().setAll(homePageRoot);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void showNewGame() {
        NewGame page = new NewGame();
        appRoot.getChildren().setAll(page.createView(this));
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

            helpPageRoot = HelpPage.getHelpStackPane();
            appRoot.setAlignment(helpPageRoot, Pos.TOP_RIGHT);
            appRoot.setMargin(helpPageRoot, new Insets(30, 30, 0, 0));
            appRoot.getChildren().add(helpPageRoot);

        } catch (Exception e) {
            System.err.println("Error loading help page: " + e.getMessage());
        }
    }

    // Dans MainApp.java
    public void showHelp(boolean show, StackPane currentRoot) {
        if (helpPageRoot == null) loadHelpPage();

        if (show) {
            //pour afficher dans le root newgame et pas dans le root homepage
            if (!currentRoot.getChildren().contains(helpPageRoot)) {
                currentRoot.getChildren().add(helpPageRoot);
            }
            helpPageRoot.toFront(); // On la met tout devant
        }

        helpPageRoot.setVisible(show);
        helpPageRoot.setManaged(show);
    }


    private Parent loadSettingsPage() {
        // On appelle ta classe PageSettings qui charge le FXML
        return PageSettings.getSettingsStackPane();
    }


    public void showSettings(boolean show, StackPane currentRoot) {
        // 1. Charger les paramètres s'ils ne le sont pas encore
        if (settingsPageRoot == null) {
            settingsPageRoot = loadSettingsPage();
        }

        if (show && settingsPageRoot != null && currentRoot != null) {
            // 2. Nettoyer le parent précédent (si on change de vue)
            if (settingsPageRoot.getParent() != null && settingsPageRoot.getParent() != currentRoot) {
                ((StackPane) settingsPageRoot.getParent()).getChildren().remove(settingsPageRoot);
            }

            // 3. Ajouter au root actuel s'il n'y est pas
            if (!currentRoot.getChildren().contains(settingsPageRoot)) {
                currentRoot.getChildren().add(settingsPageRoot);
            }

            // 4. Mettre tout devant
            settingsPageRoot.toFront();
        }

        // 5. Afficher ou masquer
        if (settingsPageRoot != null) {
            settingsPageRoot.setVisible(show);
            settingsPageRoot.setManaged(show);
        }
    }


    public static StackPane getSettingsStackPane() {
        try {
            FXMLLoader loader = new FXMLLoader(PageSettings.class.getResource("/com/historicconquest/historicconquest/ui/SettingsPage.fxml"));
            StackPane root = loader.load();

            // Récupérer le contrôleur pour lui donner le Stage principal
            PageSettingsController controller = loader.getController();
            controller.setStage(MainApp.getInstance().getStage());

            return root;
        } catch (Exception e) {
            e.printStackTrace();
            return new StackPane();
        }
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