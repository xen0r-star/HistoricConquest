package com.historicconquest.historicconquest.controller;

import com.historicconquest.historicconquest.ui.Help;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class HelpController {
    @FXML private Button button_return;

    public void return_main(){
        Help.return_main();
    }
}
