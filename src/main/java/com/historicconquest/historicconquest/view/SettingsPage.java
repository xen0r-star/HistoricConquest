package com.historicconquest.historicconquest.view;

import com.historicconquest.historicconquest.app.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsPage implements Initializable {
    // Audio
    @FXML private Slider musicSlider;
    @FXML private Slider sfxSlider;
    @FXML private Label musicValueLabel;
    @FXML private Label sfxValueLabel;
    @FXML private CheckBox muteCheck;

    // Affichage
    @FXML private ComboBox<String> resolutionCombo;
    @FXML private CheckBox fullscreenCheck;
    @FXML private ComboBox<String> langCombo;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Résolutions disponibles
        resolutionCombo.getItems().addAll("1280 x 720","1920 x 1080","2560 x 1440");
        resolutionCombo.setValue("1280 x 720");

        // Langues
        langCombo.getItems().addAll("Français", "English", "Español", "Deutsch");
        langCombo.setValue("Français");

        // Sync labels sliders
        musicSlider.valueProperty().addListener((obs, old, val) ->
            musicValueLabel.setText((int) val.doubleValue() + "%")
        );

        sfxSlider.valueProperty().addListener((obs, old, val) ->
            sfxValueLabel.setText((int) val.doubleValue() + "%")
        );

        // Muet désactive les sliders
        muteCheck.selectedProperty().addListener((obs, old, val) -> {
            musicSlider.setDisable(val);
            sfxSlider.setDisable(val);
        });
    }

    @FXML
    private void onSave() {
        System.out.println("=== Paramètres sauvegardés ===");
        System.out.println("Musique : " + (int) musicSlider.getValue() + "%");
        System.out.println("Effets  : " + (int) sfxSlider.getValue() + "%");
        System.out.println("Muet    : " + muteCheck.isSelected());
        System.out.println("Résolution : " + resolutionCombo.getValue());
        System.out.println("Plein écran: " + fullscreenCheck.isSelected());
        System.out.println("Langue  : " + langCombo.getValue());

        // Retour au menu
        goBack();
    }


    @FXML
    private void onCancel() {
        MainApp.getInstance().showSettings(false);
    }

    private void goBack() {
        MainApp.getInstance().showSettings(false);
    }
}