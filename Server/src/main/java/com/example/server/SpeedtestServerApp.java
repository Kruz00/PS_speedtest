package com.example.server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SpeedtestServerApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SpeedtestServerApp.class.getResource("server-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 550, 320);
        stage.setTitle("server - TCP_UDP_Speedtest");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}