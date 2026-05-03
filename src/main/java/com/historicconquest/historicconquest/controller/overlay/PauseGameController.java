package com.historicconquest.historicconquest.controller.overlay;

import com.historicconquest.historicconquest.controller.core.AppController;
import com.historicconquest.historicconquest.controller.core.AppPage;
import com.historicconquest.historicconquest.controller.game.GameController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class PauseGameController implements Initializable {
    private static AnchorPane root;
    @FXML public Button resumeBtn, quitMenuBtn;
    @FXML public Button settingsBtn, helpBtn;


    public PauseGameController() { }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resumeBtn.setOnAction(e -> PauseGameController.close());
        quitMenuBtn.setOnAction(e -> {
            AppController.getInstance().showPage(AppPage.HOME);
            GameController.clearGame();
            PauseGameController.close();
        });

        settingsBtn.setOnAction(e -> AppController.getInstance().showSettings(true));
        helpBtn.setOnAction(    e -> AppController.getInstance().showHelp(true));
    }


    public static void initialize() {
        if (root != null) return;

        try {
            FXMLLoader loader = new FXMLLoader(SettingsController.class.getResource("/view/fxml/PauseGame.fxml"));
            root = loader.load();

            root.setVisible(false);
            root.setManaged(false);

        } catch (Exception e) {
            System.err.println("Error loading pause game view");
        }
    }

    public static void show() {
        if (root == null) return;

        root.setVisible(true);
        root.setManaged(true);
    }

    public static void close() {
        if (root == null) return;

        root.setVisible(false);
        root.setManaged(false);
    }


    public static AnchorPane getPauseGame() {
        return root;
    }
}
