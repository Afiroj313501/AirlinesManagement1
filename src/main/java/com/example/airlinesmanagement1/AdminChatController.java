package com.example.airlinesmanagement1;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.*;

public class AdminChatController {

    @FXML
    private Label adminChatLabel;

    @FXML
    private TextArea chatArea;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendButton;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final String adminName = "Admin"; // Matches ADMIN_NAME in ChatServer

    public void initialize() {
        // Connect to the chat server
        connectToServer();
        // Start a thread to listen for incoming messages
        new Thread(this::receiveMessages).start();
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 5000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send admin name to server
            out.println(adminName);
        } catch (IOException e) {
            Platform.runLater(() -> chatArea.appendText("Error connecting to server: " + e.getMessage() + "\n"));
        }
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("SUBMITNAME")) {
                    out.println(adminName);
                } else if (message.startsWith("NAMEACCEPTED")) {
                    Platform.runLater(() -> chatArea.appendText("Connected as " + adminName + "\n"));
                } else if (message.startsWith("MESSAGE")) {
                    String finalMessage = message.substring(8); // Remove "MESSAGE " prefix
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

    // Optional: Call this when the AdminChat window is closed
    public void shutdown() {
        closeConnection();
    }
}