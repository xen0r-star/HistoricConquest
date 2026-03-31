package com.historicconquest.historicconquest.questions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Theme {
    private TypeThemes name;
    private List<String> describeDifficult;
    private List<Question> questions;

    public Theme(TypeThemes name) {
        this.name = name;
    }

    /*
    public void addQuestion(Question question) {
        if (question != null && !questions.contains(question)) {
            questions.add(question);
        }
    }*/


}
