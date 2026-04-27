package com.historicconquest.historicconquest.controller.game;

import com.historicconquest.historicconquest.model.map.Zone;
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







}
