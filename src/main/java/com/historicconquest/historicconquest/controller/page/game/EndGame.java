package com.historicconquest.historicconquest.controller.page.game;

import com.historicconquest.historicconquest.controller.core.AppController;
import com.historicconquest.historicconquest.controller.core.AppPage;
import com.historicconquest.historicconquest.controller.game.GameController;
import com.historicconquest.historicconquest.controller.overlay.PauseGameController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;

public class EndGame {
    @FXML public Label lblWinnerName;
    @FXML public StackPane root;
    @FXML public Button quitMenuBtn;

    @FXML
    public void initialize() {
        quitMenuBtn.setOnAction(e -> {
            AppController.getInstance().showPage(AppPage.HOME);
            GameController.clearGame();
            PauseGameController.close();
        });
    }


    @FXML
    private void consumeEvent(MouseEvent event) {
        event.consume();
    }

    @FXML
    private void handleScroll(ScrollEvent event) {
        event.consume();
    }
}
