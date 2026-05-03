package com.historicconquest.server.service;

import com.historicconquest.server.model.questions.Question;
import com.historicconquest.server.model.questions.Theme;
import com.historicconquest.server.model.questions.TypeThemes;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionService {
    private List<Theme> themes;


    @PostConstruct
    public void init() {
        this.themes = Theme.loadThemesFromResource("/datas/questions.json");
    }

    public Question getRandomQuestion(TypeThemes typeTheme, int difficulty) {
        Theme theme = themes.stream()
                .filter(t -> t.getName().getLabel().equalsIgnoreCase(typeTheme.getLabel()))
                .findFirst()
                .orElse(themes.isEmpty() ? null : themes.getFirst());

        if (theme == null) return null;

        List<Question> listQuestion = new ArrayList<>();
        for(Question question : theme.getQuestions()) {
            if(question.difficulty() == difficulty) {
                listQuestion.add(question);
            }
        }

        int random = (int) (Math.random() * listQuestion.size());
        return listQuestion.get(random);
    }

    public Question getQuestion(String id) {
        for(Theme theme : themes) {
            for(Question question : theme.getQuestions()) {
                if(question.id().equals(id)) {
                    return question;
                }
            }
        }
        return null;
    }
}
