package com.historicconquest.historicconquest.controller.page.game;

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
    @FXML public Label playerTop1;
    @FXML public Label playerTop2;
    @FXML public Label playerTop3;
    @FXML public Label playerTop4;
    @FXML public Label playerName1;
    @FXML public Label playerName2;
    @FXML public Label playerName3;
    @FXML public Label playerName4;
    @FXML public Label playerPower1;
    @FXML public Label playerPower2;
    @FXML public Label playerPower3;
    @FXML public Label playerPower4;

    private static final String ROW_HIGHLIGHT_STYLE = "-fx-background-color: #A47E5866;";

    public void updateGameInfo(List<Player> players, int currentPlayerIndex) {
        updateRow(0, players, currentPlayerIndex, row1, playerTop1, playerName1, playerPower1);
        updateRow(1, players, currentPlayerIndex, row2, playerTop2, playerName2, playerPower2);
        updateRow(2, players, currentPlayerIndex, row3, playerTop3, playerName3, playerPower3);
        updateRow(3, players, currentPlayerIndex, row4, playerTop4, playerName4, playerPower4);
    }

    public void show(List<Player> players, int currentPlayerIndex) {
        updateGameInfo(players, currentPlayerIndex);
        root.setVisible(true);
    }

    private void updateRow(
            int index, List<Player> players,
            int currentPlayerIndex, HBox row,
            Label topLabel, Label nameLabel, Label powerLabel
    ) {
        if (row == null || nameLabel == null || powerLabel == null) return;

        if (players == null || index >= players.size()) {
            row.setVisible(false);
            row.setManaged(false);
            return;
        }

        Player player = players.get(index);
        if (player != null) {
            int zones = player.getZones() != null ? player.getZones().size() : 0;

            long rank = 1;
            for (int i = 0; i < players.size(); i++) {
                Player other = players.get(i);
                int otherZones = other.getZones().size();

                if (otherZones > zones || (otherZones == zones && i < index)) {
                    rank++;
                }
            }

            topLabel.setText(String.valueOf(rank));
            nameLabel.setText(player.getPseudo());
            powerLabel.setText(String.valueOf(zones));
            nameLabel.setTextFill(player.getColor().getJavafxColor());
        }

        row.setVisible(true);
        row.setManaged(true);
        row.setStyle(index == currentPlayerIndex ? ROW_HIGHLIGHT_STYLE : "");
    }
}
