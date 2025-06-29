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
    private boolean isConnected = false;

    private Scene previousScene;

    public void initialize() {
        connectToServer();
        new Thread(this::receiveMessages).start();
    }

    private void connectToServer() {
        try {
            // Try to connect to the chat server on port 5000 (same as ChatServer)
            socket = new Socket("localhost", 5000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            Platform.runLater(() -> chatArea.appendText("Connecting to support chat...\n"));
            
        } catch (IOException e) {
            System.err.println("Support failed to connect to chat server: " + e.getMessage());
            Platform.runLater(() -> chatArea.appendText("Error connecting to server: " + e.getMessage() + "\n"));
            Platform.runLater(() -> chatArea.appendText("Please make sure the chat server is running.\n"));
        }
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                if (message.equals("SUBMITNAME")) {
                    out.println(clientName);
                    Platform.runLater(() -> chatArea.appendText("Submitting username: " + clientName + "\n"));
                } else if (message.startsWith("NAMEACCEPTED")) {
                    isConnected = true;
                    Platform.runLater(() -> chatArea.appendText("Connected as " + clientName + "\n"));
                    Platform.runLater(() -> chatArea.appendText("You can now chat with admin support.\n"));
                } else if (message.equals("NAMETAKEN")) {
                    // Generate a new name if current one is taken
                    clientName = "User" + (int)(Math.random() * 10000);
                    out.println(clientName);
                    Platform.runLater(() -> chatArea.appendText("Username taken, trying: " + clientName + "\n"));
                } else if (message.startsWith("MESSAGE")) {
                    String finalMessage = message.substring(8);
                    Platform.runLater(() -> chatArea.appendText(finalMessage + "\n"));
                } else if (message.startsWith("PRIVATE")) {
                    String finalMessage = message.substring(8);
                    Platform.runLater(() -> chatArea.appendText("[Admin] " + finalMessage + "\n"));
                } else if (message.startsWith("ERROR")) {
                    String errorMessage = message.substring(6);
                    Platform.runLater(() -> chatArea.appendText("[ERROR] " + errorMessage + "\n"));
                }
            }
        } catch (IOException e) {
            System.err.println("Support chat connection error: " + e.getMessage());
            Platform.runLater(() -> chatArea.appendText("Connection lost: " + e.getMessage() + "\n"));
        } catch (Exception e) {
            System.err.println("Support chat unexpected error: " + e.getMessage());
            Platform.runLater(() -> chatArea.appendText("Unexpected error: " + e.getMessage() + "\n"));
        } finally {
            isConnected = false;
            closeConnection();
        }
    }

    @FXML
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && out != null && isConnected) {
            try {
                out.println(message);
                Platform.runLater(() -> chatArea.appendText("[You] " + message + "\n"));
                messageField.clear();
            } catch (Exception e) {
                System.err.println("Error sending support message: " + e.getMessage());
                Platform.runLater(() -> chatArea.appendText("[ERROR] Failed to send message: " + e.getMessage() + "\n"));
            }
        } else if (!isConnected) {
            Platform.runLater(() -> chatArea.appendText("[ERROR] Not connected to server. Please wait...\n"));
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

    // Method to handle window closing
    @FXML
    private void handleWindowClose() {
        shutdown();
    }
}