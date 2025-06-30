package com.example.airlinesmanagement1;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatBotServer {
    private static final int PORT = 5000;
    private static final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private static final String ADMIN_NAME = "Admin";
    private static ServerSocket serverSocket;
    private static volatile boolean running = true;

    public static void main(String[] args) {
        System.out.println("🤖 ChatBot Server Starting...");
        System.out.println("Port: " + PORT);
        System.out.println("Waiting for connections...\n");

        try {
            serverSocket = new ServerSocket(PORT);
            
            // Shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                running = false;
                System.out.println("\n🛑 Shutting down ChatBot Server...");
                try {
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        serverSocket.close();
                    }
                } catch (IOException e) {
                    System.err.println("Error closing server: " + e.getMessage());
                }
            }));

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("📱 New connection from: " + clientSocket.getInetAddress().getHostAddress());
                    
                    ClientHandler handler = new ClientHandler(clientSocket);
                    new Thread(handler).start();
                } catch (IOException e) {
                    if (running) {
                        System.err.println("❌ Error accepting connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Server error: " + e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String username;
        private boolean isAdmin = false;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Handle authentication
                if (!authenticate()) {
                    return;
                }

                // Send welcome message
                sendMessage("SERVER", "Welcome to Airlines Support Chat! 🛩️");
                if (isAdmin) {
                    sendMessage("SERVER", "You are connected as Admin. You can see all users and send messages.");
                } else {
                    sendMessage("SERVER", "You are connected as " + username + ". Admin will assist you shortly.");
                }

                // Broadcast user joined
                broadcastMessage("SERVER", username + " has joined the chat", username);

                // Update admin's user list
                updateAdminUserList();

                // Handle messages
                String message;
                while ((message = in.readLine()) != null) {
                    handleMessage(message);
                }

            } catch (IOException e) {
                System.err.println("❌ Client error for " + username + ": " + e.getMessage());
            } finally {
                disconnect();
            }
        }

        private boolean authenticate() throws IOException {
            // Send auth request
            out.println("AUTH_REQUEST");
            
            // Get username
            username = in.readLine();
            if (username == null || username.trim().isEmpty()) {
                return false;
            }

            // Check if admin
            isAdmin = username.equals(ADMIN_NAME);

            // Check if username is available (except for admin reconnection)
            if (clients.containsKey(username) && !isAdmin) {
                out.println("AUTH_FAILED:Username taken");
                return false;
            }

            // Remove old admin connection if reconnecting
            if (isAdmin && clients.containsKey(ADMIN_NAME)) {
                ClientHandler oldAdmin = clients.get(ADMIN_NAME);
                oldAdmin.disconnect();
                System.out.println("🔄 Admin reconnected, removed old connection");
            }

            // Register client
            clients.put(username, this);
            out.println("AUTH_SUCCESS:" + username);
            
            System.out.println("✅ " + username + " authenticated successfully");
            return true;
        }

        private void handleMessage(String message) {
            if (message == null || message.trim().isEmpty()) {
                return;
            }

            System.out.println("💬 " + username + ": " + message);

            // Handle commands
            if (message.startsWith("/")) {
                handleCommand(message);
                return;
            }

            // Handle private messages
            if (message.startsWith("@")) {
                handlePrivateMessage(message);
                return;
            }

            // Broadcast message
            broadcastMessage(username, message, username);
        }

        private void handleCommand(String command) {
            String[] parts = command.split(" ", 2);
            String cmd = parts[0].toLowerCase();
            String args = parts.length > 1 ? parts[1] : "";

            switch (cmd) {
                case "/help":
                    sendMessage("SERVER", "Commands: /help, /users, @username message");
                    break;
                case "/users":
                    if (isAdmin) {
                        sendMessage("SERVER", "Connected users: " + getConnectedUsers());
                    } else {
                        sendMessage("SERVER", "You can use @username to send private messages");
                    }
                    break;
                case "/admin":
                    if (!isAdmin) {
                        sendMessage("SERVER", "You can contact admin by typing: @Admin your message");
                    }
                    break;
                default:
                    sendMessage("SERVER", "Unknown command. Type /help for available commands.");
            }
        }

        private void handlePrivateMessage(String message) {
            String[] parts = message.split(" ", 2);
            if (parts.length < 2) {
                sendMessage("SERVER", "Usage: @username message");
                return;
            }

            String targetUser = parts[0].substring(1); // Remove @
            String privateMessage = parts[1];

            ClientHandler target = clients.get(targetUser);
            if (target != null) {
                target.sendMessage("PRIVATE:" + username, privateMessage);
                sendMessage("PRIVATE:" + targetUser, privateMessage);
                System.out.println("🔒 Private message from " + username + " to " + targetUser);
            } else {
                sendMessage("SERVER", "User " + targetUser + " is not online");
            }
        }

        private void broadcastMessage(String sender, String message, String excludeUser) {
            for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
                if (!entry.getKey().equals(excludeUser)) {
                    entry.getValue().sendMessage(sender, message);
                }
            }
        }

        private void sendMessage(String sender, String message) {
            if (out != null) {
                out.println("MSG:" + sender + ":" + message);
            }
        }

        private void updateAdminUserList() {
            ClientHandler admin = clients.get(ADMIN_NAME);
            if (admin != null) {
                admin.sendMessage("USERLIST", getConnectedUsers());
            }
        }

        private String getConnectedUsers() {
            return String.join(",", clients.keySet().stream()
                    .filter(name -> !name.equals(ADMIN_NAME))
                    .toArray(String[]::new));
        }

        private void disconnect() {
            if (username != null) {
                clients.remove(username);
                broadcastMessage("SERVER", username + " has left the chat", username);
                updateAdminUserList();
                System.out.println("👋 " + username + " disconnected");
            }

            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.err.println("Error closing client connection: " + e.getMessage());
            }
        }
    }
} 