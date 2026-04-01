package com.historicconquest.historicconquest.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;

public class SettingsController {
    private static StackPane root;


    private SettingsController() { }

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
