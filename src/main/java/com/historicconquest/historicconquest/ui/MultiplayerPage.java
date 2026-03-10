package com.historicconquest.historicconquest.ui;

import com.historicconquest.historicconquest.MainApp;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class MultiplayerPage {
    public StackPane root;
    public Pane mapViewport;

    public HBox SelectModePanel, JoinPanel;

    // SelectModePanel
    public Pane JoinPane;
    public Pane HostPane;
    public Button BackBtn;

    // JoinPanel
    public HBox CodeBox;
    public TextField Code1, Code2, Code3, Code4, Code5, Code6;
    public Button JoinBtn;

    private static int panel = 1;

    @FXML
    public void initialize() {
        panel = 1;
        SelectModePanel.setVisible(true);
        JoinPanel.setVisible(false);


        // SelectModePanel
        JoinPane.setOnMouseClicked(e -> {
            panel = 2;
            SelectModePanel.setVisible(false);
            JoinPanel.setVisible(true);

            Code1.requestFocus();
        });

        HostPane.setOnMouseClicked(e -> {
            System.out.println("Host button clicked");
        });

        BackBtn.setOnAction(e -> {
            if (panel == 1) {
                MainApp.getInstance().showMenu();

            } else if (panel == 2) {
                panel = 1;
                SelectModePanel.setVisible(true);
                JoinPanel.setVisible(false);
            }
        });


        // JoinPanel
        JoinBtn.setOnAction(e -> {
            System.out.println(
                "Join button clicked " +
                Code1.getText() + Code2.getText() + Code3.getText() + Code4.getText() + Code5.getText() + Code6.getText()
            );
        });


        // Code Box - Join Game
        TextField[] fields = {Code1, Code2, Code3, Code4, Code5, Code6};

        for (int i = 0; i < fields.length; i++) {
            int index = i;

            fields[i].textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.isEmpty()) return;

                if (newVal.length() > 1 && newVal.length() <= fields.length) {
                    char[] chars = newVal.toUpperCase().toCharArray();

                    for (int j = 0; j < chars.length && j < fields.length; j++) {
                        fields[j].setText(String.valueOf(chars[j]));
                    }

                    fields[Math.min(chars.length, fields.length) - 1].requestFocus();
                    return;
                }

                String value = newVal.substring(0,1).toUpperCase();

                if (!value.equals(fields[index].getText())) fields[index].setText(value);
                if (index < fields.length - 1)              fields[index + 1].requestFocus();
            });

            fields[i].setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.BACK_SPACE && fields[index].getText().isEmpty() && index > 0) {
                    fields[index - 1].requestFocus();
                }
            });
        }


        // Affichage de la map décoration en arrière-plan
        MapBackgroundDisplay mapDisplay = MapBackgroundDisplay.getInstance();
        mapDisplay.setTransformations(0.8, 0.8, 100);
        mapDisplay.display(root, mapViewport);
    }
}