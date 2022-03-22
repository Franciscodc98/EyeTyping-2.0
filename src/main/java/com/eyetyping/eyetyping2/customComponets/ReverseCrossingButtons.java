package com.eyetyping.eyetyping2.customComponets;

import com.eyetyping.eyetyping2.enums.GroupNames;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReverseCrossingButtons extends Button {

    private static double margin = 5;
    private final Button parentButton;
    private boolean reverseCrossing;

    public ReverseCrossingButtons(String text, Button parentButton){
        super(text);
        this.parentButton = parentButton;
        reverseCrossing = false;
        setOnMouseEntered(this::reverseCrossingEvent);
        if(parentButton instanceof SecundaryButton secundaryButton){
            setPrefSize(this.parentButton.getWidth()/2, this.parentButton.getPrefHeight()/2); //botao com metade do tamanho do pai
            if(!secundaryButton.getGroupName().equals(GroupNames.WORDS_ROW.getGroupName())){
                setLayoutX((parentButton.getLayoutX() + parentButton.getLayoutBounds().getCenterX())-(getPrefWidth()/2)); //centrar o reverse crossing button no botao pai
                setLayoutY(parentButton.getLayoutY()-getPrefHeight()-margin); //posicionar o Y acima do botao
            }else{
                setLayoutX((parentButton.getLayoutX() + parentButton.getLayoutBounds().getCenterX())-(getPrefWidth()/2)); //centrar o reverse crossing button acima do butao pai
                setLayoutY(parentButton.getLayoutY() + parentButton.getPrefHeight()+margin); //posicionar o Y abaixo do botao
            }
        }
        else if(parentButton instanceof ActionButton){
            setPrefSize(this.parentButton.getWidth() * 0.66, this.parentButton.getPrefHeight() * 0.66); //botao com 2/3 do tamanho do pai
            setLayoutX(parentButton.getLayoutX() - getPrefWidth() - margin); //centrar o reverse crossing button acima do butao pai
            setLayoutY(parentButton.getLayoutY() + parentButton.getPrefHeight() * (0.33/2));
        }
    }

    private void reverseCrossingEvent(MouseEvent mouseEvent) {
        reverseCrossing = true;
    }

    public static double getMargin(){
        return margin;
    }


}