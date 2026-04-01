package com.historicconquest.historicconquest.view;

import com.historicconquest.historicconquest.app.App;
import com.historicconquest.historicconquest.app.AppPage;
import com.historicconquest.historicconquest.controller.MapBackgroundController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomePage {

    private static final Logger logger = LoggerFactory.getLogger(HomePage.class);
    public StackPane root;
    public Pane mapViewport;

    public Button newGameBtn;
    public Button loadGameBtn;
    public Button multiplayerBtn;

    public Button settingsBtn;
    public Button helpBtn;
    public Button exitBtn;

    @FXML
    public void initialize() {
        newGameBtn.setOnAction(    e -> App.getInstance().showPage(AppPage.NEW_GAME));
        loadGameBtn.setOnAction(   e -> logger.debug("Load game mode selected from HomePage"));
        multiplayerBtn.setOnAction(e -> App.getInstance().showPage(AppPage.MULTIPLAYER));

        settingsBtn.setOnAction(e -> App.getInstance().showSettings(true));
        helpBtn.setOnAction(    e -> App.getInstance().showHelp(true));
        exitBtn.setOnAction(    e -> App.getInstance().exit());


        MapBackgroundController.show(
            root, mapViewport,
            -55 ,-30, -0.03
        );
    }
}