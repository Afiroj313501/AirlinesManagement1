package com.example.airlinesmanagement1;

import java.io.*;
import java.net.*;

public class SimpleUser {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 5000; // Changed to match ChatServer default port
        String clientName = "User1";

        // Try to connect to the server
        Socket socket = null;
        try {
            System.out.println("Attempting to connect to " + serverAddress + ":" + port);
            socket = new Socket(serverAddress, port);
            System.out.println("Connected to server successfully!");

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Handle name submission protocol
            String serverResponse;
            while ((serverResponse = in.readLine()) != null) {
                if (serverResponse.equals("SUBMITNAME")) {
                    System.out.println("Submitting name: " + clientName);
                    out.println(clientName);
                } else if (serverResponse.startsWith("NAMEACCEPTED")) {
                    System.out.println("Connected as " + clientName);
                    break;
                } else if (serverResponse.equals("NAMETAKEN")) {
                    System.out.println("Name taken, trying again with a new name");
                    clientName = "User" + new java.util.Random().nextInt(100);
                    out.println(clientName);
                }
            }

            // Start listener thread for incoming messages
            Thread listener = new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        if (message.startsWith("PRIVATE")) {
                            System.out.println("[PRIVATE] " + message.substring(8));
                        } else if (message.startsWith("MESSAGE")) {
                            System.out.println("[BROADCAST] " + message.substring(8));
                        } else if (message.startsWith("ERROR")) {
                            System.out.println("[ERROR] " + message.substring(6));
                        } else {
                            System.out.println("[SERVER] " + message);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error reading from server: " + e.getMessage());
                }
            });
            listener.start();

            // Handle user input
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("\n=== Chat System Started ===");
            System.out.println("Commands:");
            System.out.println("- Type any message to broadcast to all users");
            System.out.println("- Type '/msg admin <message>' to send private message to admin");
            System.out.println("- Type 'exit' to disconnect");
            System.out.println("==========================\n");
            
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                if ("exit".equalsIgnoreCase(userInput)) {
                    System.out.println("Disconnecting from server...");
                    break;
                }
                
                if (!userInput.trim().isEmpty()) {
                    out.println(userInput);
                }
            }

            listener.interrupt();
            socket.close();
            System.out.println("Disconnected from server");
        } catch (ConnectException e) {
            System.err.println("Error: Could not connect to server. Make sure ChatServer is running on port " + port);
            System.err.println("To start the server, run: java -cp . com.example.airlinesmanagement1.ChatServer");
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
            }
        }
    }
}