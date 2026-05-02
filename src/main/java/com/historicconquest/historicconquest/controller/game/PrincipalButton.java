package com.historicconquest.historicconquest.controller.game;

import com.historicconquest.historicconquest.controller.page.QuestionController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class PrincipalButton {
    private GameHUD gameHUD;
    public StackPane root;

    @FXML public Button buttonAttack;
    @FXML public Button buttonTravel;
    @FXML public Button buttonPower;
    @FXML public Button buttonAlliance;
    @FXML public Button buttonSkipTurn;


    public void startAttack() {
        if (!MultiplayerGameOverlay.ensureLocalTurn("attack")) return;
        System.out.println("Attaque");
        GameController.getInstance().setPendingAction(GameController.PendingAction.ATTACK);
        ShowQuestion();
    }

    public void startTravel() {
        if (!MultiplayerGameOverlay.ensureLocalTurn("travel")) return;
        System.out.println("Travel");
        GameController.getInstance().setPendingAction(GameController.PendingAction.TRAVEL);
        ShowQuestion();
    }

    public void startPower() {
        if (!MultiplayerGameOverlay.ensureLocalTurn("power up")) return;
        System.out.println("Power up");
        GameController.getInstance().setPendingAction(GameController.PendingAction.POWER_UP);
        ShowQuestion();
    }


    public void startCoalition() {
        System.out.println("bouton clique coalition");
        if (gameHUD != null) {
            gameHUD.showCoalitionMenu();
        }
    }

    public void skipTurn() {

    }


    public void ShowQuestion() {
        root.getChildren().removeLast();
        QuestionController.showChoiceDifficultPage(root);
    }


    public void setParentRoot(StackPane root) {
        this.root = root ;
    }

    public void setGameHUD(GameHUD gameHUD) {
        this.gameHUD = gameHUD;
    }
}
