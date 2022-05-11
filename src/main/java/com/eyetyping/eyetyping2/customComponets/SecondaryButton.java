package com.eyetyping.eyetyping2.customComponets;

import javafx.application.Platform;
import javafx.scene.control.Button;
import lombok.Getter;
import lombok.Setter;

import java.util.Timer;
import java.util.TimerTask;

@Getter
@Setter
public class SecondaryButton extends Button {

    private Button parentButton;
    private String groupName;
    private boolean inFocus = false;

    public SecondaryButton(String text){
        super(text);
        parentButton = new Button();
        getStyleClass().add("secondary-button");
    }

    public SecondaryButton(){
        parentButton = new Button();
        getStyleClass().add("secondary-button");
    }

    private void setButtonColorGreen(boolean setGreen){
        if (setGreen){
            setStyle("""
                      -fx-font-size:25;
                      -fx-border-color:black;
                      -fx-border-width: 3 3 3 3;
                      -fx-background-color: palegreen;
                    """);
        }else{
            setStyle("""
                      -fx-font-size:25;
                      -fx-border-color:black;
                      -fx-border-width: 3 3 3 3;
                      -fx-background-colo: white;
                    """);
        }
    }

    public void setFocussed(boolean focussed){
        inFocus = focussed;
        if(!getText().equals("Del word") && !getText().equals("Del letter")){
            if(focussed) {
                setStyle("""
                      -fx-font-size:25;
                      -fx-border-color:black;
                      -fx-border-width: 3 3 3 3;
                    """);
            }
            else {
                setStyle("""
                       -fx-font-size:20;
                       -fx-border-color:black;
                       -fx-border-width: 1 1 1 1;
                    """);
            }
        }else{
            if(focussed) {
                setStyle("""
                      -fx-font-size:20;
                      -fx-border-color:black;
                      -fx-border-width: 3 3 3 3;
                    """);
            }
            else {
                setStyle("""
                       -fx-font-size:20;
                       -fx-border-color:black;
                       -fx-border-width: 1 1 1 1;
                    """);
            }
        }
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