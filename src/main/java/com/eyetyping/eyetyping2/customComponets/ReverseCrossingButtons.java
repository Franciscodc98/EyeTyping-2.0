package com.eyetyping.eyetyping2.customComponets;

import com.eyetyping.eyetyping2.enums.GroupNames;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

@Getter
@Setter
public class ReverseCrossingButtons extends Button {

    private static double margin = 15;
    private final Button parentButton;
    private boolean reverseCrossing;

    public ReverseCrossingButtons(String text, Button parentButton, Consumer<MouseEvent> mouseMove){
        super(text);
        this.parentButton = parentButton;
        reverseCrossing = false;
        setOnMouseEntered(this::reverseCrossingEvent);
        //setOnMouseMoved(mouseMove::accept);
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
        else if(parentButton instanceof DeleteButton){
            setPrefSize(this.parentButton.getWidth(), this.parentButton.getPrefHeight());
            setLayoutX(parentButton.getLayoutX() - getPrefWidth() - margin);
            setLayoutY(parentButton.getLayoutY());
        }
    }

    private void reverseCrossingEvent(MouseEvent mouseEvent) {
        reverseCrossing = true;
    }

    public static double getMargin(){
        return margin;
    }


}