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

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    private static StackPane root;

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



    public SettingsController() { }

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
        save(
            (int) musicSlider.getValue(),
            (int) sfxSlider.getValue(),
            muteCheck.isSelected(),
            resolutionCombo.getValue(),
            fullscreenCheck.isSelected(),
            langCombo.getValue()
        );
        goBack();
    }

    @FXML
    private void onCancel() {
        AppController.getInstance().showSettings(false);
    }

    @FXML
    private void goBack() {
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

    public static void save(int music, int effets, boolean sound, String resolution, boolean fullScreen, String lang) {
        System.out.println("=== Paramètres sauvegardés ===");
        System.out.println("Musique : " + music + "%");
        System.out.println("Effets  : " + effets + "%");
        System.out.println("Muet    : " + sound);
        System.out.println("Résolution : " +resolution);
        System.out.println("Plein écran: " + fullScreen);
        System.out.println("Langue  : " + lang);
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

