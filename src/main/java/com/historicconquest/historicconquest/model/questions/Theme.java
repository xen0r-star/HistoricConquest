package com.historicconquest.historicconquest.model.questions;
import java.util.List;

public class Theme {
    public TypeThemes name;
    public List<String> describeDifficult;
    public List<Question> questions;

    public Theme(TypeThemes name) {
        this.name = name;
    }

    public Theme() { }


    public void addQuestion(Question question) {
        if (question != null && !questions.contains(question)) {
            questions.add(question);
        }
    }
}
