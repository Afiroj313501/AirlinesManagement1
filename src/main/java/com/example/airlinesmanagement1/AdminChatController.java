package com.example.airlinesmanagement1;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.*;
import java.util.*;

public class AdminChatController {

    @FXML
    private Label adminChatLabel;

    @FXML
    private TextArea chatArea;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendButton;

    @FXML
    private ListView<String> userList;

    @FXML
    private TextField recipientField;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final String adminName = "Admin"; // Matches ADMIN_NAME in ChatServer
    private ObservableList<String> connectedUsers = FXCollections.observableArrayList();

    public void initialize() {
        // Connect to the chat server
        connectToServer();
        // Start a thread to listen for incoming messages
        new Thread(this::receiveMessages).start();
        
        // Set up user list
        userList.setItems(connectedUsers);
        userList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                recipientField.setText(newValue);
            }
        });
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 5000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Handle name submission protocol properly
            String serverResponse;
            while ((serverResponse = in.readLine()) != null) {
                if (serverResponse.equals("SUBMITNAME")) {
                    out.println(adminName);
                } else if (serverResponse.startsWith("NAMEACCEPTED")) {
                    Platform.runLater(() -> chatArea.appendText("Connected as " + adminName + "\n"));
                    break;
                } else if (serverResponse.equals("NAMETAKEN")) {
                    Platform.runLater(() -> chatArea.appendText("Admin name taken, trying with different name\n"));
                    String newAdminName = "Admin" + new java.util.Random().nextInt(100);
                    out.println(newAdminName);
                }
            }
        } catch (IOException e) {
            Platform.runLater(() -> chatArea.appendText("Error connecting to server: " + e.getMessage() + "\n"));
        }
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("MESSAGE")) {
                    String finalMessage = message.substring(8); // Remove "MESSAGE " prefix
                    Platform.runLater(() -> chatArea.appendText(finalMessage + "\n"));
                } else if (message.startsWith("PRIVATE")) {
                    String finalMessage = message.substring(8); // Remove "PRIVATE " prefix
                    Platform.runLater(() -> chatArea.appendText("[PRIVATE] " + finalMessage + "\n"));
                } else if (message.startsWith("USERLIST")) {
                    // Handle user list updates
                    String userListStr = message.substring(9); // Remove "USERLIST " prefix
                    String[] users = userListStr.split(",");
                    Platform.runLater(() -> {
                        connectedUsers.clear();
                        for (String user : users) {
                            if (!user.trim().isEmpty() && !user.equals(adminName)) {
                                connectedUsers.add(user.trim());
                            }
                        }
                    });
                } else if (message.startsWith("ERROR")) {
                    String errorMessage = message.substring(6); // Remove "ERROR " prefix
                    Platform.runLater(() -> chatArea.appendText("[ERROR] " + errorMessage + "\n"));
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
        String recipient = recipientField.getText().trim();
        
        if (!message.isEmpty()) {
            if (!recipient.isEmpty() && !recipient.equals(adminName)) {
                // Send private message to specific user
                out.println("/msg " + recipient + " " + message);
                Platform.runLater(() -> chatArea.appendText("[To " + recipient + "] " + message + "\n"));
            } else {
                // Send broadcast message
                out.println(message);
                Platform.runLater(() -> chatArea.appendText("[Broadcast] " + message + "\n"));
            }
            messageField.clear();
        }
    }

    @FXML
    private void sendBroadcast() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            out.println(message);
            Platform.runLater(() -> chatArea.appendText("[Broadcast] " + message + "\n"));
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