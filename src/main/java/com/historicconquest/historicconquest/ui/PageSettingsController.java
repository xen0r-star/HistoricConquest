package com.historicconquest.historicconquest.ui;

import com.historicconquest.historicconquest.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class PageSettingsController implements Initializable {

    @FXML private ImageView background;

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

    // Boutons
    @FXML private Button cancelBtn;
    @FXML private Button saveBtn;

    // Pour revenir à la page précédente
    private static Stage stage;
    private static Runnable onBack;

    public void setStage(Stage stage) {
        stage = stage;
    }

    public void setOnBack(Runnable onBack) {
        onBack = onBack;
    }

    public static Stage getStage(){ return stage;}

    public static Runnable getRunnable() { return onBack;}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Background
        try {
            Image bg = new Image(getClass().getResource("/com/historicconquest/historicconquest/images/background.png").toExternalForm());
            background.setImage(bg);
            background.setFitWidth(500);
            background.setFitHeight(300);
        } catch (Exception e) {
            System.out.println("Background non trouvé");
        }
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

        // Appliquer plein écran immédiatement si le stage est connu
        if (stage != null) {
            stage.setFullScreen(fullscreenCheck.isSelected());
        }

        // Retour au menu
        goBack();
    }

    @FXML
    private void onCancel() {
        MainApp.getInstance().showMenu();
    }

    private void goBack() {
        MainApp.getInstance().showMenu();
    }
}