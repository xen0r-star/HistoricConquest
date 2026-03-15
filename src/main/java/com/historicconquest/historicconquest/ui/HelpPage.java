package com.historicconquest.historicconquest.ui;

import com.historicconquest.historicconquest.Constant;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class HelpPage {

    private static StackPane help_stackPane;
    public HelpPage(StackPane stackPane) {
    }

    public static StackPane getHelpStackPane() throws IOException{
        FXMLLoader helpLoader = new FXMLLoader(HelpPage.class.getResource(Constant.PATH + "ui/HelpPage.fxml"));
        help_stackPane = helpLoader.load();
//        stackPane.setAlignment(help_stackPane, Pos.TOP_RIGHT);
//        stackPane.setMargin(help_stackPane, new Insets(30, 30, 0, 0));
        help_stackPane.setVisible(false);
        help_stackPane.setManaged(false);
        return help_stackPane;
    }

    public static void toShow(){
        help_stackPane.setVisible(true);
    }

    public static void return_main(){
        help_stackPane.setVisible(false);
    }

}
