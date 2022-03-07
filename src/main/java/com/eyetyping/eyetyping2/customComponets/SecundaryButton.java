package com.eyetyping.eyetyping2.customComponets;

import javafx.scene.control.Button;
import lombok.Getter;

@Getter
public class SecundaryButton extends Button {

    private final Button parentButton;

    public SecundaryButton(String text, Button parentButton){
        super(text);
        this.parentButton = parentButton;
    }


}