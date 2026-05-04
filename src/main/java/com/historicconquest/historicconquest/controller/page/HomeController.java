package com.historicconquest.historicconquest.controller.page;

import com.historicconquest.historicconquest.controller.core.AppPage;
import com.historicconquest.historicconquest.controller.core.AppController;
import com.historicconquest.historicconquest.controller.game.MapBackgroundController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class HomeController {
    public StackPane root;
    public Pane mapViewport;

    public Button newGameBtn;
    public Button multiplayerBtn;

    public Button settingsBtn;
    public Button helpBtn;
    public Button exitBtn;

    @FXML
    public void initialize() {
        newGameBtn.setOnAction(    e -> AppController.getInstance().showPage(AppPage.NEW_GAME));
        multiplayerBtn.setOnAction(e -> AppController.getInstance().showPage(AppPage.MULTIPLAYER));

        settingsBtn.setOnAction(e -> AppController.getInstance().showSettings(true));
        helpBtn.setOnAction(    e -> AppController.getInstance().showHelp(true));
        exitBtn.setOnAction(    e -> AppController.getInstance().exit());


        MapBackgroundController.show(
            root, mapViewport,
            -55 ,-30, -0.03
        );
    }
}
