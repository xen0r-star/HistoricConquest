package com.historicconquest.historicconquest.controller.page.game;

import com.historicconquest.historicconquest.model.questions.TypeThemes;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.util.Objects;

public class ZoneInfoPanel {
    @FXML public AnchorPane root;
    @FXML public ImageView ThemeImage;
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


    public void setThemeImage(TypeThemes typeTheme) {
        switch (typeTheme) {
            case ENTERTAINMENT ->   ThemeImage.setImage(new Image(Objects.requireNonNull(getClass().getResource("/view/icons/entertainment-icon.png")).toExternalForm()));
            case INFORMATICS ->     ThemeImage.setImage(new Image(Objects.requireNonNull(getClass().getResource("/view/icons/informatics-icon.png")).toExternalForm()));
            case TOURISM ->         ThemeImage.setImage(new Image(Objects.requireNonNull(getClass().getResource("/view/icons/tourism-icon.png")).toExternalForm()));
            case MYSTERY ->         ThemeImage.setImage(new Image(Objects.requireNonNull(getClass().getResource("/view/icons/mystery-icon.png")).toExternalForm()));
            default ->              ThemeImage.setImage(new Image(Objects.requireNonNull(getClass().getResource("/view/icons/default.png")).toExternalForm()));
        }
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


