package com.eyetyping.eyetyping2.customComponets;

import javafx.scene.control.Button;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteButton extends Button {

    private String action = "";
    private boolean reversing = false;

    public DeleteButton(){
        super("delete");
    }



}