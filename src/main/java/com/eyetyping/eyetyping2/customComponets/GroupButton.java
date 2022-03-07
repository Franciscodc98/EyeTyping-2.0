package com.eyetyping.eyetyping2.customComponets;

import javafx.scene.control.Button;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GroupButton extends Button {

    private boolean focussed = false;

    public GroupButton(String text){
        super(text);
        updateFeedback();
    }


    public void updateFeedback(){
        if(focussed){
            this.setStyle("-fx-background-color: #e6ffff; ");
        }else {
            this.setStyle("-fx-background-color: #4db8ff; ");
        }
    }


}