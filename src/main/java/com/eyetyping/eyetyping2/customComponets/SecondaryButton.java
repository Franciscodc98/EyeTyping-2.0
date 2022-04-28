package com.eyetyping.eyetyping2.customComponets;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

@Getter
@Setter
public class SecondaryButton extends StackPane {

    private SecondaryButton parentButton;
    private ProgressBar progressBar;
    private String groupName;
    private Label label;

    public SecondaryButton(String text, SecondaryButton parentButton){
        super();
        this.parentButton = parentButton;
        label = new Label(text);
        progressBar = new ProgressBar(0);
        setListeners();
        addContent();
        getStyleClass().add("secundary-button");
    }

    public static SecondaryButton asRoot(String text){
        return new SecondaryButton(text, null);
    }

    public static SecondaryButton asRootNoArgs(){
        return new SecondaryButton("", null);
    }

    private void addContent(){
        getChildren().add(progressBar);
        getChildren().add(label);
    }

    private void setListeners(){
        heightProperty().addListener((observable, oldValue, newValue) -> progressBar.setPrefHeight(newValue.doubleValue()));
        widthProperty().addListener((observable, oldValue, newValue) -> progressBar.setPrefWidth(newValue.doubleValue()));
    }

    public void setProgress(double progress){
        progressBar.setProgress(progress);
    }

    public void setText(String text){
        label.setText(text);
    }

    public String getText(){
        return label.getText();
    }


    private void setButtonColorGreen(boolean setGreen){
        if (setGreen){
            progressBar.setStyle("-fx-control-inner-background: palegreen;");
        }else{
            progressBar.setStyle("-fx-control-inner-background: white;");
        }
    }

    public void setFocussed(boolean focussed){
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