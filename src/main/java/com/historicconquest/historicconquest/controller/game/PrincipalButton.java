package com.historicconquest.historicconquest.controller.game;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class PrincipalButton {

    private GameHUD gameHUD ;

    public void setGameHUD(GameHUD gameHUD) {
        this.gameHUD = gameHUD;
    }

    @FXML
    public Button buttonAttack ;

    @FXML
    public Button buttonStart ;

    @FXML
    public Button buttonAlliance ;

    @FXML
    public void startShowInfo()
    {
        if (gameHUD != null) {
            gameHUD.togglePlayerInfo();
        }
    }

    @FXML
    public void startStart()
    {
        if (gameHUD != null) {
            gameHUD.showSelectAction();
        }
    }

    @FXML
    public void startCoalition()
    {
        System.out.println("bouton clique coalition");
        if (gameHUD != null) {
            gameHUD.showCoalitionMenu();
        }
    }

}
