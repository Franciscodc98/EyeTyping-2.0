package com.eyetyping.eyetyping2.customComponets;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.Setter;

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
    }

    public static SecondaryButton asRoot(String text){
        return new SecondaryButton(text, null);
    }

    public static SecondaryButton asRootNoArgs(){
        return new SecondaryButton("", null);
    }

    private void addContent(){
        super.getChildren().add(progressBar);
        super.getChildren().add(label);
    }

    private void setListeners(){
        super.heightProperty().addListener((observable, oldValue, newValue) -> {
            progressBar.setPrefHeight(newValue.doubleValue());
        });
        super.widthProperty().addListener((observable, oldValue, newValue) -> {
            progressBar.setPrefWidth(newValue.doubleValue());
        });
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


}