package com.eyetyping.eyetyping2.customComponets;

import javafx.scene.control.Button;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SuggestedLetterButton extends Button {

    private Button parentButton;
    private String groupName;

    public SuggestedLetterButton(String groupName){
        super();
        parentButton = new Button();
        this.groupName = groupName;
    }



}