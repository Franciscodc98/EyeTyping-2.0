module com.eyetyping.eyetyping2 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires lombok;

    opens com.eyetyping.eyetyping2 to javafx.fxml;
    exports com.eyetyping.eyetyping2;
    exports com.eyetyping.eyetyping2.controllers;
    opens com.eyetyping.eyetyping2.controllers to javafx.fxml;
}