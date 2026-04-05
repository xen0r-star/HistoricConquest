package com.historicconquest.historicconquest.view;

import com.historicconquest.historicconquest.model.questions.Question;
import com.historicconquest.historicconquest.model.questions.Theme;
import com.historicconquest.historicconquest.model.questions.TypeThemes;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QuestionPage {
    private int myAnswer = 0; // A METTRE DANS LA LOGIC GAME PLUS TARD


    private static QuestionPage instance;
    private Question question;

    @FXML public Slider slider;
    @FXML public Button yesButton, noButton, answer1, answer2, answer3, answer4;
    @FXML public Label describeDifficult, theme_label, boutonSelected, questionId;

    private static int difficultyQuestion;
    private static StackPane mainStackPane;
    private static Theme theme;

    public QuestionPage() { instance = this; }

    public static QuestionPage getInstance() { return instance; }

    public static void showChoiceDifficultPage(StackPane parent){
        List<Theme> themeList = Question.getThemesFromJsonFile("/datas/Questions.json");

        // CHOISIR LE VRAI THEME ===========================
        int random = (int) (Math.random() * 4);
        theme = themeList.get(random);
        FXMLLoader questionLoader = new FXMLLoader(
                Objects.requireNonNull(QuestionPage.class.getResource("view/fxml/ChoiceDifficultPage.fxml")));

        FXMLLoader confirmLoader = new FXMLLoader(
                Objects.requireNonNull(QuestionPage.class.getResource("view/fxml/ConfirmPage.fxml")));

        try {
            StackPane difficultStackPane = questionLoader.load();
            StackPane confirmStackPane = confirmLoader.load();

            QuestionPage difficultController = questionLoader.getController();
            QuestionPage confirmController = confirmLoader.getController();

            difficultController.setMainStackPane(parent);
            confirmController.setMainStackPane(parent);
            difficultController.setLabelsTheme();

            confirmStackPane.setVisible(false);

            parent.getChildren().add(difficultStackPane);
            parent.getChildren().add(confirmStackPane);

        } catch (IOException e) {
            System.err.println("Erreur de chargement des fichiers FXML");
        }
    }

    @FXML
    public void confirmDifficult(){
        if (difficultyQuestion == (int) Math.round(slider.getValue())){
            mainStackPane.getChildren().getLast().setVisible(true);

        } else{
            setLabelsTheme();
        }
    }

    public void setLabelsTheme(){
        theme_label.setText(String.valueOf(theme.name));

        switch(theme.name){
            case TypeThemes.HISTORY :
                theme_label.setPrefWidth(180);
                break;
            case TypeThemes.INFORMATIC:
                theme_label.setPrefWidth(280);
                break;
            case TypeThemes.DIVERTISSMENT:
                theme_label.setPrefWidth(340);
                break;
            case TypeThemes.TOURISM:
                theme_label.setPrefWidth(200);
                break;
        }

        difficultyQuestion = (int) Math.round(slider.getValue());
        describeDifficult.setText(theme.describeDifficult.get(difficultyQuestion - 1));
    }

    @FXML
    public void destroyStackPane(){
        mainStackPane.getChildren().removeLast();
        mainStackPane.getChildren().removeLast();
    }


    @FXML
    public void hideConfirm(){ mainStackPane.getChildren().getLast().setVisible(false);}

    @FXML
    public void showQuestionPage(){
        destroyStackPane();
        FXMLLoader questionLoader = new FXMLLoader(
                Objects.requireNonNull(QuestionPage.class.getResource("view/fxml/QuestionPage.fxml")));

        try {
            StackPane questionStackPane = questionLoader.load();

            QuestionPage questionController = questionLoader.getController();
            questionController.startQuestion();

            StackPane stackPane = questionController.getMainStackPane();
            stackPane.getChildren().add(questionStackPane);

        } catch (IOException e) {
            System.err.println("Error loading QuestionPage");
        }
    }

    @FXML
    public void tellWhatAnswerChoiced(ActionEvent e){
        if (e.getSource() == answer1){
            boutonSelected.setText("Answer 1 selected !");
            myAnswer = 1;

        } else if(e.getSource() == answer2){
            boutonSelected.setText("Answer 2 selected !");
            myAnswer = 2;

        } else if(e.getSource() == answer3){
            boutonSelected.setText("Answer 3 selected !");
            myAnswer = 3;

        } else {
            boutonSelected.setText("Answer 4 selected !");
            myAnswer = 4;
        }
    }

    public void startQuestion(){
        List<Question> listQuestion = new ArrayList<>();
        for(Question question : theme.questions){
            if(question.difficulty() == difficultyQuestion){
                listQuestion.add(question);
            }
        }

        int random = (int) (Math.random() * listQuestion.size());
        Question test = listQuestion.get(random);
        questionId.setText(test.question());

        this.setTest(test);
        answer1.setText(test.choices().get(0));
        answer2.setText(test.choices().get(1));
        answer3.setText(test.choices().get(2));
        answer4.setText(test.choices().get(3));
    }

    @FXML
    public void checkWin(){
        if (question.isCorrectAnswer(myAnswer)) {
            System.out.println("WELL DONE YOU WIN !!!");

        } else System.out.println("Unfortunately you lose...");
    }

    public void setMainStackPane(StackPane stackPane) { mainStackPane = stackPane; }
    public StackPane getMainStackPane() { return mainStackPane; }
    public Theme getTheme() { return theme; }
    public void setTest(Question test) { question = test; }
    public Question getQuestion() { return this.question; }

}