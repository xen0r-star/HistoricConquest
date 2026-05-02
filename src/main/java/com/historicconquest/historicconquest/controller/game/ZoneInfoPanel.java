package com.historicconquest.historicconquest.controller.game;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class ZoneInfoPanel {
    @FXML public AnchorPane root;
    @FXML public Button CloseBtn;
    @FXML public Label ThemeLabel;
    @FXML public Label BlockLabel;
    @FXML public Label OwnerLabel;
    @FXML public Label PowerLabel ;
    @FXML public Label ZoneNameLabel;


    private static ZoneInfoPanel instance;


    public static ZoneInfoPanel getInstance() {
        return instance;
    }

    public static void setInstance(ZoneInfoPanel instance) {
        ZoneInfoPanel.instance = instance;
    }

    public AnchorPane getRoot() {
        return root;
    }

    public void setData(String name) {
        this.ZoneNameLabel.setText(name);
    }


    public void setThemeLabel(String theme) {
        this.ThemeLabel.setText(theme);
    }


    public void setBlockLabel(String block) {
        this.BlockLabel.setText(block);
    }

    public void setOwnerLabel(String owner)
    {
        this.OwnerLabel.setText(owner);
    }

    public void setPowerLabel(String powerLabel) {
        this.PowerLabel.setText(powerLabel);
    }

    public void show() {
        root.setVisible(true);
        root.setManaged(true);
    }

    public void hide() {
        root.setVisible(false);
        root.setManaged(false);
    }

    public void CloseZoneInfoPanel() {
        root.setVisible(false);
    }
}


