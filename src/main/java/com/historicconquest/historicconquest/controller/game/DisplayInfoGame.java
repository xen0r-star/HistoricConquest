package com.historicconquest.historicconquest.controller.game;

import com.historicconquest.historicconquest.model.player.Player;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.util.List;

public class DisplayInfoGame {
    @FXML public AnchorPane root;
    @FXML public HBox row1;
    @FXML public HBox row2;
    @FXML public HBox row3;
    @FXML public HBox row4;
    @FXML public Label playerName1;
    @FXML public Label playerName2;
    @FXML public Label playerName3;
    @FXML public Label playerName4;
    @FXML public Label playerPower1;
    @FXML public Label playerPower2;
    @FXML public Label playerPower3;
    @FXML public Label playerPower4;

    private static final String ROW_HIGHLIGHT_STYLE = "-fx-background-color: #C5A68266;";

    public void updateGameInfo(List<Player> players, int currentPlayerIndex) {
        updateRow(0, players, currentPlayerIndex, row1, playerName1, playerPower1);
        updateRow(1, players, currentPlayerIndex, row2, playerName2, playerPower2);
        updateRow(2, players, currentPlayerIndex, row3, playerName3, playerPower3);
        updateRow(3, players, currentPlayerIndex, row4, playerName4, playerPower4);
    }

    public void show(List<Player> players, int currentPlayerIndex) {
        updateGameInfo(players, currentPlayerIndex);
        root.setVisible(true);
    }

    private void updateRow(
            int index, List<Player> players,
            int currentPlayerIndex, HBox row,
            Label nameLabel, Label powerLabel
    ) {
        if (row == null || nameLabel == null || powerLabel == null) return;

        if (players == null || index >= players.size()) {
            row.setVisible(false);
            row.setManaged(false);
            return;
        }

        Player player = players.get(index);
        if (player != null) {
            String name = player.getPseudo() != null ? player.getPseudo() : "-";
            int zones = player.getZones() != null ? player.getZones().size() : 0;

            nameLabel.setText(name);
            powerLabel.setText(String.valueOf(zones));
            nameLabel.setTextFill(player.getColor().getJavafxColor());
        }

        row.setVisible(true);
        row.setManaged(true);
        row.setStyle(index == currentPlayerIndex ? ROW_HIGHLIGHT_STYLE : "");
    }
}
