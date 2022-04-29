package com.eyetyping.eyetyping2.customComponets;

import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

public class ReverseCrossingDelete extends Button{

    private static double margin = 5;
    private final DeleteButton parentButton;
    private boolean word = false;

    public ReverseCrossingDelete(DeleteButton parentButton, boolean word){
        super();
        this.parentButton = parentButton;
        this.word = word;
        getStyleClass().add("reverse-crossing-button");
        setOnMouseEntered(this::reverseCrossingEnterEvent);
        setOnMouseExited(this::reverseCrossingExitEvent);
        if(word){
            setPrefSize(parentButton.getWidth(), parentButton.getPrefHeight());
            setLayoutX(parentButton.getLayoutX() - getPrefWidth() - margin);
            setLayoutY(parentButton.getLayoutY());
            setText("word");
        }else{
            setPrefSize(parentButton.getWidth(), parentButton.getPrefHeight());
            setLayoutX(parentButton.getLayoutX() + getPrefWidth() + margin);
            setLayoutY(parentButton.getLayoutY());
            setText("letter");
        }
    }



    private void reverseCrossingEnterEvent(MouseEvent mouseEvent) {
        parentButton.setReversing(true);
        parentButton.setAction(getText());
        setFocussed(true);
    }

    private void reverseCrossingExitEvent(MouseEvent mouseEvent) {
        setFocussed(false);
    }

    public static double getMargin(){
        return margin;
    }


    public void refreshSize() {
        if(word){
            setPrefSize(parentButton.getWidth(), parentButton.getPrefHeight());
            setLayoutX(parentButton.getLayoutX() - getPrefWidth() - margin);
            setLayoutY(parentButton.getLayoutY());
        }else{
            setPrefSize(parentButton.getWidth(), parentButton.getPrefHeight());
            setLayoutX(parentButton.getLayoutX() + getPrefWidth() + margin);
            setLayoutY(parentButton.getLayoutY());
        }
    }

    public void setFocussed(boolean focussed){
        if(focussed)
            setStyle("""
                      -fx-font-size:20;
                      -fx-border-color:black;
                      -fx-border-width: 3 3 3 3;
                    """);
        else
            setStyle("""
                       -fx-font-size:15;
                       -fx-border-color:black;
                       -fx-border-width: 1 1 1 1;
                    """);
    }

}