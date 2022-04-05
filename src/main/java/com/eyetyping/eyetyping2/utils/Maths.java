package com.eyetyping.eyetyping2.utils;

public class Maths {

    public static double normalizeBetween0and1(double min, double max, double value){
        return ((value - min)/(max - min));
    }




}