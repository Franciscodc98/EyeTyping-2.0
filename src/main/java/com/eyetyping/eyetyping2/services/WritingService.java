package com.eyetyping.eyetyping2.services;

import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Getter
public class WritingService {

    private static WritingService singleton = null;

    private final LinkedList<String> writtenPhrases = new LinkedList<>();

    private final LinkedList<Character> writtenText = new LinkedList<>();

    private WritingService(){}

    public List<Character> addLetters(String letters) {
        for (char c: letters.toCharArray())
            writtenText.add(c);
        return writtenText;
    }

    public List<Character> addWord(String word){
        int lastWordIndex = getLastWordIndex();
        if(lastWordIndex != 0){
            for(int i = writtenText.size() - 1; i > lastWordIndex; i--)
                writtenText.removeLast();
            for (char c: word.toCharArray())
                writtenText.add(c);
            writtenText.add(' ');
        }else{
            writtenText.clear();
            for (char c: word.toCharArray())
                writtenText.add(c);
            writtenText.add(' ');
        }
        return writtenText;
    }

    public List<Character> deleteLetter() {
        if(!writtenText.isEmpty())
            writtenText.removeLast();
        return writtenText;
    }

    public List<Character> deleteWord() {
        LinkedList<Character> result = new LinkedList<>();
        if(!writtenText.isEmpty()){
            String [] aux = getTextString().split(" ");
            for(int i = 0; i< aux.length-1; i++){
                for(char c: aux[i].toCharArray())
                    result.add(c);
                result.add(' ');
            }
        }
        writtenText.clear();
        writtenText.addAll(result);
        return result;
    }

    public int getLastWordLength(){
        int length = 0;
        String text = getTextString();
        if(!writtenText.isEmpty()){
            if(text.charAt(text.length()-1) == ' ')
                length++;
            String [] aux = text.split(" ");
            length += aux[aux.length-1].length();
        }
        return length;
    }

    public static WritingService getInstance(){
        if(singleton==null)
            singleton = new WritingService();
        return singleton;
    }

    public String getCurrentTypingWord(){
        String allText = getTextString();
        return allText.substring(allText.lastIndexOf(" ") + 1);
    }

    private int getLastWordIndex(){
        int index = getTextString().lastIndexOf(" ");
        if(index == -1){
            return 0;
        }else{
            return index;
        }
    }

    public String getTextString() {
        StringBuilder sb = new StringBuilder();
        for (Character c: writtenText)
            sb.append(c);
        return sb.toString();
    }

    public void nextPhrase(){
        writtenPhrases.add(getTextString());
        writtenText.clear();
    }

    public void clearWrittenText(){
        writtenText.clear();
    }

}