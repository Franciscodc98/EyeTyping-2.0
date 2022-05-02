package com.eyetyping.eyetyping2.services;

import com.eyetyping.eyetyping2.controllers.KeyboardController;
import com.eyetyping.eyetyping2.utils.Position2D;
import com.eyetyping.eyetyping2.utils.ShiftList;
import com.eyetyping.eyetyping2.utils.WindowDimensions;
import javafx.application.Platform;
import javafx.scene.control.Label;
import lombok.Data;

@Data
public class MouseService {

    private static MouseService singleton = null;

    private ShiftList<Position2D> lastMouseCoords = new ShiftList<>(5);
    private Label label;
    private WindowDimensions windowDimensions;
    private KeyboardController keyboardController;

    private MouseService(){}


    public void updateList(Position2D pos){
        lastMouseCoords.add(fixPositionBounds(pos));
        if(keyboardController != null){
            Position2D avg = averagePosition();
            Platform.runLater(() -> {
                //keyboardController.refreshNewMouse(avg);
                label.setLayoutX(avg.getX());
                label.setLayoutY(avg.getY());
                //System.out.println("Label:("+avg.getX() +", " + avg.getY()+ ")");
        });
        }
    }

    public Position2D averagePosition(){
        if(lastMouseCoords.isFull()){
            double xAverage = 0;
            double yAverage = 0;
            for (Position2D pos: lastMouseCoords) {
                xAverage+= pos.getX();
                yAverage+= pos.getY();
            }
            return new Position2D(xAverage/(double)lastMouseCoords.getMaxSize(), yAverage/(double)lastMouseCoords.getMaxSize());
        }else
            return new Position2D(0, 0);
    }

    public static MouseService getSingleton() {
        if(singleton == null)
            singleton = new MouseService();
        return singleton;
    }

    private Position2D fixPositionBounds(Position2D pos){
        if(pos.getX() < 0)
            pos.setX(0);
        if(pos.getX() > windowDimensions.getWidth())
            pos.setX(windowDimensions.getWidth());
        if(pos.getY() < 0)
            pos.setY(0);
        if(pos.getY() > windowDimensions.getHeight())
            pos.setY(windowDimensions.getHeight());
        return pos;
    }



}