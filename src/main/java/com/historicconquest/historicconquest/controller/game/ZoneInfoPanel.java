package com.historicconquest.historicconquest.controller.game;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ZoneInfoPanel {
    @FXML private VBox root;
    @FXML private Label zoneName;

    public VBox getRoot() {
        return root;
    }

    public void setData(String name) {
        this.zoneName.setText(name);
    }

    public void show() {
        root.setVisible(true);
        root.setManaged(true);
    }

    public void hide() {
        root.setVisible(false);
        root.setManaged(false);
    }
}


