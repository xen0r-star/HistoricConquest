package com.historicconquest.historicconquest.controller.page.game;

import com.historicconquest.historicconquest.model.player.Player;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class DisplayInfoPlayer {
    @FXML public AnchorPane root;
    @FXML public Label LabelSuccessCount;
    @FXML public Label LabelFailureCount;


    public void updatePlayerData(Player player) {
        if (player == null) {
            LabelSuccessCount.setText("Successes: 0");
            LabelFailureCount.setText("Failures: 0");
            return;
        }

        LabelSuccessCount.setText("Successes: " + player.getConsecutiveSuccesses());
        LabelFailureCount.setText("Failures: " + player.getConsecutiveFailures());
    }

    public void show(Player player) {
        updatePlayerData(player);
        root.setVisible(true);
    }
}
