package com.historicconquest.historicconquest.controller;

import com.historicconquest.historicconquest.ui.HelpPage;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class HelpController {
    @FXML private Button button_return;

    public void return_main(){
        HelpPage.return_main();
    }
}
