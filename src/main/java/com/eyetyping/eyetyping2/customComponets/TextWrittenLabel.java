package com.eyetyping.eyetyping2.customComponets;

import javafx.scene.control.Label;
import javafx.scene.text.Font;

public class TextWrittenLabel extends Label {

    public TextWrittenLabel(){
        super();
        setText("Text you write will appear here");
        setFont(new Font("Arial", 20));
        setStyle("""
                        -fx-border-color: black;
                        -fx-border-width: 3;
                    """);
    }

    public void setTimerOnFeedback(boolean onOff){
        if(onOff){
            setStyle("""
                        -fx-border-color: green;
                        -fx-border-width: 3;
                    """);
        }else{
            setStyle("""
                        -fx-border-color: red;
                        -fx-border-width: 3;
                    """);
        }
    }


}