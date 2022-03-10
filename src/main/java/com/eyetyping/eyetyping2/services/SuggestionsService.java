package com.eyetyping.eyetyping2.services;

import com.eyetyping.eyetyping2.utils.GlobalVariables;
import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Data
public class SuggestionsService {

    private static SuggestionsService singleton = null;

    private List<String> allWords = new ArrayList<>();

    private SuggestionsService(){
        loadTop20Words();
    }

    private List<String> generateSuggestedSubstring(String substring){
        return allWords.stream().filter(word -> word.startsWith(substring)).toList();
    }

    private void loadTop20Words(){
        try {
            allWords = Files.readAllLines(Paths.get(GlobalVariables.WORDS_20K_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SuggestionsService getInstance(){
        if(singleton==null)
            singleton = new SuggestionsService();
        return singleton;
    }


    public static void main(String[] args) {
        getInstance().generateSuggestedSubstring("he").forEach(System.out::println);
    }

    public List<String> getSuggestionList(String text) {
        return allWords.stream().filter(word -> word.startsWith(text.toLowerCase())).toList();
    }
}