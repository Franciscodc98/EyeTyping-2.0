package com.eyetyping.eyetyping2.customComponets;

import javafx.scene.control.Button;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GroupButton extends Button {

    public GroupButton(String text){
        super(text);
        loadCss();
    }


    public void setFocussed(boolean focussed){
        if(focussed)
            setStyle("""
                    -fx-font-size:30;
                    -fx-background-color: palegreen;
                    -fx-border-width: 3 3 3 3;
                    """);
        else
            setStyle("""
                    -fx-font-size:20;
                    -fx-background-color: white;
                    -fx-border-width: 1 1 1 1;
                    """);
    }

    private void loadCss(){
            getStyleClass().add("group-button");
    }


}