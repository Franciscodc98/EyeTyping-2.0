package com.eyetyping.eyetyping2.customComponets;

import javafx.scene.control.Label;
import javafx.scene.text.Font;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

@Getter
public class TextWrittenLabel extends Label {

    private final List<Character> words = new LinkedList<>();

    public TextWrittenLabel(){
        super();

        super.setText("Text you write will appear here");
        super.setFont(new Font("Arial", 20));
        super.setStyle("""
                        -fx-border-color: black;
                        -fx-border-width: 1;
                    """);
    }

}