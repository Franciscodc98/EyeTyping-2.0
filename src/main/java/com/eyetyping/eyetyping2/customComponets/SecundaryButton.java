package com.eyetyping.eyetyping2.customComponets;

import javafx.scene.control.Button;
import lombok.Getter;

@Getter
public class SecundaryButton extends Button {

    private final Button parentButton;
    private String groupName = "secondRow";

    public SecundaryButton(String text, Button parentButton){
        super(text);
        this.parentButton = parentButton;
    }


}