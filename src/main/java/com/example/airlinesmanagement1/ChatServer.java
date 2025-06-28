package com.example.airlinesmanagement1;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 5000;
    private static HashMap<String, PrintWriter> clients = new HashMap<>(); // Store client name and writer
    private static final String ADMIN_NAME = "Admin"; // Special name for admin

    public static void main(String[] args) {
        System.out.println("Chat Server is starting on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat Server is running and accepting connections on port " + PORT);
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
                    } else if (!message.isEmpty()) {
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
    }
}