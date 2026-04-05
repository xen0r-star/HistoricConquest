package com.historicconquest.historicconquest.model.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public record Question(String question, int difficulty, List<String> choices, int answer) {
    private static final int NUMBER_CHOICE = 4;

    @JsonCreator
    public Question(
            @JsonProperty("question") String question,
            @JsonProperty("difficulty") int difficulty,
            @JsonProperty("choices") List<String> choices,
            @JsonProperty("answer") int answer
    ) {
        this.question = question;
        this.difficulty = difficulty;

        if (choices == null || choices.size() != NUMBER_CHOICE) {
            throw new IllegalArgumentException("Choices must be a list of " + NUMBER_CHOICE + " items.");
        }

        this.choices = choices;
        this.answer = answer;
    }

    public static List<Theme> getThemesFromJsonFile(String file) {
        ObjectMapper objectMapper = new ObjectMapper();

        try (InputStream is = Question.class.getResourceAsStream(file)) {
            if (is == null) return new ArrayList<>();

            return objectMapper.readValue(is, new TypeReference<>() {
            });

        } catch (IOException e) {
            return new ArrayList<>();
        }
    }


    public boolean isCorrectAnswer(int answer) {
        return this.answer == answer;
    }
}