package com.historicconquest.historicconquest.controller.overlay;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class ConfirmChoiceController {
    @FXML private Label messageLabel;
    @FXML private Button btn1, btn2;

    private Runnable action1;
    private Runnable action2;
    private Pane  container;
    private Node view;

    public static void show(Pane container, String message, String textBtn1, Runnable onBtn1, String textBtn2, Runnable onBtn2) {
        if (container == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(ConfirmChoiceController.class.getResource("/view/fxml/ConfirmChoice.fxml"));
            Node view = loader.load();

            ConfirmChoiceController controller = loader.getController();
            if (controller == null) return;

            controller.container = container;
            controller.view = view;
            controller.setup(message, textBtn1, onBtn1, textBtn2, onBtn2);

            container.getChildren().add(view);
            view.toFront();

        } catch (IOException e) {
            System.err.println("Error showing ConfirmChoice.fxml");
        }
    }

    private void setup(String message, String t1, Runnable a1, String t2, Runnable a2) {
        messageLabel.setText(message);
        btn1.setText(t1);
        btn2.setText(t2);
        this.action1 = a1;
        this.action2 = a2;
    }

    @FXML
    private void handleBtn1() {
        try {
            if (action1 != null) action1.run();
        } finally {
            close();
        }
    }

    @FXML
    private void handleBtn2() {
        try {
            if (action2 != null) action2.run();
        } finally {
            close();
        }
    }

    private void close() {
        if (container == null || view == null) return;
        container.getChildren().remove(view);
        view = null;
        container = null;
    }
}
