package com.example.airlinesmanagement1;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 5000;
    private static HashMap<String, PrintWriter> clients = new HashMap<>(); // Store client name and writer
    private static final String ADMIN_NAME = "Admin"; // Special name for admin

    public static void main(String[] args) {
        System.out.println("Chat Server is running on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
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
                // Set up input and output streams
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Request and store client name
                while (true) {
                    out.println("SUBMITNAME");
                    clientName = in.readLine();
                    if (clientName == null) {
                        return;
                    }
                    synchronized (clients) {
                        if (!clientName.isEmpty() && !clients.containsKey(clientName)) {
                            clients.put(clientName, out);
                            break;
                        }
                    }
                }

                // Welcome the client
                out.println("NAMEACCEPTED " + clientName);
                broadcastMessage("Server", clientName + " has joined the chat");

                // Handle messages
                String message;
                while ((message = in.readLine()) != null) {
                    if (!message.isEmpty()) {
                        broadcastMessage(clientName, message);
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
                for (PrintWriter writer : clients.values()) {
                    writer.println("MESSAGE " + sender + ": " + message);
                }
            }
        }

        // Optional: Private messaging (uncomment to enable)
        /*
        private void sendPrivateMessage(String sender, String recipient, String message) {
            synchronized (clients) {
                PrintWriter recipientWriter = clients.get(recipient);
                if (recipientWriter != null) {
                    recipientWriter.println("MESSAGE " + sender + ": " + message);
                }
                // Also send to sender for confirmation
                if (!sender.equals(recipient)) {
                    PrintWriter senderWriter = clients.get(sender);
                    if (senderWriter != null) {
                        senderWriter.println("MESSAGE " + sender + ": " + message);
                    }
                }
            }
        }
        */
    }
}