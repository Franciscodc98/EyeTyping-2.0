package com.eyetyping.eyetyping2.customComponets;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.Timer;
import java.util.TimerTask;

@Setter
@Getter
public class GroupButton extends Button {

    private boolean focussed = false;

    //split gaze margin
    private static final long SLIP_MARGIN = 250;

    private Timer timer = new Timer();
    private TimerTask slipMargin;

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