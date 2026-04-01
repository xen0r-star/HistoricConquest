package com.historicconquest.historicconquest.model.questions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Question {

    private static final Logger logger = LoggerFactory.getLogger(Question.class);

    public Question(){

    }

    public static String readFile(String path) {
        StringBuilder theme = null;
        try (InputStream is = QuestionPage.class.getResourceAsStream(path);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            theme = new StringBuilder();
            String line = "";
            while (line != null) {
                theme.append(line);
                line = bufferedReader.readLine();
            }

        } catch (NullPointerException e) {
            logger.error("The file {} is not found in classpath!", path, e);

        } catch (IOException e) {
            logger.error("I/O error while reading questions file {}", path, e);
        }

        return theme.toString();
    }


    public static Theme getThemeFromJsonFile(String file){
        String json = readFile(file);
        Theme this_theme = new Theme();
        List<Theme> themes = null;
        if (json != null) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);

            try {
                themes = mapper.readValue(
                        json,
                        new TypeReference<List<Theme>>() {
                        }
                );

            } catch (Exception e) {
                logger.error("Failed to parse theme JSON from file {}", file, e);
                return null;
            }
        }

        if (themes == null || themes.isEmpty()) {
            logger.warn("No themes found in JSON file {}", file);
            return null;
        }

        this_theme = themes.getFirst();
        return this_theme;
    }

}
