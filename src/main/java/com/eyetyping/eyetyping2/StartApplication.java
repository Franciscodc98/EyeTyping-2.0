package com.eyetyping.eyetyping2;

import com.eyetyping.eyetyping2.controllers.KeyboardController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class StartApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(StartApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1500, 800);
        stage.setFullScreen(true);
        stage.setTitle("EyeTyping!");
        stage.setScene(scene);
        KeyboardController controller = fxmlLoader.getController();
        controller.setKeyListener(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch();
    }
}