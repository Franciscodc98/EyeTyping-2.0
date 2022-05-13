package com.eyetyping.eyetyping2.services;

import com.eyetyping.eyetyping2.utils.GlobalVariables;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class SuggestionsService {

    private static SuggestionsService singleton = null;

    private List<String> allWords = new ArrayList<>();

    private SuggestionsService(){
        loadTop20kWords();
    }

    public List<String> getSuggestionList(String text) {
        return allWords.stream().filter(word -> word.startsWith(text.toLowerCase())).filter(word -> word.length() > 1).map(String::toUpperCase).toList();
    }

    public List<String> getSuggestionListForSuggestedLetters(String typedText, String buttonText) {
        String currentTextWithButton = typedText + buttonText;
        return allWords.stream()
                .filter(word -> word.startsWith(currentTextWithButton.toLowerCase()))
                .filter(word -> word.length() > 1)
                .map(word -> word.substring(typedText.length()))
                .map(String::toUpperCase).toList();
    }

    public List<String> sortedMostCommonSubstrings(List<String> allWords, int indexes){
        LinkedHashMap<String, Integer> count = new LinkedHashMap<>();
        for (String word: allWords) {
            if (word.length() > indexes){
                String wordAux = word.substring(0, indexes);
                if(!count.containsKey(wordAux))
                    count.put(wordAux, 1);
                else
                    count.put(wordAux,count.get(wordAux)+1);
            }
        }
        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(count.entrySet());
        sortedList.sort((o1, o2) -> -Integer.compare(o1.getValue(), o2.getValue()));
        return sortedList.stream().map(Map.Entry::getKey).toList();
    }

    private void loadTop20kWords(){
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



}