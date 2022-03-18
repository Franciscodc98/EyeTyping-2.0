package com.eyetyping.eyetyping2.services;

import lombok.Getter;

import java.util.LinkedList;

@Getter
public class WrittingService {

    private static WrittingService singleton = null;

    private final LinkedList<Character> writtenText = new LinkedList<>();

    private WrittingService(){}

    public LinkedList<Character> addLetters(String letters) {
        for (char c: letters.toCharArray())
            writtenText.add(c);
        return writtenText;
    }

    public LinkedList<Character> addWord(String word){
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

    public LinkedList<Character> deleteLetter() {
        if(!writtenText.isEmpty())
            writtenText.removeLast();
        return writtenText;
    }

    public static WrittingService getInstance(){
        if(singleton==null)
            singleton = new WrittingService();
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

    private String getTextString() {
        StringBuilder sb = new StringBuilder();
        for (Character c: writtenText)
            sb.append(c);
        return sb.toString();
    }

    public static void main(String[] args) {
        WrittingService service = WrittingService.getInstance();
        service.addWord("Hello word I need there");
        service.addLetters("h");
        service.addWord("heyy");

        System.out.println(service.getTextString());
    }
}