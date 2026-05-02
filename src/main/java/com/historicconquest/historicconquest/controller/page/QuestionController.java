package com.historicconquest.historicconquest.controller.page;

import com.historicconquest.historicconquest.controller.game.ZoneInfoPanel;
import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.model.questions.Question;
import com.historicconquest.historicconquest.model.questions.Theme;
import com.historicconquest.historicconquest.model.questions.TypeThemes;
import com.historicconquest.historicconquest.controller.game.GameController;
import com.historicconquest.historicconquest.controller.game.MultiplayerGameOverlay;
import com.historicconquest.historicconquest.controller.overlay.Notification;
import com.historicconquest.historicconquest.controller.overlay.NotificationController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QuestionController {
    private int myAnswer = 0; // A METTRE DANS LA LOGIC GAME PLUS TARD


    private static QuestionController instance;
    private Question question;
    private GameController gameController ;

    @FXML public Slider slider;
    @FXML public Button yesButton, noButton, answer1, answer2, answer3, answer4;
    @FXML public Label describeDifficult, theme_label, boutonSelected, questionId;

    private static int difficultyQuestion;
    private static StackPane mainStackPane;
    private static Theme theme;

    public QuestionController() { instance = this; }

    public static QuestionController getInstance() { return instance; }

    public static void showChoiceDifficultPage(StackPane parent){
        List<Theme> themeList = Question.getThemesFromJsonFile("/datas/Questions.json");

       Player player = GameController.getInstance().getCurrentPlayer();
       String targetLabel = player.getCurrentZone().getThemes().getLabel();

       theme = themeList.stream()
               .filter(t -> t.name.getLabel().equalsIgnoreCase(targetLabel))
               .findFirst()
               .orElse(themeList.getFirst());




        FXMLLoader questionLoader = new FXMLLoader(
                Objects.requireNonNull(QuestionController.class.getResource("/view/fxml/ChoiceDifficultPage.fxml")));



        try {
            ZoneInfoPanel.getInstance().hide();
            StackPane difficultStackPane = questionLoader.load();


            QuestionController difficultController = questionLoader.getController();


            difficultController.setMainStackPane(parent);
            difficultController.setLabelsTheme();



            parent.getChildren().add(difficultStackPane);





        } catch (IOException e) {
            System.err.println("Erreur de chargement des fichiers FXML");
        }
    }

    @FXML
    public void confirmDifficult(){
        int sliderValue = (int) Math.round(slider.getValue());
        difficultyQuestion = sliderValue;

        mainStackPane.getChildren().getLast().setVisible(true);
        showQuestionPage();

    }

    public void setLabelsTheme(){
        theme_label.setText(String.valueOf(theme.name));

        Player player = GameController.getInstance().getCurrentPlayer();

        String LabelTheme = player.getCurrentZone().getThemes().getLabel() ;

        theme_label.setText(LabelTheme);

        switch(LabelTheme) {
            case "History" :
                theme_label.setPrefWidth(180);
                theme_label.setAlignment(Pos.CENTER);
                break;
            case "Informatic":
                theme_label.setPrefWidth(200);
                theme_label.setAlignment(Pos.CENTER);
                break;
            case "Divertissment":
                theme_label.setPrefWidth(260);
                theme_label.setAlignment(Pos.CENTER);
                break;
            case "Tourism":
                theme_label.setPrefWidth(160);
                theme_label.setAlignment(Pos.CENTER);
                break;
        }

        difficultyQuestion = (int) Math.round(slider.getValue());

    }

    @FXML
    public void destroyStackPane(){
        mainStackPane.getChildren().removeLast();
    }




    @FXML
    public void hideConfirm(){ mainStackPane.getChildren().getLast().setVisible(false);}

    @FXML
    public void showQuestionPage(){
        destroyStackPane();
        FXMLLoader questionLoader = new FXMLLoader(
                Objects.requireNonNull(QuestionController.class.getResource("/view/fxml/QuestionPage.fxml")));

        try {

            StackPane questionStackPane = questionLoader.load();

            QuestionController questionController = questionLoader.getController();
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

        checkWin();
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

        boolean correct = question.isCorrectAnswer(myAnswer);

        MultiplayerGameOverlay.requestQuestionResult(difficultyQuestion, correct);

        mainStackPane.getChildren().removeLast();

    }

    public void setMainStackPane(StackPane stackPane) { mainStackPane = stackPane; }
    public StackPane getMainStackPane() { return mainStackPane; }
    public Theme getTheme() { return theme; }
    public void setTest(Question test) { question = test; }
    public Question getQuestion() { return this.question; }


}
