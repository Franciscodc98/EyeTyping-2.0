package com.eyetyping.eyetyping2.customComponets;

import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

import java.util.function.Consumer;

public class ReverseCrossingDelete extends Button{

    private static double margin = 5;
    private final DeleteButton parentButton;
    private boolean word = false;

    public ReverseCrossingDelete(DeleteButton parentButton, Consumer<MouseEvent> mouseMove, boolean word){
        super();
        this.parentButton = parentButton;
        this.word = word;
        setOnMouseEntered(this::reverseCrossingEvent);
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



    private void reverseCrossingEvent(MouseEvent mouseEvent) {
        parentButton.setReversing(true);
        parentButton.setAction(getText());
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
}