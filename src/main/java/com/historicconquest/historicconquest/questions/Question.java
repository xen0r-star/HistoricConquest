package com.historicconquest.historicconquest.questions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Question {


    public Question(){

    }

    public static String readFile(String path){
        String theme = null;
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(path))){
            theme = "";
            String line = "";
            while (line != null){
                theme += line;
                line = bufferedReader.readLine();
            }
        }
        catch(FileNotFoundException e){
            System.err.println("The file " + path + "is not found !");
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return theme;
    }

/*
    public Theme getThemeFromJsonFile(String file){
        String json = readFile(file);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
        List <Theme> themes = null;
        try {
            themes = mapper.readValue(
                    json,
                    new TypeReference<List<Theme>>() {}
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
        return (Theme) themes;
    }*/

}
