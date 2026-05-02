package com.historicconquest.historicconquest.controller.page.game;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class ZoneInfoPanel {
    @FXML public AnchorPane root;
    @FXML public Label TitleLabel;
    @FXML public Label SubTitleLabel;
    @FXML public Label DescriptionLabel;


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


    public void setTitleLabel(String title) {
        this.TitleLabel.setText(title);
    }

    public void setSubTitleLabel(String subTitle) {
        this.SubTitleLabel.setText(subTitle);
    }

    public void setDescriptionLabel(String description) {
        this.DescriptionLabel.setText(description);
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


