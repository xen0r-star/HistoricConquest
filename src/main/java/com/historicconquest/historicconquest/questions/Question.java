package com.historicconquest.historicconquest.questions;

import java.util.List;

public class Question {
    private static final int NUMBER_CHOICE = 4;

    private final String question;
    private final int difficulty;
    private final List<String> choices;
    private final int answer;

    public Question(String question, int difficulty, List<String> choices, int answer) {
        this.question = question;
        this.difficulty = difficulty;

        if (choices == null || choices.size() != NUMBER_CHOICE) {
            throw new IllegalArgumentException("Choices must be a list of " + NUMBER_CHOICE + " items.");
        }

        this.choices = choices;
        this.answer = answer;
    }
}
