package com.example.airlinesmanagement1;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.*;
import java.net.*;
import java.time.LocalDate;

public class AdminPanelController {

    // City Management Fields
    @FXML
    private TextField cityNameField;
    @FXML
    private TableView<City> cityTable;
    @FXML
    private TableColumn<City, String> cityNameColumn;

    // Flight Management Fields
    @FXML
    private TextField flightNumberField;
    @FXML
    private TextField fromCityField;
    @FXML
    private TextField toCityField;
    @FXML
    private DatePicker flightDatePicker;
    @FXML
    private TextField departureTimeField;
    @FXML
    private TextField priceField;
    @FXML
    private TableView<Flight> flightTable;
    @FXML
    private TableColumn<Flight, String> flightNumberColumn;
    @FXML
    private TableColumn<Flight, String> fromCityColumn;
    @FXML
    private TableColumn<Flight, String> toCityColumn;
    @FXML
    private TableColumn<Flight, LocalDate> flightDateColumn;
    @FXML
    private TableColumn<Flight, String> departureTimeColumn;
    @FXML
    private TableColumn<Flight, Double> priceColumn;

    // Chat Fields
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

    // Data Models
    private ObservableList<City> cityList = FXCollections.observableArrayList();
    private ObservableList<Flight> flightList = FXCollections.observableArrayList();

    public void initialize() {
        // Initialize City Table
        cityNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityTable.setItems(cityList);

        // Initialize Flight Table
        flightNumberColumn.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        fromCityColumn.setCellValueFactory(new PropertyValueFactory<>("fromCity"));
        toCityColumn.setCellValueFactory(new PropertyValueFactory<>("toCity"));
        flightDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        departureTimeColumn.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        flightTable.setItems(flightList);

        // Connect to chat server
        connectToServer();
        // Start a thread to listen for incoming messages
        new Thread(this::receiveMessages).start();
    }

    // City Management Handlers
    @FXML
    private void handleAddCity(ActionEvent event) {
        String cityName = cityNameField.getText().trim();
        if (!cityName.isEmpty()) {
            cityList.add(new City(cityName));
            cityNameField.clear();
        } else {
            showAlert("Error", "City name cannot be empty.");
        }
    }

    // Flight Management Handlers
    @FXML
    private void handleAddFlight(ActionEvent event) {
        try {
            String flightNumber = flightNumberField.getText().trim();
            String fromCity = fromCityField.getText().trim();
            String toCity = toCityField.getText().trim();
            LocalDate date = flightDatePicker.getValue();
            String departureTime = departureTimeField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());

            if (flightNumber.isEmpty() || fromCity.isEmpty() || toCity.isEmpty() || date == null || departureTime.isEmpty()) {
                showAlert("Error", "All fields must be filled.");
                return;
            }

            flightList.add(new Flight(flightNumber, fromCity, toCity, date, departureTime, price));
            clearFlightFields();
        } catch (NumberFormatException e) {
            showAlert("Error", "Price must be a valid number.");
        }
    }

    // Chat Handlers
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

    // Utility Methods
    private void clearFlightFields() {
        flightNumberField.clear();
        fromCityField.clear();
        toCityField.clear();
        flightDatePicker.setValue(null);
        departureTimeField.clear();
        priceField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Data Model Classes
    public static class City {
        private String name;

        public City(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class Flight {
        private String flightNumber;
        private String fromCity;
        private String toCity;
        private LocalDate date;
        private String departureTime;
        private double price;

        public Flight(String flightNumber, String fromCity, String toCity, LocalDate date, String departureTime, double price) {
            this.flightNumber = flightNumber;
            this.fromCity = fromCity;
            this.toCity = toCity;
            this.date = date;
            this.departureTime = departureTime;
            this.price = price;
        }

        public String getFlightNumber() {
            return flightNumber;
        }

        public String getFromCity() {
            return fromCity;
        }

        public String getToCity() {
            return toCity;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getDepartureTime() {
            return departureTime;
        }

        public double getPrice() {
            return price;
        }
    }

    // Optional: Call this when the AdminPanel window is closed
    public void shutdown() {
        closeConnection();
    }
}