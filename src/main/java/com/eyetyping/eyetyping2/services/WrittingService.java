package com.eyetyping.eyetyping2.services;

import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Getter
public class WrittingService {

    private static WrittingService singleton = null;

    private final LinkedList<String> writtenPhrases = new LinkedList<>();

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

    private String getTextStringFromCharList(List<Character> characterList) {
        StringBuilder sb = new StringBuilder();
        for (Character c: characterList)
            sb.append(c);
        return sb.toString();
    }

    public List<String> getWordsWrittenList(){
        return Arrays.stream(getTextString().split(" ")).toList();
    }

    public int getTotalWordsWritten(){
        int total = 0;
        for (String s: writtenPhrases) {
            String [] split = s.split(" ");
            if(!split[0].isEmpty())
                total += split.length;
        }
        return total;
    }

    public void nextPhrase(){
        writtenPhrases.add(getTextString());
        writtenText.clear();
    }


    public static void main(String[] args) {
        WrittingService service = WrittingService.getInstance();
        service.addWord("Hello word I need there");
        service.addLetters("h");
        service.addWord("heyy.");
        service.nextPhrase();
        service.addWord("Fcing shit");
        service.addLetters("h");
        service.addWord("hahahah.");
        service.nextPhrase();

        service.getWrittenPhrases().forEach(System.out::println);
        System.out.println(service.getTotalWordsWritten());
    }
}