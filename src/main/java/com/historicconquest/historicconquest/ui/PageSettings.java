package com.historicconquest.historicconquest.ui;

import com.historicconquest.historicconquest.MainApp; // Vérifie bien cet import
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import java.util.Objects;

public class PageSettings {

    // On rend la méthode STATIC pour que MainApp puisse l'appeler facilement
    public static StackPane getSettingsStackPane() {
        try {
            // Attention au chemin du FXML, il doit être précis
            FXMLLoader loader = new FXMLLoader(PageSettings.class.getResource("/com/historicconquest/historicconquest/ui/SettingsPage.fxml"));
            StackPane root = loader.load();

            // On configure le contrôleur
            PageSettingsController controller = loader.getController();

            // On lui donne le stage principal via le Singleton de MainApp
            if (MainApp.getInstance() != null) {
                controller.setStage(MainApp.getInstance().getStage());
            }

            return root;

        } catch (Exception e) {
            System.err.println("Erreur chargement PageSettings.fxml : " + e.getMessage());
            e.printStackTrace();
            return new StackPane(); // Retourne un panneau vide pour éviter le crash
        }
    }
}
