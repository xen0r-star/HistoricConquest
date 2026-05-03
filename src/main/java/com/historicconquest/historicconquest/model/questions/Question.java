package com.historicconquest.historicconquest.model.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public record Question(
    String theme,
    String subject,
    int difficulty,
    String question,
    String answer,
    List<String> choices
) {
    private static final Logger logger = LoggerFactory.getLogger(Question.class);
    private static final int NUMBER_CHOICE = 4;

    @JsonCreator
    public Question(
        @JsonProperty("theme") String theme,
        @JsonProperty("subject") String subject,
        @JsonProperty("difficulty") int difficulty,
        @JsonProperty("question") String question,
        @JsonProperty("answer") String answer,
        @JsonProperty("choices") List<String> choices
    ) {
        this.theme = theme;
        this.subject = subject;
        this.difficulty = difficulty;
        this.question = question;
        this.answer = answer;
        this.choices = choices;

        if (choices == null || choices.size() != NUMBER_CHOICE) {
            throw new IllegalArgumentException("Choices must be a list of " + NUMBER_CHOICE + " items.");
        }
    }

    public static List<Theme> getThemesFromJsonFile(String file) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Theme> themesMap = new LinkedHashMap<>();

        try (InputStream is = Question.class.getResourceAsStream(file)) {
            if (is == null) return new ArrayList<>();

            List<Question> questions = objectMapper.readValue(is, new TypeReference<>() { });

            for (Question question : questions) {
                themesMap.computeIfAbsent(question.theme(), name ->
                    new Theme(TypeThemes.fromString(name))
                );

                themesMap.get(question.theme()).addQuestion(question);
            }

        } catch (IOException e) {
            logger.error("Error loading questions from file: {}", file);
            return new ArrayList<>();
        }

        return new ArrayList<>(themesMap.values());
    }


    public boolean isCorrectAnswer(String answer) {
        return Objects.equals(this.answer, answer);
    }
}