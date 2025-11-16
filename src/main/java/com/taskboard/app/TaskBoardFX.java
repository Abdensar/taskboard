package com.taskboard.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class TaskBoardFX extends Application {

    @Override
    public void start(Stage stage) {
        Label hello = new Label("Hello JavaFX from Codespace !");
        Scene scene = new Scene(hello, 300, 200);
        stage.setTitle("TaskBoard");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}