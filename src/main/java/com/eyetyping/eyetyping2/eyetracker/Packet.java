package com.eyetyping.eyetyping2.eyetracker;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class Packet implements Serializable {

    private String category;
    private String request;
    private int statuscode;
    private Values values;

    @Data
    @NoArgsConstructor
    public static class Avg{
        private double x;
        private double y;
    }

    @Data
    @NoArgsConstructor
    public static class Pcenter{
        private double x;
        private double y;
    }

    @Data
    @NoArgsConstructor
    public static class Raw{
        private double x;
        private double y;
    }

    @Data
    @NoArgsConstructor
    public static class Lefteye{
        private Avg avg;
        private Pcenter pcenter;
        private double psize;
        private Raw raw;
    }

    @Data
    @NoArgsConstructor
    public static class Righteye{
        private Avg avg;
        private Pcenter pcenter;
        private double psize;
        private Raw raw;
    }

    @Data
    @NoArgsConstructor
    public static class Frame{
        private Avg avg;
        private boolean fix;
        private Lefteye lefteye;
        private Raw raw;
        private Righteye righteye;
        private int state;
        private int time;
        private String timestamp;
    }

    @Data
    @NoArgsConstructor
    public static class Values{
        private Frame frame;
    }


}