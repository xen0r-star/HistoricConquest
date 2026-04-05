package com.historicconquest.historicconquest.app;

import com.historicconquest.historicconquest.controller.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Objects;

public class App extends Application {
    private Stage stage;
    private static StackPane root;
    private static App instance;


    public App() {
        instance = this;
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        stage.setTitle("Historic Conquest");
        stage.getIcons().add(new Image(Objects.requireNonNull(
            getClass().getResourceAsStream("/view/images/icon.png")
        )));


        root = new StackPane();
        Scene scene = new Scene(root, 1280, 720);

        scene.getStylesheets().add(Objects.requireNonNull(
            getClass().getResource("/view/styles/style.css")
        ).toExternalForm());


        HelpController.initialize();
        SettingsController.initialize();
        NotificationController.initialize();

        MapBackgroundController.initialize();


        showPage(AppPage.HOME);

        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }


    public void showPage(AppPage page) {
        switch (page) {
            case HOME -> {
                try {
                    FXMLLoader loaderHomePage = new FXMLLoader(getClass().getResource("/view/fxml/HomePage.fxml"));
                    StackPane homePageRoot = loaderHomePage.load();

                    setAppContent(homePageRoot);

                } catch (Exception e) {
                    System.err.println("Error loading home page");
                }
            }

            case NEW_GAME -> {
                try {
                    FXMLLoader loaderNewGamePage = new FXMLLoader(getClass().getResource("/view/fxml/NewGame.fxml"));
                    StackPane newGamePageRoot = loaderNewGamePage.load();

                    setAppContent(newGamePageRoot);

                } catch (Exception e) {
                    System.err.println("Error loading new game page");
                }
            }

            case MULTIPLAYER -> {
                try {
                    FXMLLoader loaderMultiplayerPage = new FXMLLoader(getClass().getResource("/view/fxml/MultiplayerPage.fxml"));
                    StackPane multiplayerPageRoot = loaderMultiplayerPage.load();

                    setAppContent(multiplayerPageRoot);

                } catch (Exception e) {
                    System.err.println("Error loading multiplayer page");
                }
            }
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





    private void setAppContent(Node content) {
        root.getChildren().setAll(content);

        addOverlay(SettingsController.getSettings());
        addOverlay(HelpController.getHelp());
        addOverlay(NotificationController.getNotifications());
    }

    private void addOverlay(Node overlay) {
        if (overlay == null) return;

        if (overlay.getParent() instanceof Pane parent && parent != root) {
            parent.getChildren().remove(overlay);
        }

        if (!root.getChildren().contains(overlay)) {
            root.getChildren().add(overlay);
        }
    }

    public void exit() {
        stage.close();
    }



    public static App getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        launch(args);
    }
}