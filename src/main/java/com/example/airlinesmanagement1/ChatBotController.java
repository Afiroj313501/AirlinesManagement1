package com.example.airlinesmanagement1;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatBotController {

    @FXML
    private TextArea chatArea;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendButton;

    @FXML
    private ListView<String> userList;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox mainContainer;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private boolean isAdmin = false;
    private boolean isConnected = false;
    private boolean isInitialized = false;
    private ObservableList<String> connectedUsers = FXCollections.observableArrayList();
    private Scene previousScene;

    public void initialize() {
        // Set up user list
        userList.setItems(connectedUsers);
        userList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals("No users connected")) {
                messageField.setPromptText("Type @Admin " + newValue + " to send private message");
            }
        });

        // Set up enter key han
        messageField.setOnAction(e -> sendMessage());

        // Set up send button
        sendButton.setOnAction(e -> sendMessage());

        isInitialized = true;
        
        // If username is already set, connect immediately
        if (username != null && !username.isEmpty()) {
            connectToServer();
        }
    }

    public void setUsername(String username) {
        this.username = username;
        this.isAdmin = "Admin".equals(username);
        updateStatus("Connecting as " + username + "...");
        
        // If already initialized, connect to server
        if (isInitialized) {
            connectToServer();
        }
    }

    private void connectToServer() {
        if (username == null || username.isEmpty()) {
            updateStatus("❌ No username set");
            appendMessage("ERROR", "No username set. Please try again.");
            return;
        }
        
        try {
            socket = new Socket("localhost", 5000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Handle authentication
            String response = in.readLine();
            if ("AUTH_REQUEST".equals(response)) {
                out.println(username);
                response = in.readLine();
                
                if (response != null && response.startsWith("AUTH_SUCCESS")) {
                    isConnected = true;
                    updateStatus("✅ Connected as " + username);
                    appendMessage("SERVER", "Welcome to Airlines Support Chat! 🛩️");
                    
                    if (isAdmin) {
                        appendMessage("SERVER", "You are Admin. You can see all users and send messages.");
                        showUserList();
                    } else {
                        appendMessage("SERVER", "Admin will assist you shortly. Use @Admin to send private messages.");
                        hideUserList();
                    }
                    
                    // Start message receiving thread
                    new Thread(this::receiveMessages).start();
                } else if (response != null && response.startsWith("AUTH_FAILED")) {
                    String error = response.substring(response.indexOf(":") + 1);
                    updateStatus("❌ Authentication failed: " + error);
                    appendMessage("ERROR", "Authentication failed: " + error);
                    closeConnection();
                } else {
                    updateStatus("❌ Unexpected server response");
                    appendMessage("ERROR", "Unexpected server response: " + response);
                    closeConnection();
                }
            } else {
                updateStatus("❌ Server not responding properly");
                appendMessage("ERROR", "Server not responding properly. Make sure ChatBot server is running.");
                closeConnection();
            }
        } catch (IOException e) {
            updateStatus("❌ Connection failed: " + e.getMessage());
            appendMessage("ERROR", "Failed to connect to server: " + e.getMessage());
            appendMessage("ERROR", "Please make sure the ChatBot server is running on port 5000");
            closeConnection();
        }
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("MSG:")) {
                    String[] parts = message.split(":", 3);
                    if (parts.length >= 3) {
                        String sender = parts[1];
                        String content = parts[2];
                        Platform.runLater(() -> appendMessage(sender, content));
                    }
                } else if (message.startsWith("USERLIST:")) {
                    String userListStr = message.substring(9);
                    Platform.runLater(() -> updateUserList(userListStr));
                }
            }
        } catch (IOException e) {
            Platform.runLater(() -> {
                updateStatus("❌ Connection lost: " + e.getMessage());
                appendMessage("ERROR", "Connection lost: " + e.getMessage());
            });
        } finally {
            isConnected = false;
            closeConnection();
        }
    }

    @FXML
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && isConnected) {
            try {
                out.println(message);
                messageField.clear();
            } catch (Exception e) {
                appendMessage("ERROR", "Failed to send message: " + e.getMessage());
            }
        } else if (!isConnected) {
            appendMessage("ERROR", "Not connected to server. Please check if ChatBot server is running.");
        }
    }

    private void appendMessage(String sender, String message) {
        String timestamp = new java.text.SimpleDateFormat("HH:mm").format(new Date());
        String formattedMessage;
        
        if ("SERVER".equals(sender)) {
            formattedMessage = String.format("[%s] 🛩️ %s\n", timestamp, message);
        } else if ("ERROR".equals(sender)) {
            formattedMessage = String.format("[%s] ❌ %s\n", timestamp, message);
        } else if (sender.startsWith("PRIVATE:")) {
            String actualSender = sender.substring(8);
            formattedMessage = String.format("[%s] 🔒 %s: %s\n", timestamp, actualSender, message);
        } else {
            formattedMessage = String.format("[%s] %s: %s\n", timestamp, sender, message);
        }
        
        chatArea.appendText(formattedMessage);
        chatArea.setScrollTop(Double.MAX_VALUE);
    }

    private void updateStatus(String status) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText(status);
            }
        });
    }

    private void updateUserList(String userListStr) {
        connectedUsers.clear();
        if (userListStr != null && !userListStr.trim().isEmpty()) {
            String[] users = userListStr.split(",");
            for (String user : users) {
                if (!user.trim().isEmpty()) {
                    connectedUsers.add(user.trim());
                }
            }
        }
        
        if (connectedUsers.isEmpty()) {
            connectedUsers.add("No users connected");
        }
    }

    private void showUserList() {
        if (userList != null) {
            userList.setVisible(true);
            userList.setManaged(true);
        }
    }

    private void hideUserList() {
        if (userList != null) {
            userList.setVisible(false);
            userList.setManaged(false);
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

    @FXML
    private void goBack() {
        if (previousScene != null) {
            Stage stage = (Stage) sendButton.getScene().getWindow();
            stage.setScene(previousScene);
        }
    }

    public void setPreviousScene(Scene scene) {
        this.previousScene = scene;
    }

    public void shutdown() {
        closeConnection();
    }
} 