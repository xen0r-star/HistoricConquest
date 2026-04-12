package com.historicconquest.historicconquest.controller.page;

import com.historicconquest.historicconquest.controller.core.AppPage;
import com.historicconquest.historicconquest.controller.core.AppController;
import com.historicconquest.historicconquest.controller.game.MapBackgroundController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
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
        newGameBtn.setOnAction(    e -> AppController.getInstance().showPage(AppPage.NEW_GAME));
        loadGameBtn.setOnAction(   e -> logger.debug("Load game mode selected from HomePage"));
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
