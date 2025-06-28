package com.example.airlinesmanagement1;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int DEFAULT_PORT = 5000;
    private static final int MAX_PORT_ATTEMPTS = 10; // Try up to 10 different ports
    private static HashMap<String, PrintWriter> clients = new HashMap<>(); // Store client name and writer
    private static final String ADMIN_NAME = "Admin"; // Special name for admin

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        
        // Try to find an available port
        for (int attempt = 0; attempt < MAX_PORT_ATTEMPTS; attempt++) {
            int currentPort = port + attempt;
            if (isPortAvailable(currentPort)) {
                port = currentPort;
                break;
            } else {
                System.out.println("Port " + currentPort + " is already in use, trying next port...");
            }
        }
        
        System.out.println("Chat Server is starting on port " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Chat Server is running and accepting connections on port " + port);
            System.out.println("Server is ready to accept client connections.");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connection from: " + clientSocket.getInetAddress());
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static boolean isPortAvailable(int port) {
        try (ServerSocket testSocket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    out.println("SUBMITNAME");
                    clientName = in.readLine();
                    System.out.println("Received name attempt from " + socket.getInetAddress() + ": " + clientName);
                    if (clientName == null) {
                        return;
                    }
                    synchronized (clients) {
                        if (!clientName.isEmpty() && !clients.containsKey(clientName)) {
                            clients.put(clientName, out);
                            break;
                        } else {
                            out.println("NAMETAKEN");
                        }
                    }
                }

                out.println("NAMEACCEPTED " + clientName);
                broadcastMessage("Server", clientName + " has joined the chat");

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(clientName + " sent: " + message);
                    if (message.startsWith("/msg admin ")) {
                        sendPrivateMessage(clientName, ADMIN_NAME, message.substring(10)); // Remove "/msg admin "
                    } else if (message.startsWith("/msg ") && clientName.equals(ADMIN_NAME)) {
                        // Admin sending private message to user
                        String[] parts = message.split(" ", 3);
                        if (parts.length >= 3) {
                            String recipient = parts[1];
                            String privateMessage = parts[2];
                            sendPrivateMessage(ADMIN_NAME, recipient, privateMessage);
                        }
                    } else if (!message.isEmpty()) {
                        broadcastMessage(clientName, message);
                    }
                    
                    // Send updated user list to admin
                    if (clientName.equals(ADMIN_NAME)) {
                        sendUserListToAdmin();
                    }
                }
            } catch (IOException e) {
                System.err.println("Client handler error for " + clientName + ": " + e.getMessage());
            } finally {
                if (clientName != null) {
                    synchronized (clients) {
                        clients.remove(clientName);
                    }
                    broadcastMessage("Server", clientName + " has left the chat");
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
            }
        }

        private void broadcastMessage(String sender, String message) {
            synchronized (clients) {
                System.out.println("Broadcasting: " + sender + ": " + message + " to " + clients.size() + " clients");
                for (Map.Entry<String, PrintWriter> entry : clients.entrySet()) {
                    PrintWriter writer = entry.getValue();
                    writer.println("MESSAGE " + sender + ": " + message);
                    System.out.println("Sent to: " + entry.getKey());
                }
            }
        }

        private void sendPrivateMessage(String sender, String recipient, String message) {
            synchronized (clients) {
                PrintWriter recipientWriter = clients.get(recipient);
                if (recipientWriter != null) {
                    System.out.println("Sending private message from " + sender + " to " + recipient + ": " + message);
                    recipientWriter.println("PRIVATE " + sender + ": " + message);
                } else {
                    PrintWriter senderWriter = clients.get(sender);
                    if (senderWriter != null) {
                        senderWriter.println("ERROR Private message failed: Recipient " + recipient + " not found");
                    }
                }
            }
        }

        private void sendUserListToAdmin() {
            synchronized (clients) {
                PrintWriter adminWriter = clients.get(ADMIN_NAME);
                if (adminWriter != null) {
                    StringBuilder userList = new StringBuilder();
                    for (String client : clients.keySet()) {
                        if (!client.equals(ADMIN_NAME)) {
                            userList.append(client).append(",");
                        }
                    }
                    if (userList.length() > 0) {
                        userList.setLength(userList.length() - 1); // Remove the trailing comma
                    }
                    adminWriter.println("USERLIST " + userList.toString());
                }
            }
        }
    }
}