package com.historicconquest.historicconquest.controller.overlay;

import com.historicconquest.historicconquest.controller.core.AppController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    private static StackPane root;

    @FXML private Slider musicSlider;
    @FXML private Slider sfxSlider;
    @FXML private Label musicValueLabel;
    @FXML private Label sfxValueLabel;
    @FXML private CheckBox muteCheck;

    @FXML private ComboBox<String> resolutionCombo;
    @FXML private CheckBox fullscreenCheck;


    public SettingsController() { }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resolutionCombo.getItems().addAll("1280 x 720", "1920 x 1080", "2560 x 1440");
        resolutionCombo.setValue("1280 x 720");

        musicSlider.valueProperty().addListener((obs, old, val) ->
            musicValueLabel.setText((int) val.doubleValue() + "%")
        );

        sfxSlider.valueProperty().addListener((obs, old, val) ->
            sfxValueLabel.setText((int) val.doubleValue() + "%")
        );

        muteCheck.selectedProperty().addListener((obs, old, muted) -> {
            musicSlider.setDisable(muted);
            sfxSlider.setDisable(muted);
        });
    }

    @FXML
    private void onSave() {
        String resolution = resolutionCombo.getValue();
        String[] parts = resolution.split(" x ");
        int width  = Integer.parseInt(parts[0].trim());
        int height = Integer.parseInt(parts[1].trim());

        Stage stage = AppController.getInstance().getStage();
        if (stage != null) {
            stage.setWidth(width);
            stage.setHeight(height);
            stage.setFullScreen(fullscreenCheck.isSelected());
        }

        System.out.println("=== Paramètres sauvegardés ===");
        System.out.println("Musique    : " + (int) musicSlider.getValue() + "%");
        System.out.println("Effets     : " + (int) sfxSlider.getValue() + "%");
        System.out.println("Muet       : " + muteCheck.isSelected());
        System.out.println("Résolution : " + resolution);
        System.out.println("Plein écran: " + fullscreenCheck.isSelected());

        AppController.getInstance().showSettings(false);
    }

    @FXML
    private void onCancel() {
        AppController.getInstance().showSettings(false);
    }



    public static void initialize() {
        if (root != null) return;

        try {
            FXMLLoader loader = new FXMLLoader(SettingsController.class.getResource("/view/fxml/SettingsPage.fxml"));
            root = loader.load();

            root.setVisible(false);
            root.setManaged(false);

        } catch (Exception e) {
            System.err.println("Error loading settings");
        }
    }

    public static void show(){
        if (root == null) return;

        root.setVisible(true);
        root.setManaged(true);
    }

    public static void close(){
        if (root == null) return;

        root.setVisible(false);
        root.setManaged(false);
    }


    public static StackPane getSettings() {
        return root;
    }
}