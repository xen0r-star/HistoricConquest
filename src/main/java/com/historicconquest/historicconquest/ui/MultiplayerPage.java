package com.historicconquest.historicconquest.ui;

import com.historicconquest.historicconquest.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class MultiplayerPage {
    public StackPane root;
    public Pane mapViewport;

    public Pane JoinBtn;
    public Pane HostBtn;
    public Button BackBtn;

    @FXML
    public void initialize() {
        JoinBtn.setOnMouseClicked(e -> {
            System.out.println("Join button clicked");
        });

        HostBtn.setOnMouseClicked(e -> {
            System.out.println("Host button clicked");
        });


        BackBtn.setOnAction(e -> {
            BackBtn.setDisable(true);
            MainApp.getInstance().showMenu();
        });



        // Affichage de la map décoration en arrière-plan
        MapBackgroundDisplay mapDisplay = MapBackgroundDisplay.getInstance();
        mapDisplay.setTransformations(0.8, 0.8, 100);
        mapDisplay.display(root, mapViewport);
    }
}