package com.historicconquest.historicconquest.controller.page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.historicconquest.historicconquest.controller.game.GameController;
import com.historicconquest.historicconquest.controller.page.game.ZoneInfoPanel;
import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.model.questions.Question;
import com.historicconquest.historicconquest.model.questions.Theme;
import com.historicconquest.historicconquest.model.questions.TypeThemes;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;

public class QuestionController {
    private String myAnswer;

    private static QuestionController instance;
    private Question question;

    @FXML public Slider slider;
    @FXML public Button answer1, answer2, answer3, answer4;
    @FXML public Label describeDifficult, theme_label, questionId;

    private static int difficultyQuestion;
    private static StackPane mainStackPane;
    private Theme theme;


    public QuestionController() {
        instance = this;
    }

    public static QuestionController getInstance() {
        return instance;
    }

    public static void showChoiceDifficultPage() {
        FXMLLoader questionLoader = new FXMLLoader(
            Objects.requireNonNull(QuestionController.class.getResource("/view/fxml/question/ChoiceDifficultPage.fxml")));

        try {
            StackPane difficultStackPane = questionLoader.load();
            QuestionController controller = questionLoader.getController();

            controller.theme = findCurrentTheme();
            controller.setLabelsTheme();

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

    public static Theme findCurrentTheme() {
        List<Theme> themeList = Question.getThemesFromJsonFile("/datas/Questions.json");
        if (themeList.isEmpty()) return new Theme(TypeThemes.NONE);

        Player player = GameController.getInstance().getCurrentPlayer();
        String targetLabel = player.getCurrentZone().getThemes().getLabel();

        return themeList.stream()
                .filter(t -> t.name.getLabel().equalsIgnoreCase(targetLabel))
                .findFirst()
                .orElse(themeList.getFirst());
    }

    public void setLabelsTheme(){
        theme_label.setText(String.valueOf(theme.name));

        Player player = GameController.getInstance().getCurrentPlayer();

        TypeThemes LabelTheme = player.getCurrentZone().getThemes();
        theme_label.setText(LabelTheme.getLabel());

        switch(LabelTheme) {
            case MYSTERY -> {
                theme_label.setPrefWidth(180);
                theme_label.setAlignment(Pos.CENTER);
            }
            case INFORMATIC -> {
                theme_label.setPrefWidth(200);
                theme_label.setAlignment(Pos.CENTER);
            }
            case ENTERTAINMENT -> {
                theme_label.setPrefWidth(260);
                theme_label.setAlignment(Pos.CENTER);
            }
            case TOURISM -> {
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
    public static void showQuestionPage() {
        FXMLLoader questionLoader = new FXMLLoader(
            Objects.requireNonNull(QuestionController.class.getResource("/view/fxml/question/QuestionPage.fxml")));

        try {
            StackPane questionStackPane = questionLoader.load();
            QuestionController controller = questionLoader.getController();

            controller.theme = findCurrentTheme();
            controller.startQuestion();

            mainStackPane.getChildren().add(questionStackPane);

        } catch (IOException e) {
            System.err.println("Error loading QuestionPage"+e);
        }
    }

    @FXML
    public void tellWhatAnswerChoice(ActionEvent e) {
        List.of(answer1, answer2, answer3, answer4).forEach(b -> b.getStyleClass().remove("button-selected"));

        Button selectedButton = (Button) e.getSource();
        selectedButton.getStyleClass().add("button-selected");

        if (e.getSource() == answer1)     myAnswer = answer1.getText();
        else if(e.getSource() == answer2) myAnswer = answer2.getText();
        else if(e.getSource() == answer3) myAnswer = answer3.getText();
        else if(e.getSource() == answer4) myAnswer = answer4.getText();
    }

    public void startQuestion() {
        List<Question> listQuestion = new ArrayList<>();
        for(Question themeQuestion : theme.questions){
            if(themeQuestion.difficulty() == difficultyQuestion){
                listQuestion.add(themeQuestion);
            }
        }

        if (listQuestion.isEmpty()) {
            throw new IllegalStateException("No question found for theme " + theme.name + " and difficulty " + difficultyQuestion);
        }

        int random = (int) (Math.random() * listQuestion.size());
        Question selectedQuestion = listQuestion.get(random);
        questionId.setText(selectedQuestion.question());

        this.setQuestion(selectedQuestion);
        answer1.setText(selectedQuestion.choices().get(0));
        answer2.setText(selectedQuestion.choices().get(1));
        answer3.setText(selectedQuestion.choices().get(2));
        answer4.setText(selectedQuestion.choices().get(3));
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

    public void setQuestion(Question question) { this.question = question; }
}
