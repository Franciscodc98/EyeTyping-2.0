module com.eyetyping.eyetyping2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires lombok;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.opencsv;

    opens com.eyetyping.eyetyping2 to javafx.fxml;
    exports com.eyetyping.eyetyping2;
    exports com.eyetyping.eyetyping2.controllers;
    exports com.eyetyping.eyetyping2.eyetracker;
    exports com.eyetyping.eyetyping2.utils;
    exports com.eyetyping.eyetyping2.customComponets;
    exports com.eyetyping.eyetyping2.enums;
    opens com.eyetyping.eyetyping2.controllers to javafx.fxml;
}