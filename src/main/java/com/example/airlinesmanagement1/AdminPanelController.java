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
import java.sql.*;
import java.time.LocalDate;

public class AdminPanelController {

    // City UI components
    @FXML private TextField cityNameField;
    @FXML private TableView<City> cityTable;
    @FXML private TableColumn<City, String> cityNameColumn;

    // Flight UI components
    @FXML private TextField flightNumberField;
    @FXML private TextField fromCityField;
    @FXML private TextField toCityField;
    @FXML private DatePicker flightDatePicker;
    @FXML private TextField departureTimeField;
    @FXML private TextField priceField;

    @FXML private TableView<Flight> flightTable;
    @FXML private TableColumn<Flight, String> flightNumberColumn;
    @FXML private TableColumn<Flight, String> fromCityColumn;
    @FXML private TableColumn<Flight, String> toCityColumn;
    @FXML private TableColumn<Flight, LocalDate> flightDateColumn;
    @FXML private TableColumn<Flight, String> departureTimeColumn;
    @FXML private TableColumn<Flight, Double> priceColumn;

    // Chatbox UI components
    @FXML private ListView<String> messageList;
    @FXML private TextField messageInput;

    private final String DB_URL = "jdbc:mysql://localhost:3306/airlines_management1?useSSL=false";
    private final String DB_USER = "root";
    private final String DB_PASSWORD = ""; // Set your password here

    // Observable lists for TableViews
    private ObservableList<City> cityList = FXCollections.observableArrayList();
    private ObservableList<Flight> flightList = FXCollections.observableArrayList();

    // Socket for messaging
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    @FXML
    public void initialize() {
        // Setup City Table column
        cityNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Setup Flight Table columns
        flightNumberColumn.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        fromCityColumn.setCellValueFactory(new PropertyValueFactory<>("fromCity"));
        toCityColumn.setCellValueFactory(new PropertyValueFactory<>("toCity"));
        flightDateColumn.setCellValueFactory(new PropertyValueFactory<>("flightDate"));
        departureTimeColumn.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Setup date validation for flight date picker
        setupDateValidation();

        // Load initial data
        loadCitiesFromDatabase();
        loadFlightsFromDatabase();

        // Bind observable lists to tables
        cityTable.setItems(cityList);
        flightTable.setItems(flightList);

        // Initialize chat connection
        initializeChat();
    }

    private void setupDateValidation() {
        // Set minimum date to today to prevent booking past dates
        flightDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date != null && date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #cccccc; -fx-text-fill: #666666;");
                }
            }
        });
        
        // Set the minimum date
        flightDatePicker.setValue(LocalDate.now());
    }

    private void initializeChat() {
        try {
            socket = new Socket("localhost", 5001);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Handle name submission loop
            new Thread(() -> {
                try {
                    receiveMessages();
                } catch (IOException e) {
                    System.err.println("Admin chat connection error: " + e.getMessage());
                    // Don't show alert for connection errors to avoid blocking the UI
                } catch (Exception e) {
                    System.err.println("Admin chat unexpected error: " + e.getMessage());
                }
            }).start();
        } catch (IOException e) {
            System.err.println("Admin failed to connect to chat server: " + e.getMessage());
            // Don't show alert to avoid blocking the UI
        }
    }

    private void receiveMessages() throws IOException {
        String message;
        while ((message = in.readLine()) != null) {
            System.out.println("Admin received: " + message); // Debug log
            if (message.equals("SUBMITNAME")) {
                System.out.println("Submitting name: Admin");
                out.println("Admin");
            } else if (message.startsWith("NAMEACCEPTED")) {
                Platform.runLater(() -> {
                    messageList.getItems().add("Connected as Admin");
                    System.out.println("UI updated with: Connected as Admin"); // Confirm UI update
                });
                break;
            } else if (message.equals("NAMETAKEN")) {
                System.out.println("Admin name taken, this shouldn't happen");
                return;
            }
        }

        // Now listen for chat messages
        while ((message = in.readLine()) != null) {
            System.out.println("Admin received message: " + message);
            if (message.startsWith("MESSAGE")) {
                String finalMessage = message.substring(8);
                Platform.runLater(() -> {
                    messageList.getItems().add(finalMessage);
                    messageList.scrollTo(messageList.getItems().size() - 1);
                });
            } else if (message.startsWith("PRIVATE")) {
                String finalMessage = message.substring(8);
                Platform.runLater(() -> {
                    messageList.getItems().add("[PRIVATE] " + finalMessage);
                    messageList.scrollTo(messageList.getItems().size() - 1);
                });
            }
        }
    }

    @FXML
    private void handleSendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty() && out != null) {
            try {
                out.println(message);
                messageInput.clear();
            } catch (Exception e) {
                System.err.println("Error sending message: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleAddCity(ActionEvent event) {
        String cityName = cityNameField.getText().trim();
        if (cityName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "City name cannot be empty.");
            return;
        }
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement("INSERT INTO cities (name) VALUES (?)")) {
            ps.setString(1, cityName);
            ps.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Success", "City added successfully!");
            cityNameField.clear();
            loadCitiesFromDatabase();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    @FXML
    public void handleAddFlight(ActionEvent event) {
        String flightNumber = flightNumberField.getText().trim();
        String fromCity = fromCityField.getText().trim();
        String toCity = toCityField.getText().trim();
        LocalDate flightDate = flightDatePicker.getValue();
        String departureTime = departureTimeField.getText().trim();
        String priceText = priceField.getText().trim();

        if (flightNumber.isEmpty() || fromCity.isEmpty() || toCity.isEmpty() || flightDate == null || departureTime.isEmpty() || priceText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill all flight details.");
            return;
        }

        // Check if the selected date is in the past
        if (flightDate.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, "Invalid Date", "Cannot add flights for past dates. Please select today or a future date.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Price must be a valid number.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO flights (flight_number, from_city, to_city, flight_date, departure_time, price) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, flightNumber);
            ps.setString(2, fromCity);
            ps.setString(3, toCity);
            ps.setDate(4, Date.valueOf(flightDate));
            ps.setString(5, departureTime);
            ps.setDouble(6, price);
            ps.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Flight added successfully!");
            clearFlightFields();
            loadFlightsFromDatabase();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    private void loadCitiesFromDatabase() {
        cityList.clear();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM cities")) {
            while (rs.next()) {
                cityList.add(new City(rs.getString("name")));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    private void loadFlightsFromDatabase() {
        flightList.clear();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT flight_number, from_city, to_city, flight_date, departure_time, price FROM flights")) {
            while (rs.next()) {
                flightList.add(new Flight(
                        rs.getString("flight_number"),
                        rs.getString("from_city"),
                        rs.getString("to_city"),
                        rs.getDate("flight_date").toLocalDate(),
                        rs.getString("departure_time"),
                        rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    private void clearFlightFields() {
        flightNumberField.clear();
        fromCityField.clear();
        toCityField.clear();
        flightDatePicker.setValue(null);
        departureTimeField.clear();
        priceField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // Cleanup method to close chat connection
    public void cleanup() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing admin chat connection: " + e.getMessage());
        }
    }
    
    // Method to handle window closing
    @FXML
    private void handleWindowClose() {
        cleanup();
    }
}