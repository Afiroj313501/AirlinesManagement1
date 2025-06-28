package com.example.airlinesmanagement1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        // Login window
        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Scene loginScene = new Scene(loginLoader.load(), 700, 600);
        primaryStage.setTitle("Login Page");
        primaryStage.setScene(loginScene);
        primaryStage.show();

        // Admin window
        FXMLLoader adminLoader = new FXMLLoader(getClass().getResource("Admin_panel.fxml"));
        Scene adminScene = new Scene(adminLoader.load(), 900, 700);
        Stage adminStage = new Stage();
        adminStage.setTitle("Admin Panel");
        adminStage.setScene(adminScene);
        adminStage.show();
    }


    public static void main(String[] args) {
        launch();
    }
}
