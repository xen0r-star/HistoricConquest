package com.historicconquest.historicconquest.questions;

import com.historicconquest.historicconquest.Constant;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.Objects;

public class QuestionPage {
    private static QuestionPage instance;
    public Slider slider;
    public Button yesButton, noButton;
    public Label describeDifficult;
    private static byte difficultyQuestion;
    private static StackPane mainStackPane, questionStackPane, confirmStackPane;
    private static Theme theme;

    public QuestionPage(){ instance = this;}

    public static QuestionPage getInstance(){return instance; }

    //OK
    public static void showQuestionPage(StackPane parent){
        /*String content = Question.readFile("/com/historicconquest/historicconquest/datas/History.json");
        System.out.println(content);*/
        theme = Question.getThemeFromJsonFile("/com/historicconquest/historicconquest/datas/History.json");

        //Chargement du FXML avec les différentes difficultés
        FXMLLoader questionLoader = new FXMLLoader(
                Objects.requireNonNull(
                        QuestionPage.class.getResource(Constant.PATH + "ui/QuestionsPage.fxml")
                )
        );

        //Chargement du FXML avec la demande de confirmation
        FXMLLoader confirmLoader = new FXMLLoader(
                Objects.requireNonNull(
                        QuestionPage.class.getResource(Constant.PATH + "ui/ConfirmPage.fxml")
                )
        );

        try {
            questionStackPane = questionLoader.load();
            confirmStackPane = confirmLoader.load();

            // 🔥 récupère les DEUX contrôleurs
            QuestionPage questionController = questionLoader.getController();
            QuestionPage confirmController = confirmLoader.getController();

            // 🔥 initialise les DEUX
            questionController.setMainStackPane(parent);
            confirmController.setMainStackPane(parent);

            confirmStackPane.setVisible(false);

            parent.getChildren().add(questionStackPane);
            parent.getChildren().add(confirmStackPane);

        } catch (IOException e) {
            System.err.println("Erreur de chargement des fichiers FXML");
        }
    }

    //Ok
    public void setMainStackPane(StackPane stackPane){ this.mainStackPane = stackPane;}

    @FXML
    public void confirmDifficult(){
        if (difficultyQuestion == (byte)slider.getValue()){
            mainStackPane.getChildren().getLast().setVisible(true);
        }
        else{
            /* A changer avec les fichier JSON */
            difficultyQuestion = (byte) slider.getValue();
            switch (difficultyQuestion){
                case 1 :
                    describeDifficult.setText(theme.describeDifficult.get(0));
                    break;
                case 2:
                    describeDifficult.setText(theme.describeDifficult.get(1));
                    break;
                case 3:
                    describeDifficult.setText(theme.describeDifficult.get(2));
                    break;
                case 4:
                    describeDifficult.setText(theme.describeDifficult.get(3));
                    break;
                default:
                    describeDifficult.setText("Hey what the ....");
                    break;
            }
        }
    }

    //Ok
    @FXML
    public void destroyStackPane(){
        mainStackPane.getChildren().removeLast();
        mainStackPane.getChildren().removeLast();
    }

    //Ok
    @FXML
    public void hideConfirm(){ mainStackPane.getChildren().getLast().setVisible(false);}
}