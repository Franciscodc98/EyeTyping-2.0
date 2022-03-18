package com.eyetyping.eyetyping2.customComponets;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActionButton extends Button {

    private final String action;

    public ActionButton(String action){
        super(action);
        this.action = action;
    }




}