package com.historicconquest.server.model.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record Question(
        String id,
        String theme,
        String subject,
        int difficulty,
        String question,
        String answer,
        List<String> choices
) {
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
        this(UUID.randomUUID().toString(), theme, subject, difficulty, question, answer, choices);

        if (choices == null || choices.size() != NUMBER_CHOICE) {
            throw new IllegalArgumentException("Choices must be a list of " + NUMBER_CHOICE + " items.");
        }
    }

    public boolean isCorrectAnswer(String input) {
        return Objects.equals(this.answer, input);
    }
}
