package com.historicconquest.historicconquest.app;

import com.historicconquest.historicconquest.controller.core.AppController;
import com.historicconquest.historicconquest.controller.core.AppPage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Objects;

public class App extends Application {
    private final AppController appController = new AppController();


    @Override
    public void start(Stage stage) {
        stage.setTitle("Historic Conquest");
        stage.getIcons().add(new Image(Objects.requireNonNull(
            getClass().getResourceAsStream("/view/images/icon.png")
        )));
        stage.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) AppController.getInstance().showPauseGame(true);
        });


        StackPane root = new StackPane();
        Scene scene = new Scene(root, 1280, 720);

        scene.getStylesheets().add(Objects.requireNonNull(
            getClass().getResource("/view/styles/style.css")
        ).toExternalForm());

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) AppController.getInstance().showPauseGame(true);
        });

        appController.initialize(stage, root);
        appController.showPage(AppPage.HOME);

        Platform.setImplicitExit(true);
        stage.setOnCloseRequest(event -> {
            event.consume();
            appController.exit();
        });

        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }
}