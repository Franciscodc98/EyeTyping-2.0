package com.eyetyping.eyetyping2.customComponets;

import javafx.scene.control.Label;
import javafx.scene.text.Font;

public class TextWrittenLabel extends Label {

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