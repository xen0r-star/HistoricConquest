package com.historicconquest.historicconquest.controller;

import com.historicconquest.historicconquest.Constant;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class HelpController {
    private static StackPane root;


    private HelpController() { }

    public static void initialize() {
        if (root != null) return;

        try {
            FXMLLoader helpLoader = new FXMLLoader(HelpController.class.getResource(Constant.PATH + "ui/HelpPage.fxml"));
            root = helpLoader.load();

            StackPane.setAlignment(root, Pos.TOP_RIGHT);
            StackPane.setMargin(root, new Insets(30, 30, 0, 0));

            root.setVisible(false);
            root.setManaged(false);

        } catch (IOException e) {
            System.err.println("Error loading help page");
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


    public static StackPane getHelp() {
        return root;
    }
}
