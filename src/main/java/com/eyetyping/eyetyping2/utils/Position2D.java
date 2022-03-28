package com.eyetyping.eyetyping2.utils;

import com.eyetyping.eyetyping2.eyetracker.Packet;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Position2D {

    private double x;
    private double y;

    public Position2D(Packet packet){
        x = packet.getValues().getFrame().getAvg().getX();
        y = packet.getValues().getFrame().getAvg().getY();
    }



    @Override
    public String toString() {
        return "x=" + x +
                ", y=" + y +
                '}';
    }
}