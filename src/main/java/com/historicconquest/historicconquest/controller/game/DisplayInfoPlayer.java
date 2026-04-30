package com.historicconquest.historicconquest.controller.game;

import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.player.Player;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

public class DisplayInfoPlayer {

    @FXML
    public AnchorPane root ;
    @FXML
    public Label labelNamePlayer ;

    @FXML
    public Label LabelSuccessCount ;

    @FXML
    public Label LabelFailureCount ;

    @FXML
    public TableView<Zone> labelItems ;

    @FXML
    public TableColumn<Zone , String > colName ;

    @FXML
    public TableColumn<Zone , Integer> colPower ;

    @FXML
    public Label labelCurrentLoc ;

    @FXML
    public Label LabelWorldProgress ;

    @FXML
    public Button CloseBtn ;


    @FXML
    public void Close()
    {
        root.setVisible(false);
    }


    public Label getLabelFailureCount() {
        return LabelFailureCount;
    }


    public Label getLabelCurrentLoc() {
        return labelCurrentLoc;
    }

    public Label getLabelNamePlayer() {
        return labelNamePlayer;
    }


    public Label getLabelSuccessCount() {
        return LabelSuccessCount;
    }

    public TableColumn<Zone, Integer> getColPower() {
        return colPower;
    }

    public TableColumn<Zone, String> getColName() {
        return colName;
    }

    public Label getLabelWorldProgress() {
        return LabelWorldProgress;
    }

    public TableView<Zone> getLabelItems() {
        return labelItems;
    }


    public void setColName(TableColumn<Zone, String> colName) {
        this.colName = colName;
    }

    public void setLabelWorldProgress(Label labelWorldProgress) {
        LabelWorldProgress = labelWorldProgress;
    }

    public void setLabelSuccessCount(Label labelSuccessCount) {
        LabelSuccessCount = labelSuccessCount;
    }

    public void setColPower(TableColumn<Zone, Integer> colPower) {
        this.colPower = colPower;
    }

    public void setLabelCurrentLoc(Label labelCurrentLoc) {
        this.labelCurrentLoc = labelCurrentLoc;
    }

    public void setLabelFailureCount(Label labelFailureCount) {
        LabelFailureCount = labelFailureCount;
    }

    public void setLabelItems(TableView<Zone> labelItems) {
        this.labelItems = labelItems;
    }

    public void setLabelNamePlayer(Label labelNamePlayer) {
        this.labelNamePlayer = labelNamePlayer;
    }


    public void updatePlayerData(Player player)
    {
        labelNamePlayer.setText(player.getPseudo());
        LabelSuccessCount.setText(String.valueOf(player.getConsecutiveSuccesses()));
        LabelFailureCount.setText(String.valueOf(player.getConsecutiveFailures()));
        labelCurrentLoc.setText(player.getCurrentZone().getName());
        LabelWorldProgress.setText(String.valueOf(player.getZones().size()));

        colName.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));

        colPower.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPower()));

        // Conversion de la liste des zones en ObservableList pour JavaFX
        javafx.collections.ObservableList<Zone> zones =
                javafx.collections.FXCollections.observableArrayList(player.getZones());
        labelItems.setItems(zones);


    }

    public void show(Player player)
    {
        updatePlayerData(player);
        root.setVisible(true);
    }

}
