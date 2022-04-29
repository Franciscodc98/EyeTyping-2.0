package com.eyetyping.eyetyping2.customComponets;

import javafx.application.Platform;
import javafx.scene.control.Button;
import lombok.Getter;
import lombok.Setter;

import java.util.Timer;
import java.util.TimerTask;

@Getter
@Setter
public class DeleteButton extends Button {

    private String action = "";
    private boolean reversing = false;
    private boolean inFocus = false;

    public DeleteButton(){
        super("delete");
        getStyleClass().add("secondary-button");
    }

    private void setButtonColorGreen(boolean setGreen){
        if (setGreen){
            setStyle("-fx-background-color: palegreen;");
        }else{
            setStyle("-fx-background-colo: white;");
        }
    }

    public void setFocussed(boolean focussed){
        inFocus = focussed;
        if(focussed)
            setStyle("""
                      -fx-font-size:25;
                      -fx-border-color:black;
                      -fx-border-width: 3 3 3 3;
                    """);
        else
            setStyle("""
                       -fx-font-size:20;
                       -fx-border-color:black;
                       -fx-border-width: 1 1 1 1;
                    """);
    }

    public void updateBackgroundColor(){
        setButtonColorGreen(true);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> setButtonColorGreen(false));
            }
        }, 500);
    }



}