package com.historicconquest.historicconquest.controller.page.game;

import com.historicconquest.historicconquest.controller.game.GameController;
import com.historicconquest.historicconquest.controller.overlay.Notification;
import com.historicconquest.historicconquest.controller.overlay.NotificationController;
import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.player.Player;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class CoalitionController {
    @FXML public ListView<Player> listPlayers ;
    @FXML public Button btnClose ;
    @FXML public Button btnProposal ;
    @FXML public StackPane root ;
    @FXML public VBox panePendingRequest ;
    @FXML public Label lblRequesterName ;



    @FXML
    public  void closeAlliance() {
        root.setVisible(false);
    }

    @FXML
    public void initialize() {
        GameController game = GameController.getInstance();

        if (game != null) {
            ObservableList<Player> observablePlayers = FXCollections.observableArrayList(game.getPlayers());

            observablePlayers.remove(game.getCurrentPlayer());

            listPlayers.setItems(observablePlayers);
        }

        listPlayers.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Player player, boolean empty) {
                super.updateItem(player, empty);
                if (empty || player == null) {
                    setText(null);

                } else {
                    setText(player.getPseudo());
                }
            }
        });
    }


    @FXML
    public void sendProposal() {
        Player target = listPlayers.getSelectionModel().getSelectedItem();
        Player sender = GameController.getInstance().getCurrentPlayer();

        if (target != null && !target.hasAlly()) {
            target.setPendingAllianceRequest(sender);

            NotificationController.show(
                "Proposal Sent",
                "Request sent to " + target.getPseudo(),
                Notification.Type.SUCCESS,
                5000
            );
            closeAlliance();
        }
    }


    public void refreshPlayerList() {
        Player current = GameController.getInstance().getCurrentPlayer();

        if(current.hasPendingRequest()) {
            panePendingRequest.setVisible(true);
            panePendingRequest.setManaged(true);
            lblRequesterName.setText(current.getPendingAllianceRequest().getPseudo());

        } else {
            panePendingRequest.setVisible(false);
            panePendingRequest.setManaged(false);
        }

        GameController game = GameController.getInstance();
        if (game != null) {
            ObservableList<Player> players = FXCollections.observableArrayList(game.getPlayers());
            players.removeIf(p ->
                p.equals(current) || p.hasAlly()
            );
            listPlayers.setItems(players);
        }
    }



    @FXML
    public void acceptAlliance() {
        Player current = GameController.getInstance().getCurrentPlayer();
        Player requester = current.getPendingAllianceRequest();

        Color allianceColor = GameController.getInstance().getNextAllianceColor();

        current.setAlly(requester);
        current.setCurrentAllianceColor(allianceColor);

        requester.setAlly(current);
        requester.setCurrentAllianceColor(allianceColor);

        for (Zone z : current.getZones()) z.setColor(allianceColor);
        for (Zone z : requester.getZones()) z.setColor(allianceColor);

        current.clearPendingRequest();
        closeAlliance();
        NotificationController.show(
            "Alliance",
            "You are now allied with " + requester.getPseudo(),
            Notification.Type.SUCCESS,
            5000
        );
    }


    @FXML
    public void declineAlliance() {
        Player current = GameController.getInstance().getCurrentPlayer();
        current.clearPendingRequest();
        refreshPlayerList();
    }
}
