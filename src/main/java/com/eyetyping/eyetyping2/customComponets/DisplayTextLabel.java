package com.eyetyping.eyetyping2.customComponets;

import com.eyetyping.eyetyping2.utils.GlobalVariables;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

@Getter
public class DisplayTextLabel extends Label {

    private List<String> wordsToWrite;
    private final String finishedDisplaying = "All done, good job!";

    public DisplayTextLabel(){
        super();
        super.setText("Text to write will appear here");
        super.setFont(new Font("Arial", 24));
        super.setStyle("""
                        -fx-border-color: black;
                        -fx-border-width: 1;
                        -fx-alignment: center
                    """);
        try {
            wordsToWrite = Files.readAllLines(Paths.get(GlobalVariables.PHRASES_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void displayPhrase(){
        String phrase = getPhrase();
        if(!phrase.isEmpty())
            super.setText(phrase);
        else
            super.setText(finishedDisplaying);

    }


    /**
     *
     * @return a phrase from a set of phrases or empty string if set is already completely used
     */
    private String getPhrase(){
        if(wordsToWrite.isEmpty()){
            return "";
        }
        int i = new Random().nextInt(wordsToWrite.size());
        String phrase = wordsToWrite.get(i);
        wordsToWrite.remove(i);
        return phrase;
    }



}