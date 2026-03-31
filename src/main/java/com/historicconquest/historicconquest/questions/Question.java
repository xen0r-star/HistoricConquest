package com.historicconquest.historicconquest.questions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Question {


    public Question(){

    }

    public static String readFile(String path) {
        String theme = null;
        try (InputStream is = QuestionPage.class.getResourceAsStream(path);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            theme = "";
            String line = "";
            while (line != null) {
                theme += line;
                line = bufferedReader.readLine();
            }
        } catch (NullPointerException e) {
            System.err.println("The file " + path + " is not found in classpath!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return theme;
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

                // Test
                for (Theme t : themes) {
                    System.out.println("Theme: " + t.name);

                    for (String label : t.describeDifficult) {
                        System.out.println(" - " + label);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this_theme = themes.get(0);
    }

}
