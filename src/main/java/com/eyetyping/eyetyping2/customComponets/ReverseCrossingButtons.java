package com.eyetyping.eyetyping2.customComponets;

import javafx.scene.control.Button;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReverseCrossingButtons extends Button {

    private double margin = 5;
    private final Button parentButton;

    public ReverseCrossingButtons(String text, Button parentButton){
        super(text);
        this.parentButton = parentButton;
        setPrefSize(this.parentButton.getWidth()/2, this.parentButton.getPrefHeight()/2); //botao com metade do tamanho do pai
        setLayoutX((parentButton.getLayoutX() + parentButton.getLayoutBounds().getCenterX())-(getPrefWidth()/2)); //centrar o reverse crossing button acima do butao pai
        setLayoutY(parentButton.getLayoutY()-getPrefHeight()-margin); //posicionar o Y
    }




}