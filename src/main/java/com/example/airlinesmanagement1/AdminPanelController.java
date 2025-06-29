package com.example.airlinesmanagement1;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

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
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextField capacityField;

    @FXML private TableView<Flight> flightTable;
    @FXML private TableColumn<Flight, String> flightNumberColumn;
    @FXML private TableColumn<Flight, String> fromCityColumn;
    @FXML private TableColumn<Flight, String> toCityColumn;
    @FXML private TableColumn<Flight, LocalDate> flightDateColumn;
    @FXML private TableColumn<Flight, String> departureTimeColumn;
    @FXML private TableColumn<Flight, Double> priceColumn;
    @FXML private TableColumn<Flight, String> statusColumn;
    @FXML private TableColumn<Flight, Integer> capacityColumn;
    @FXML private TableColumn<Flight, Integer> availableSeatsColumn;
    @FXML private TableColumn<Flight, String> loadFactorColumn;

    // Search and Filter components
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ComboBox<String> cityFilterComboBox;
    @FXML private DatePicker dateFilterPicker;

    // Action buttons
    @FXML private Button editFlightButton;
    @FXML private Button deleteFlightButton;
    @FXML private Button updateStatusButton;

    // Chatbox UI components
    @FXML private ListView<String> messageList;
    @FXML private TextField messageInput;

    private final String DB_URL = "jdbc:mysql://localhost:3306/airlines_management1?useSSL=false";
    private final String DB_USER = "root";
    private final String DB_PASSWORD = ""; // Set your password here

    // Observable lists for TableViews
    private ObservableList<City> cityList = FXCollections.observableArrayList();
    private ObservableList<Flight> flightList = FXCollections.observableArrayList();
    private FilteredList<Flight> filteredFlightList;

    // Socket for messaging
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    // Currently selected flight for editing
    private Flight selectedFlight;

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
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        availableSeatsColumn.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));
        loadFactorColumn.setCellValueFactory(new PropertyValueFactory<>("loadFactor"));

        // Setup status combo box
        statusComboBox.setItems(FXCollections.observableArrayList("on-time", "delayed", "cancelled"));
        statusComboBox.setValue("on-time");

        // Setup filter combo boxes
        statusFilterComboBox.setItems(FXCollections.observableArrayList("All", "on-time", "delayed", "cancelled"));
        statusFilterComboBox.setValue("All");

        // Setup date validation for flight date picker
        setupDateValidation();

        // Load initial data
        loadCitiesFromDatabase();
        loadFlightsFromDatabase();

        // Bind observable lists to tables
        cityTable.setItems(cityList);

        // Setup filtered list
        filteredFlightList = new FilteredList<>(flightList, p -> true);
        flightTable.setItems(filteredFlightList);

        // Setup search functionality
        setupSearchAndFiltering();

        // Setup table selection
        setupTableSelection();

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

    private void setupSearchAndFiltering() {
        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredFlightList.setPredicate(flight -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return flight.getFlightNumber().toLowerCase().contains(lowerCaseFilter) ||
                       flight.getFromCity().toLowerCase().contains(lowerCaseFilter) ||
                       flight.getToCity().toLowerCase().contains(lowerCaseFilter);
            });
        });

        // Status filter
        statusFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            filteredFlightList.setPredicate(flight -> {
                if (newValue == null || newValue.equals("All")) {
                    return true;
                }
                return flight.getStatus().equals(newValue);
            });
        });

        // Date filter
        dateFilterPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            filteredFlightList.setPredicate(flight -> {
                if (newValue == null) {
                    return true;
                }
                return flight.getFlightDate().equals(newValue);
            });
        });
    }

    private void setupTableSelection() {
        flightTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedFlight = newSelection;
            boolean hasSelection = newSelection != null;
            
            if (editFlightButton != null) editFlightButton.setDisable(!hasSelection);
            if (deleteFlightButton != null) deleteFlightButton.setDisable(!hasSelection);
            if (updateStatusButton != null) updateStatusButton.setDisable(!hasSelection);
        });
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
        String status = statusComboBox.getValue() != null ? statusComboBox.getValue() : "on-time";
        String capacityText = capacityField.getText().trim();

        if (flightNumber.isEmpty() || fromCity.isEmpty() || toCity.isEmpty() || flightDate == null || departureTime.isEmpty() || priceText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill all required flight details.");
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

        int capacity = 28; // default capacity
        if (!capacityText.isEmpty()) {
            try {
                capacity = Integer.parseInt(capacityText);
                if (capacity <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Capacity must be greater than 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Capacity must be a valid number.");
                return;
            }
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Check if new columns exist
            boolean hasNewColumns = false;
            try (Statement checkStmt = conn.createStatement()) {
                checkStmt.executeQuery("SELECT status, capacity, booked_seats FROM flights LIMIT 1");
                hasNewColumns = true;
            } catch (SQLException e) {
                hasNewColumns = false;
            }

            PreparedStatement ps;
            if (hasNewColumns) {
                ps = conn.prepareStatement(
                        "INSERT INTO flights (flight_number, from_city, to_city, flight_date, departure_time, price, status, capacity, booked_seats) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                ps.setString(1, flightNumber);
                ps.setString(2, fromCity);
                ps.setString(3, toCity);
                ps.setDate(4, Date.valueOf(flightDate));
                ps.setString(5, departureTime);
                ps.setDouble(6, price);
                ps.setString(7, status);
                ps.setInt(8, capacity);
                ps.setInt(9, 0); // booked_seats starts at 0
            } else {
                ps = conn.prepareStatement(
                        "INSERT INTO flights (flight_number, from_city, to_city, flight_date, departure_time, price) VALUES (?, ?, ?, ?, ?, ?)");
                ps.setString(1, flightNumber);
                ps.setString(2, fromCity);
                ps.setString(3, toCity);
                ps.setDate(4, Date.valueOf(flightDate));
                ps.setString(5, departureTime);
                ps.setDouble(6, price);
            }
            
            ps.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Flight added successfully!");
            clearFlightFields();
            loadFlightsFromDatabase();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    @FXML
    public void handleEditFlight(ActionEvent event) {
        Flight selectedFlight = flightTable.getSelectionModel().getSelectedItem();
        if (selectedFlight == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a flight to edit.");
            return;
        }

        // Create a dialog for editing
        Dialog<Flight> dialog = new Dialog<>();
        dialog.setTitle("Edit Flight");
        dialog.setHeaderText("Edit Flight Details");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the custom content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField flightNumberEdit = new TextField(selectedFlight.getFlightNumber());
        TextField fromCityEdit = new TextField(selectedFlight.getFromCity());
        TextField toCityEdit = new TextField(selectedFlight.getToCity());
        DatePicker dateEdit = new DatePicker(selectedFlight.getFlightDate());
        TextField timeEdit = new TextField(selectedFlight.getDepartureTime());
        TextField priceEdit = new TextField(String.valueOf(selectedFlight.getPrice()));
        ComboBox<String> statusEdit = new ComboBox<>(FXCollections.observableArrayList("on-time", "delayed", "cancelled"));
        statusEdit.setValue(selectedFlight.getStatus());
        TextField capacityEdit = new TextField(String.valueOf(selectedFlight.getCapacity()));

        grid.add(new Label("Flight Number:"), 0, 0);
        grid.add(flightNumberEdit, 1, 0);
        grid.add(new Label("From:"), 0, 1);
        grid.add(fromCityEdit, 1, 1);
        grid.add(new Label("To:"), 0, 2);
        grid.add(toCityEdit, 1, 2);
        grid.add(new Label("Date:"), 0, 3);
        grid.add(dateEdit, 1, 3);
        grid.add(new Label("Time:"), 0, 4);
        grid.add(timeEdit, 1, 4);
        grid.add(new Label("Price:"), 0, 5);
        grid.add(priceEdit, 1, 5);
        grid.add(new Label("Status:"), 0, 6);
        grid.add(statusEdit, 1, 6);
        grid.add(new Label("Capacity:"), 0, 7);
        grid.add(capacityEdit, 1, 7);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the first field
        Platform.runLater(flightNumberEdit::requestFocus);

        // Convert the result to a Flight object when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    double price = Double.parseDouble(priceEdit.getText());
                    int capacity = Integer.parseInt(capacityEdit.getText());
                    
                    if (capacity <= 0) {
                        showAlert(Alert.AlertType.ERROR, "Validation Error", "Capacity must be greater than 0.");
                        return null;
                    }
                    
                    if (price <= 0) {
                        showAlert(Alert.AlertType.ERROR, "Validation Error", "Price must be greater than 0.");
                        return null;
                    }
                    
                    Flight editedFlight = new Flight(
                        selectedFlight.getFlightId(),
                        flightNumberEdit.getText(),
                        fromCityEdit.getText(),
                        toCityEdit.getText(),
                        dateEdit.getValue(),
                        timeEdit.getText(),
                        price,
                        statusEdit.getValue(),
                        capacity,
                        selectedFlight.getBookedSeats()
                    );
                    
                    // Update the database
                    updateFlightInDatabase(editedFlight);
                    return editedFlight;
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter valid numbers for price and capacity.");
                    return null;
                }
            }
            return null;
        });

        Optional<Flight> result = dialog.showAndWait();
        if (result.isPresent()) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Flight updated successfully!");
            // Refresh the table to show updated data
            loadFlightsFromDatabase();
        }
    }

    @FXML
    public void handleDeleteFlight(ActionEvent event) {
        if (selectedFlight == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a flight to delete.");
            return;
        }

        // Check if flight has bookings
        if (selectedFlight.getBookedSeats() > 0) {
            showAlert(Alert.AlertType.WARNING, "Cannot Delete", "Cannot delete flight with existing bookings. Please cancel all bookings first.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Flight");
        alert.setContentText("Are you sure you want to delete flight " + selectedFlight.getFlightNumber() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM flights WHERE flight_id = ?")) {
                ps.setInt(1, selectedFlight.getFlightId());
                int affectedRows = ps.executeUpdate();
                if (affectedRows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Flight deleted successfully!");
                    loadFlightsFromDatabase();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete flight.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            }
        }
    }

    @FXML
    public void handleUpdateStatus(ActionEvent event) {
        Flight selectedFlight = flightTable.getSelectionModel().getSelectedItem();
        if (selectedFlight == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a flight to update status.");
            return;
        }

        // Create a dialog for status update
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Update Flight Status");
        dialog.setHeaderText("Update Status for Flight " + selectedFlight.getFlightNumber());

        // Set the button types
        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        // Create the custom content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList("on-time", "delayed", "cancelled"));
        statusCombo.setValue(selectedFlight.getStatus());

        grid.add(new Label("Current Status: " + selectedFlight.getStatus()), 0, 0);
        grid.add(new Label("New Status:"), 0, 1);
        grid.add(statusCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the combo box
        Platform.runLater(statusCombo::requestFocus);

        // Convert the result when the update button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                return statusCombo.getValue();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newStatus = result.get();
            if (!newStatus.equals(selectedFlight.getStatus())) {
                updateFlightStatus(selectedFlight.getFlightId(), newStatus);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Flight status updated to: " + newStatus);
                // Refresh the table to show updated data
                loadFlightsFromDatabase();
            } else {
                showAlert(Alert.AlertType.INFORMATION, "No Change", "Status is already set to: " + newStatus);
            }
        }
    }

    @FXML
    public void handleClearFilters(ActionEvent event) {
        searchField.clear();
        statusFilterComboBox.setValue("All");
        dateFilterPicker.setValue(null);
        cityFilterComboBox.setValue("All");
    }

    @FXML
    public void handleRefreshData(ActionEvent event) {
        try {
            loadCitiesFromDatabase();
            loadFlightsFromDatabase();
            showAlert(Alert.AlertType.INFORMATION, "Refresh Complete", "Data has been refreshed from the database.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Refresh Error", "Failed to refresh data: " + e.getMessage());
        }
    }

    private void updateFlightInDatabase(Flight flight) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Check if new columns exist
            boolean hasNewColumns = false;
            try (Statement checkStmt = conn.createStatement()) {
                checkStmt.executeQuery("SELECT status, capacity, booked_seats FROM flights LIMIT 1");
                hasNewColumns = true;
            } catch (SQLException e) {
                hasNewColumns = false;
            }

            PreparedStatement ps;
            if (hasNewColumns) {
                ps = conn.prepareStatement(
                        "UPDATE flights SET flight_number=?, from_city=?, to_city=?, flight_date=?, departure_time=?, price=?, status=?, capacity=? WHERE flight_id=?");
                ps.setString(1, flight.getFlightNumber());
                ps.setString(2, flight.getFromCity());
                ps.setString(3, flight.getToCity());
                ps.setDate(4, Date.valueOf(flight.getFlightDate()));
                ps.setString(5, flight.getDepartureTime());
                ps.setDouble(6, flight.getPrice());
                ps.setString(7, flight.getStatus());
                ps.setInt(8, flight.getCapacity());
                ps.setInt(9, flight.getFlightId());
            } else {
                ps = conn.prepareStatement(
                        "UPDATE flights SET flight_number=?, from_city=?, to_city=?, flight_date=?, departure_time=?, price=? WHERE flight_id=?");
                ps.setString(1, flight.getFlightNumber());
                ps.setString(2, flight.getFromCity());
                ps.setString(3, flight.getToCity());
                ps.setDate(4, Date.valueOf(flight.getFlightDate()));
                ps.setString(5, flight.getDepartureTime());
                ps.setDouble(6, flight.getPrice());
                ps.setInt(7, flight.getFlightId());
            }
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                showAlert(Alert.AlertType.ERROR, "Update Failed", "No flight was updated. Please try again.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update flight: " + e.getMessage());
        }
    }

    private void updateFlightStatus(int flightId, String newStatus) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Check if status column exists
            boolean hasStatusColumn = false;
            try (Statement checkStmt = conn.createStatement()) {
                checkStmt.executeQuery("SELECT status FROM flights LIMIT 1");
                hasStatusColumn = true;
            } catch (SQLException e) {
                hasStatusColumn = false;
            }

            if (hasStatusColumn) {
                PreparedStatement ps = conn.prepareStatement("UPDATE flights SET status = ? WHERE flight_id = ?");
                ps.setString(1, newStatus);
                ps.setInt(2, flightId);
                
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected == 0) {
                    showAlert(Alert.AlertType.ERROR, "Update Failed", "No flight was updated. Please try again.");
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Feature Not Available", "Status column not found in database. Please run the database update script.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update flight status: " + e.getMessage());
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
             Statement stmt = conn.createStatement()) {
            
            // First, check if the new columns exist
            boolean hasNewColumns = false;
            try {
                ResultSet rs = stmt.executeQuery("SELECT status, capacity, booked_seats FROM flights LIMIT 1");
                hasNewColumns = true;
            } catch (SQLException e) {
                // New columns don't exist, use old query
                hasNewColumns = false;
            }
            
            String query;
            if (hasNewColumns) {
                query = "SELECT flight_id, flight_number, from_city, to_city, flight_date, departure_time, price, status, capacity, booked_seats FROM flights ORDER BY flight_date, departure_time";
            } else {
                query = "SELECT flight_id, flight_number, from_city, to_city, flight_date, departure_time, price FROM flights ORDER BY flight_date, departure_time";
            }
            
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                if (hasNewColumns) {
                    flightList.add(new Flight(
                            rs.getInt("flight_id"),
                            rs.getString("flight_number"),
                            rs.getString("from_city"),
                            rs.getString("to_city"),
                            rs.getDate("flight_date").toLocalDate(),
                            rs.getString("departure_time"),
                            rs.getDouble("price"),
                            rs.getString("status"),
                            rs.getInt("capacity"),
                            rs.getInt("booked_seats")
                    ));
                } else {
                    // Use old constructor with default values for new fields
                    Flight flight = new Flight(
                            rs.getString("flight_number"),
                            rs.getString("from_city"),
                            rs.getString("to_city"),
                            rs.getDate("flight_date").toLocalDate(),
                            rs.getString("departure_time"),
                            rs.getDouble("price")
                    );
                    // Set the flight ID manually if needed
                    try {
                        java.lang.reflect.Field idField = Flight.class.getDeclaredField("flightId");
                        idField.setAccessible(true);
                        idField.set(flight, rs.getInt("flight_id"));
                    } catch (Exception e) {
                        // Ignore if we can't set the ID
                    }
                    flightList.add(flight);
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    private void clearFlightFields() {
        flightNumberField.clear();
        fromCityField.clear();
        toCityField.clear();
        flightDatePicker.setValue(LocalDate.now());
        departureTimeField.clear();
        priceField.clear();
        capacityField.clear();
        statusComboBox.setValue("on-time");
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