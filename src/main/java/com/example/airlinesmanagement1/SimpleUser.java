package com.example.airlinesmanagement1;

import java.io.*;
import java.net.*;

public class SimpleUser {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 5000;
        String clientName = "User1";

        try {
            System.out.println("Attempting to connect to " + serverAddress + ":" + port);
            Socket socket = new Socket(serverAddress, port);
            System.out.println("Connected to server");

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

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

            Thread listener = new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("Received: " + message);
                        if (message.startsWith("PRIVATE")) {
                            System.out.println("Private from Admin: " + message.substring(8));
                        } else if (message.startsWith("MESSAGE")) {
                            System.out.println("Broadcast: " + message.substring(8));
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error reading from server: " + e.getMessage());
                }
            });
            listener.start();

            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                System.out.println("Sending: " + userInput);
                if (userInput.startsWith("/msg admin ")) {
                    out.println(userInput); // Send as is for private message
                } else {
                    out.println(userInput); // Broadcast otherwise
                }
                if ("exit".equalsIgnoreCase(userInput)) {
                    break;
                }
            }

            listener.interrupt();
            socket.close();
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}