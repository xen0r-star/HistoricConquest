package com.historicconquest.historicconquest.controller.page;

import com.historicconquest.historicconquest.controller.page.game.ZoneInfoPanel;
import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.model.questions.Question;
import com.historicconquest.historicconquest.model.questions.Theme;
import com.historicconquest.historicconquest.controller.game.GameController;
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
    private int myAnswer = 0;

    private static QuestionController instance;
    private Question question;

    @FXML public Slider slider;
    @FXML public Button yesButton, noButton, answer1, answer2, answer3, answer4;
    @FXML public Label describeDifficult, theme_label, questionId;

    private static int difficultyQuestion;
    private static StackPane mainStackPane;
    private static Theme theme;


    public QuestionController() {
        instance = this;
        loadTheme();
    }

    public static QuestionController getInstance() {
        return instance;
    }

    public static void showChoiceDifficultPage() {
        FXMLLoader questionLoader = new FXMLLoader(
            Objects.requireNonNull(QuestionController.class.getResource("/view/fxml/question/ChoiceDifficultPage.fxml")));

        try {
            ZoneInfoPanel.getInstance().hide();
            StackPane difficultStackPane = questionLoader.load();

            QuestionController difficultController = questionLoader.getController();
            difficultController.setLabelsTheme();

            mainStackPane.getChildren().add(difficultStackPane);

        } catch (IOException e) {
            System.err.println("Erreur de chargement des fichiers FXML");
        }
    }

    @FXML
    public void confirmDifficult() {
        difficultyQuestion = (int) Math.round(slider.getValue());

        mainStackPane.getChildren().getLast().setVisible(true);
        destroyStackPane();
        showQuestionPage();
    }

    public static void loadTheme() {
        if (theme == null) {
            List<Theme> themeList = Question.getThemesFromJsonFile("/datas/Questions.json");
            Player player = GameController.getInstance().getCurrentPlayer();
            String targetLabel = player.getCurrentZone().getThemes().getLabel();

            theme = themeList.stream()
                    .filter(t -> t.name.getLabel().equalsIgnoreCase(targetLabel))
                    .findFirst()
                    .orElse(themeList.getFirst());
        }
    }

    public void setLabelsTheme(){
        theme_label.setText(String.valueOf(theme.name));

        Player player = GameController.getInstance().getCurrentPlayer();

        String LabelTheme = player.getCurrentZone().getThemes().getLabel() ;

        theme_label.setText(LabelTheme);

        switch(LabelTheme) {
            case "History" -> {
                theme_label.setPrefWidth(180);
                theme_label.setAlignment(Pos.CENTER);
            }
            case "Informatic" -> {
                theme_label.setPrefWidth(200);
                theme_label.setAlignment(Pos.CENTER);
            }
            case "Divertissment" -> {
                theme_label.setPrefWidth(260);
                theme_label.setAlignment(Pos.CENTER);
            }
            case "Tourism" -> {
                theme_label.setPrefWidth(160);
                theme_label.setAlignment(Pos.CENTER);
            }
        }

        difficultyQuestion = (int) Math.round(slider.getValue());
    }

    @FXML
    public static void destroyStackPane() {
        if (mainStackPane != null && mainStackPane.getChildren().size() > 1) {
            mainStackPane.getChildren().removeLast();
        }
    }

    @FXML
    public void hideConfirm() { mainStackPane.getChildren().getLast().setVisible(false);}

    @FXML
    public static void showQuestionPage() {
        FXMLLoader questionLoader = new FXMLLoader(
            Objects.requireNonNull(QuestionController.class.getResource("/view/fxml/question/QuestionPage.fxml")));

        try {
            StackPane questionStackPane = questionLoader.load();
            QuestionController questionController = questionLoader.getController();

            questionController.startQuestion();
            mainStackPane.getChildren().add(questionStackPane);

        } catch (IOException e) {
            System.err.println("Error loading QuestionPage");
        }
    }

    @FXML
    public void tellWhatAnswerChoice(ActionEvent e) {
        List.of(answer1, answer2, answer3, answer4).forEach(b -> b.getStyleClass().remove("button-selected"));

        Button selectedButton = (Button) e.getSource();
        selectedButton.getStyleClass().add("button-selected");

        if (e.getSource() == answer1) myAnswer = 1;
        else if(e.getSource() == answer2) myAnswer = 2;
        else if(e.getSource() == answer3) myAnswer = 3;
        else myAnswer = 4;
    }

    public void startQuestion() {
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
    public void checkWin() {
        boolean correct = question.isCorrectAnswer(myAnswer);

//        MultiplayerGameOverlay.requestQuestionResult(difficultyQuestion, correct);
        GameController.getInstance().applyQuestionResult(difficultyQuestion, correct);
        mainStackPane.getChildren().removeLast();
    }

    public static void setDifficultyQuestion(int difficultyQuestion) {
        QuestionController.difficultyQuestion = difficultyQuestion;
        showQuestionPage();
    }

    public static void setMainStackPane(StackPane stackPane) { mainStackPane = stackPane; }
    public StackPane getMainStackPane() { return mainStackPane; }
    public Theme getTheme() { return theme; }
    public void setTest(Question test) { question = test; }
    public Question getQuestion() { return this.question; }
}
