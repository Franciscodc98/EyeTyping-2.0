package com.eyetyping.eyetyping2.customComponets;

import javafx.scene.control.Button;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
public class GroupButton extends Button {

    public GroupButton(String text){
        super(text);
        loadCss();
    }


    public void setFocussed(boolean focussed){
        if(focussed)
            this.setStyle("""
                    -fx-background-color: palegreen;
                    -fx-border-width: 3 3 3 3;
                    """);
        else
            this.setStyle("""
                    -fx-background-color: white;
                    -fx-border-width: 1 1 1 1;
                    """);
    }

    private void loadCss(){
        String resource = Objects.requireNonNull(getClass().getResource("/css/mainCss.css")).toExternalForm();
        if(resource!= null){
            getStylesheets().add(resource);
            getStyleClass().add("group-button");
        }
    }


}