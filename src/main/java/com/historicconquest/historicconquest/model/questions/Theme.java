package com.historicconquest.historicconquest.model.questions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Theme {
    private static final Logger logger = LoggerFactory.getLogger(Theme.class);

    private TypeThemes name;
    private List<Question> questions;

    public Theme(TypeThemes name) {
        this.name = name;
        this.questions = new ArrayList<>();
    }


    public static List<Theme> loadThemesFromResource(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Theme> themesMap = new LinkedHashMap<>();

        try (InputStream is = Theme.class.getResourceAsStream(filePath)) {
            if (is == null) {
                logger.warn("File not found: {}", filePath);
                return Collections.emptyList();
            }

            List<Question> allQuestions = objectMapper.readValue(is, new TypeReference<>() {});

            for (Question q : allQuestions) {
                themesMap.computeIfAbsent(q.theme(), themeName ->
                    new Theme(TypeThemes.fromString(themeName))
                ).addQuestion(q);
            }

        } catch (IOException e) {
            logger.error("Error loading themes from JSON: {}", filePath, e);
            return Collections.emptyList();
        }

        return new ArrayList<>(themesMap.values());
    }

    public void addQuestion(Question question) {
        if (question != null && !questions.contains(question)) {
            questions.add(question);
        }
    }


    public TypeThemes getName() {
        return name;
    }

    public void setName(TypeThemes name) {
        this.name = name;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
