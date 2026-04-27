package com.historicconquest.historicconquest.controller.page;

import com.historicconquest.historicconquest.controller.game.GameController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class SelectAction {

    @FXML
    public Button buttonAttack ;

    @FXML
    public Button buttonTravel ;

    @FXML
    public Button buttonPower ;

    public StackPane root ;

    private GameController.PendingAction pendingAction = GameController.PendingAction.NONE ;


    public void startAttack()
    {
        System.out.println("Attaque");

        GameController.getInstance().setPendingAction(GameController.PendingAction.ATTACK);

        ShowQuestion();
    }

    public void startTravel()
    {
        System.out.println("Travel");
        GameController.getInstance().setPendingAction(GameController.PendingAction.TRAVEL);
        ShowQuestion();
    }

    public void startPower()
    {
        System.out.println("Power up");
        GameController.getInstance().setPendingAction(GameController.PendingAction.POWER_UP);
        ShowQuestion();

    }


    public void ShowQuestion()
    {

        root.getChildren().removeLast();
        QuestionController.showChoiceDifficultPage(root);
    }

    public void setParentRoot(StackPane root) {

        this.root = root ;
    }
}
