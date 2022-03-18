package com.eyetyping.eyetyping2.customComponets;

import javafx.scene.control.Button;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SecundaryButton extends Button {

    private Button parentButton;
    private String groupName;

    public SecundaryButton(String text){
        super(text);
        parentButton = new Button();
    }

    public SecundaryButton(){
        parentButton = new Button();
    }


}