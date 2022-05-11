package com.eyetyping.eyetyping2.customComponets;

import com.eyetyping.eyetyping2.enums.GroupNames;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReverseCrossingButtons extends Button {

    private static double margin = 15;
    private SecondaryButton parentButton;
    private boolean reverseCrossing;

    public ReverseCrossingButtons(String text, SecondaryButton parentButton){
        super(text);
        this.parentButton = parentButton;
        getStyleClass().add("reverse-crossing-button");
        reverseCrossing = false;
        setOnMouseEntered(this::reverseCrossingEnterEvent);
        setOnMouseExited(this::reverseCrossingExitEvent);
            setPrefSize(parentButton.getWidth()/2, parentButton.getPrefHeight()/2);
            if(!parentButton.getGroupName().equals(GroupNames.WORDS_ROW.getGroupName())){
                if(getText().equals("SPACE") || getText().equals("Del letter") || getText().equals("Del word")){
                    setPrefSize(parentButton.getWidth()*0.75, parentButton.getPrefHeight()*0.66);
                    setLayoutX(parentButton.getLayoutX() + +parentButton.getPrefWidth() + margin);
                    setLayoutY(parentButton.getLayoutY() + parentButton.getPrefHeight()*0.16);
                }else {
                    setLayoutX((parentButton.getLayoutX() + parentButton.getLayoutBounds().getCenterX()) - (getPrefWidth() / 2));
                    setLayoutY(parentButton.getLayoutY() - getPrefHeight() - margin);
                }
            }else{
                setLayoutX((parentButton.getLayoutX() + parentButton.getLayoutBounds().getCenterX())-(getPrefWidth()/2));
                setLayoutY(parentButton.getLayoutY() + parentButton.getPrefHeight()+margin);
            }
    }

    private void reverseCrossingEnterEvent(MouseEvent mouseEvent) {
        reverseCrossing = true;
        setFocussed(true);
    }
    private void reverseCrossingExitEvent(MouseEvent mouseEvent){
        setFocussed(false);
    }

    public static double getMargin(){
        return margin;
    }

    public void setFocussed(boolean focussed){
        if(!getText().equals("Del word") && !getText().equals("Del letter")){
            if(focussed) {
                setStyle("""
                      -fx-font-size:20;
                      -fx-border-color:black;
                      -fx-border-width: 3 3 3 3;
                      -fx-background-color: palegreen;
                    """);
            }
            else {
                setStyle("""
                       -fx-font-size:15;
                       -fx-border-color:black;
                       -fx-border-width: 1 1 1 1;
                       -fx-background-color: palegreen;
                    """);
            }
        }else{
            if(focussed) {
                setStyle("""
                      -fx-font-size:15;
                      -fx-border-color:black;
                      -fx-border-width: 3 3 3 3;
                      -fx-background-color: palegreen;
                    """);
            }
            else {
                setStyle("""
                       -fx-font-size:15;
                       -fx-border-color:black;
                       -fx-border-width: 1 1 1 1;
                       -fx-background-color: palegreen;
                    """);
            }
        }
    }

    public void clearBackground(){
            setStyle("""
                       -fx-font-size:15;
                       -fx-border-color:black;
                       -fx-border-width: 1 1 1 1;
                       -fx-background-color: white;
                    """);
    }


}