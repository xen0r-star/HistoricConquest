package com.historicconquest.historicconquest.ui;

import com.historicconquest.historicconquest.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class HomePage {
    public StackPane root;
    public Pane mapViewport;

    public Button newGameBtn;
    public Button loadGameBtn;
    public Button multiplayerBtn;

    public Button settingsBtn;
    public Button helpBtn;
    public Button exitBtn;

    @FXML
    public void initialize() {
        // Configuration des actions des boutons
        newGameBtn.setOnAction(e -> {
            newGameBtn.setDisable(true);
            MainApp.getInstance().showNewGame();
        });

        loadGameBtn.setOnAction(e -> {
            System.out.println("Load game mode");
        });

        multiplayerBtn.setOnAction(e -> {
            System.out.println("Multiplayer mode");
        });

        settingsBtn.setOnAction(e -> {
            System.out.println("Settings");
        });


        settingsBtn.setOnAction(e -> MainApp.getInstance().showSettings(true));
        helpBtn.setOnAction(    e -> MainApp.getInstance().showHelp(true));
        exitBtn.setOnAction(    e -> MainApp.getInstance().exit());



        // Affichage de la map décoration en arrière-plan
        MapBackgroundDisplay mapDisplay = new MapBackgroundDisplay(
            root, mapViewport,
            0.80, 0.80,
            -55 ,-30, -0.03
        );
        mapDisplay.initialize();
    }
}