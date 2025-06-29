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
    private boolean isConnected = false;

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
        
        // Set up enter key handling for message field
        messageField.setOnAction(e -> sendMessage());
        
        // Add connection status check
        Platform.runLater(() -> {
            // Check connection status after a short delay
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(() -> {
                        if (isConnected && out != null) {
                            chatArea.appendText("✅ Connection status: Connected and ready to send messages\n");
                        } else {
                            chatArea.appendText("❌ Connection status: Not connected or writer is null\n");
                            chatArea.appendText("isConnected: " + isConnected + ", out null: " + (out == null) + "\n");
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 5000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Platform.runLater(() -> chatArea.appendText("Connecting to chat server...\n"));

            // Handle name submission protocol properly
            String serverResponse;
            while ((serverResponse = in.readLine()) != null) {
                System.out.println("Admin received from server: " + serverResponse);
                if (serverResponse.equals("SUBMITNAME")) {
                    out.println(adminName);
                    System.out.println("Admin sending name: " + adminName);
                    Platform.runLater(() -> chatArea.appendText("Submitting admin name...\n"));
                } else if (serverResponse.startsWith("NAMEACCEPTED")) {
                    isConnected = true;
                    Platform.runLater(() -> chatArea.appendText("Connected as " + adminName + "\n"));
                    Platform.runLater(() -> chatArea.appendText("You can now chat with users and send private messages.\n"));
                    System.out.println("Admin connection successful");
                    break;
                } else if (serverResponse.equals("NAMETAKEN")) {
                    Platform.runLater(() -> chatArea.appendText("Admin name taken, trying with different name\n"));
                    String newAdminName = "Admin" + new java.util.Random().nextInt(100);
                    System.out.println("Admin trying new name: " + newAdminName);
                    out.println(newAdminName);
                } else {
                    System.out.println("Admin received unexpected response: " + serverResponse);
                }
            }
        } catch (IOException e) {
            System.err.println("Admin connection error: " + e.getMessage());
            Platform.runLater(() -> chatArea.appendText("Error connecting to server: " + e.getMessage() + "\n"));
            Platform.runLater(() -> chatArea.appendText("Please make sure the chat server is running.\n"));
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
                        if (connectedUsers.isEmpty()) {
                            connectedUsers.add("No users connected");
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
            isConnected = false;
            closeConnection();
        }
    }

    @FXML
    private void sendMessage() {
        String message = messageField.getText().trim();
        String recipient = recipientField.getText().trim();
        
        System.out.println("Admin attempting to send message: " + message);
        System.out.println("Recipient: " + recipient);
        System.out.println("Is connected: " + isConnected);
        System.out.println("Out writer null: " + (out == null));
        
        if (!message.isEmpty() && isConnected && out != null) {
            try {
                if (!recipient.isEmpty() && !recipient.equals(adminName) && !recipient.equals("No users connected")) {
                    // Send private message to specific user
                    String privateMessage = "/msg " + recipient + " " + message;
                    System.out.println("Sending private message: " + privateMessage);
                    out.println(privateMessage);
                    Platform.runLater(() -> chatArea.appendText("[To " + recipient + "] " + message + "\n"));
                } else {
                    // Send broadcast message
                    System.out.println("Sending broadcast message: " + message);
                    out.println(message);
                    Platform.runLater(() -> chatArea.appendText("[Broadcast] " + message + "\n"));
                }
                messageField.clear();
                System.out.println("Message sent successfully");
            } catch (Exception e) {
                System.err.println("Error sending message: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> chatArea.appendText("[ERROR] Failed to send message: " + e.getMessage() + "\n"));
            }
        } else {
            if (message.isEmpty()) {
                Platform.runLater(() -> chatArea.appendText("[ERROR] Message cannot be empty\n"));
            } else if (!isConnected) {
                Platform.runLater(() -> chatArea.appendText("[ERROR] Not connected to server. Please wait...\n"));
            } else if (out == null) {
                Platform.runLater(() -> chatArea.appendText("[ERROR] Connection writer is null. Please reconnect.\n"));
            }
        }
    }

    @FXML
    private void sendBroadcast() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && isConnected) {
            try {
                out.println(message);
                Platform.runLater(() -> chatArea.appendText("[Broadcast] " + message + "\n"));
                messageField.clear();
            } catch (Exception e) {
                Platform.runLater(() -> chatArea.appendText("[ERROR] Failed to send broadcast: " + e.getMessage() + "\n"));
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

    // Optional: Call this when the AdminChat window is closed
    public void shutdown() {
        closeConnection();
    }
}