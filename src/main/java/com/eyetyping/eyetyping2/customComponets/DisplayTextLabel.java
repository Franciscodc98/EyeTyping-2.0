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


    public DisplayTextLabel(){
        super();
        super.setText("Text to write will appear here");
        super.setFont(new Font("Arial", 24));
        super.setStyle("""
                        -fx-border-color: black;
                        -fx-border-width: 1;
                        -fx-alignment: center
                    """);
    }



}