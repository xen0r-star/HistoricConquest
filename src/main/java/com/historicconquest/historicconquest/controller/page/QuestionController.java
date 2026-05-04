package com.historicconquest.historicconquest.controller.page;

import java.io.IOException;
import java.util.*;

import com.historicconquest.historicconquest.controller.game.GameController;
import com.historicconquest.historicconquest.controller.game.GameNetworkService;
import com.historicconquest.historicconquest.controller.game.MultiplayerGameOverlay;
import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.model.questions.Question;
import com.historicconquest.historicconquest.model.questions.Theme;
import com.historicconquest.historicconquest.model.questions.TypeThemes;

import com.historicconquest.historicconquest.service.network.RoomService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;

public class QuestionController {
    private static QuestionController instance;
    private static StackPane mainStackPane;
    private static List<Theme> themes;
    private static int difficultyQuestion;
    private String currentNetworkQuestionId;

    private Question question;
    private Theme theme;
    private String myAnswer;

    @FXML public Slider slider;
    @FXML public Button answer1, answer2, answer3, answer4;
    @FXML public Label describeDifficult, theme_label, questionId;



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

        GameController gc = GameController.getInstance();
        Zone target = gc.getTargetZone();

        GameController.getInstance().setCurrentDifficulty(difficultyQuestion);

        MultiplayerGameOverlay.requestZoneAction(
            gc.getPendingAction(), target
        );


        mainStackPane.getChildren().getLast().setVisible(true);
        destroyStackPane();

        if (!GameNetworkService.isEnabled()) {
            showQuestionPage();
        }
    }

    public static Theme findCurrentTheme() {
        if (themes.isEmpty()) throw new IllegalStateException("No themes available");

        String targetLabel = GameController.getInstance().getTargetZone().getThemes().getLabel();

        return themes.stream()
                .filter(t -> t.getName().getLabel().equalsIgnoreCase(targetLabel))
                .findFirst()
                .orElse(themes.isEmpty() ? null : themes.getFirst());
    }

    public void setLabelsTheme(){
        theme_label.setText(String.valueOf(theme.getName()));

        Player player = GameController.getInstance().getCurrentPlayer();

        TypeThemes LabelTheme = player.getCurrentZone().getThemes();
        theme_label.setText(LabelTheme.getLabel());

        switch(LabelTheme) {
            case MYSTERY -> {
                theme_label.setPrefWidth(180);
                theme_label.setAlignment(Pos.CENTER);
            }
            case INFORMATICS -> {
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
            System.err.println("Error loading QuestionPage" + e);
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

    public static void showQuestionPage(String questionId, String question, List<String> choices) {
        FXMLLoader questionLoader = new FXMLLoader(
                Objects.requireNonNull(QuestionController.class.getResource("/view/fxml/question/QuestionPage.fxml")));

        try {
            StackPane questionStackPane = questionLoader.load();
            QuestionController controller = questionLoader.getController();

            controller.theme = findCurrentTheme();
            controller.currentNetworkQuestionId = questionId;
            controller.setTextQuestionPage(question, choices);

            mainStackPane.getChildren().add(questionStackPane);

        } catch (IOException e) {
            System.err.println("Error loading QuestionPage" + e);
        }
    }


    public void startQuestion() {
        List<Question> listQuestion = new ArrayList<>();
        for(Question themeQuestion : theme.getQuestions()){
            if(themeQuestion.difficulty() == difficultyQuestion){
                listQuestion.add(themeQuestion);
            }
        }

        if (listQuestion.isEmpty()) {
            throw new IllegalStateException("No question found for theme " + theme.getName() + " and difficulty " + difficultyQuestion);
        }

        int random = (int) (Math.random() * listQuestion.size());
        Question selectedQuestion = listQuestion.get(random);
        this.setQuestion(selectedQuestion);

        setTextQuestionPage(selectedQuestion.question(), selectedQuestion.choices());
    }

    public void setTextQuestionPage(String question, List<String> choices) {
        questionId.setText(question);
        answer1.setText(choices.get(0));
        answer2.setText(choices.get(1));
        answer3.setText(choices.get(2));
        answer4.setText(choices.get(3));
    }

    @FXML
    public void checkWin() {
        if (GameNetworkService.isEnabled() && currentNetworkQuestionId != null) {
            RoomService.sendGameAction(
                "SUBMIT_ANSWER",
                Map.of("questionId", currentNetworkQuestionId,  "answer", myAnswer)
            );

        } else if (question != null) {
            boolean correct = question.isCorrectAnswer(myAnswer);
            GameController.getInstance().applyQuestionResult(difficultyQuestion, correct);
        }

        mainStackPane.getChildren().removeLast();
    }

    public static void setDifficultyQuestion(int difficultyQuestion) {
        QuestionController.difficultyQuestion = difficultyQuestion;
        showQuestionPage();
    }

    public static void setMainStackPane(StackPane stackPane) { mainStackPane = stackPane; }

    public void setQuestion(Question question) { this.question = question; }

    public static void setThemes(List<Theme> themes) {
        QuestionController.themes = themes;
    }
}
