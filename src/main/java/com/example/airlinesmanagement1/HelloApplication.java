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
        try {
            // Load and show Login window
            FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Scene loginScene = new Scene(loginLoader.load(), 700, 600);
            Stage loginStage = new Stage();
            loginStage.setTitle("Airlines Management System - Login");
            loginStage.setScene(loginScene);
            loginStage.setResizable(false);
            loginStage.show();
            
            // Load and show Admin Panel window
            FXMLLoader adminLoader = new FXMLLoader(getClass().getResource("Admin_panel.fxml"));
            Scene adminScene = new Scene(adminLoader.load(), 1200, 800);
            Stage adminStage = new Stage();
            adminStage.setTitle("Admin Panel - Airlines Management");
            adminStage.setScene(adminScene);
            adminStage.setResizable(true);
            adminStage.show();
            
            System.out.println("✅ Application started successfully - Both Login and Admin Panel opened");
        } catch (Exception e) {
            System.err.println("❌ Error starting application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("🚀 Starting Airlines Management System...");
            launch(args);
        } catch (Exception e) {
            System.err.println("❌ Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
