package com.historicconquest.historicconquest.model.questions;
import java.util.ArrayList;
import java.util.List;

public class Theme {
    public TypeThemes name;
    public List<Question> questions;

    public Theme(TypeThemes name) {
        this.name = name;
        this.questions = new ArrayList<>();
    }

    public void addQuestion(Question question) {
        if (question != null && !questions.contains(question)) {
            questions.add(question);
        }
    }
}
