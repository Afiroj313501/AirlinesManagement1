package com.example.airlinesmanagement1;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;

public class SupportController {

    @FXML
    private Label supportLabel;

    @FXML
    private TextArea chatArea;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendButton;

    @FXML
    private Button backButton;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName = "User" + (int)(Math.random() * 1000);

    private Scene previousScene;

    public void initialize() {
        connectToServer();
        new Thread(this::receiveMessages).start();
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 5000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println(clientName);
        } catch (IOException e) {
            Platform.runLater(() -> chatArea.appendText("Error connecting to server: " + e.getMessage() + "\n"));
        }
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("SUBMITNAME")) {
                    out.println(clientName);
                } else if (message.startsWith("NAMEACCEPTED")) {
                    Platform.runLater(() -> chatArea.appendText("Connected as " + clientName + "\n"));
                } else if (message.startsWith("MESSAGE")) {
                    String finalMessage = message.substring(8);
                    Platform.runLater(() -> chatArea.appendText(finalMessage + "\n"));
                }
            }
        } catch (IOException e) {
            Platform.runLater(() -> chatArea.appendText("Connection lost: " + e.getMessage() + "\n"));
        } finally {
            closeConnection();
        }
    }

    @FXML
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            out.println(message);
            messageField.clear();
        }
    }

    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    public void shutdown() {
        closeConnection();
    }

    @FXML
    private void goBackToDashboard() {
        System.out.println("Attempting to go back to Dashboard. previousScene: " + (previousScene != null));
        if (previousScene != null) {
            Stage stage = (Stage) backButton.getScene().getWindow();
            if (stage != null) {
                stage.setScene(previousScene);
                stage.setTitle("Dashboard");
                System.out.println("Successfully navigated back to Dashboard.");
            } else {
                System.err.println("Error: Stage is null.");
            }
        } else {
            System.err.println("Error: Previous scene not set. Check navigation setup.");
        }
    }

    public void setPreviousScene(Scene scene) {
        this.previousScene = scene;
        System.out.println("Previous scene set to: " + (scene != null ? scene.getRoot().getClass().getSimpleName() : "null"));
    }
}