package com.historicconquest.historicconquest.questions;

import java.util.List;

public class Theme {
    private final TypeThemes name;
    private List<Question> questions;

    public Theme(TypeThemes name) {
        this.name = name;
    }

    public void addQuestion(Question question) {
        if (question != null && !questions.contains(question)) {
            questions.add(question);
        }
    }
}
