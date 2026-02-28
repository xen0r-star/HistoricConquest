package com.historicconquest.historicconquest.ui;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

public class GameHUD {
    @FXML private Pane mapViewport;

    public void initializeMap(Group mapInterface) {
        if (mapViewport != null) {
            mapViewport.getChildren().add(mapInterface);

            // Map cutting
            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(mapViewport.widthProperty());
            clip.heightProperty().bind(mapViewport.heightProperty());
            mapViewport.setClip(clip);
        }
    }
}