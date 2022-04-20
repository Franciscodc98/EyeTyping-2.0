package com.eyetyping.eyetyping2.customComponets;

import com.eyetyping.eyetyping2.utils.GlobalVariables;
import javafx.scene.control.Button;
import javafx.scene.text.Font;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteButton extends Button {

    private String action = "";
    private boolean reversing = false;

    public DeleteButton(){
        super("delete");
        loadCss();

    }

    private void loadCss(){
        getStylesheets().add(getClass().getResource("/css/mainCss.css").toExternalForm());
        getStyleClass().add("secundary-button");
        applyCss();
    }



}