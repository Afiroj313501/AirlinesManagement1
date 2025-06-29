package com.example.airlinesmanagement1;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ToggleButton;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.*;
import java.util.*;
import java.io.IOException;

public class SeatSelectionController {

    @FXML
    private GridPane seatGrid;
    
    @FXML
    private Label fromCityLabel;
    
    @FXML
    private Label toCityLabel;
    
    @FXML
    private Label dateLabel;
    
    @FXML
    private Label priceLabel;
    
    @FXML
    private Label seatCountLabel;
    
    @FXML
    private Label totalPriceLabel;
    
    @FXML
    private Button paymentButton;

    private int flightId;
    private int seatPrice = 0;
    private int selectedSeatCount = 0;
    private String fromCity;
    private String toCity;
    private String flightDate;

    // Remove hardcoded default, rely on setCurrentUsername
    private String currentUsername;

    private final Map<String, ToggleButton> selectedSeatButtons = new HashMap<>();

    public void setFlightInfo(String from, String to, String date) {
        this.fromCity = from;
        this.toCity = to;
        this.flightDate = date;
        this.flightId = getFlightId(from, to, date);
        
        // Update flight info labels
        fromCityLabel.setText(from);
        toCityLabel.setText(to);
        dateLabel.setText(date);
        
        if (this.flightId > 0) {
            this.seatPrice = getSeatPrice(flightId);
            priceLabel.setText("BDT " + seatPrice);
            initializeSeatsForFlight(flightId);
            loadSeats();
        } else {
            showAlert(AlertType.ERROR, "Flight Not Found", "No flight found for selected route and date.");
        }
    }

    private int getFlightId(String from, String to, String date) {
        try (Connection conn = new DatabaseConnection().getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT flight_id FROM flights WHERE from_city=? AND to_city=? AND flight_date=?"
            );
            stmt.setString(1, from);
            stmt.setString(2, to);
            stmt.setString(3, date);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("flight_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int getSeatPrice(int flightId) {
        try (Connection conn = new DatabaseConnection().getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT price FROM flights WHERE flight_id=?");
            stmt.setInt(1, flightId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("price");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void initializeSeatsForFlight(int flightId) {
        String[] rows = {"A", "B", "C", "D", "E", "F", "G"};
        int cols = 4;

        try (Connection conn = new DatabaseConnection().getConnection()) {
            for (String row : rows) {
                for (int col = 1; col <= cols; col++) {
                    String seatNo = row + col;

                    PreparedStatement checkStmt = conn.prepareStatement(
                            "SELECT COUNT(*) FROM seats WHERE flight_id=? AND seat_number=?"
                    );
                    checkStmt.setInt(1, flightId);
                    checkStmt.setString(2, seatNo);
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next() && rs.getInt(1) == 0) {
                        PreparedStatement insertStmt = conn.prepareStatement(
                                "INSERT INTO seats (flight_id, seat_number, status) VALUES (?, ?, 'available')"
                        );
                        insertStmt.setInt(1, flightId);
                        insertStmt.setString(2, seatNo);
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSeats() {
        seatGrid.getChildren().clear();
        seatGrid.setHgap(8);
        seatGrid.setVgap(8);
        seatGrid.setPadding(new Insets(10));

        String[] rows = {"A", "B", "C", "D", "E", "F", "G"};
        int cols = 4;

        try (Connection conn = new DatabaseConnection().getConnection()) {
            for (int i = 0; i < rows.length; i++) {
                int visualRow = i;
                if (i >= 2) visualRow++;  // Adjust row for aisle space or visual layout

                for (int j = 1; j <= cols; j++) {
                    String seatNo = rows[i] + j;

                    PreparedStatement stmt = conn.prepareStatement(
                            "SELECT status FROM seats WHERE flight_id=? AND seat_number=?"
                    );
                    stmt.setInt(1, flightId);
                    stmt.setString(2, seatNo);
                    ResultSet rs = stmt.executeQuery();

                    ToggleButton seatBtn = new ToggleButton(seatNo);
                    seatBtn.setPrefWidth(50);
                    seatBtn.setPrefHeight(35);
                    
                    // Professional styling for seat buttons
                    seatBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-font-size: 11px;");

                    if (rs.next() && "booked".equalsIgnoreCase(rs.getString("status"))) {
                        seatBtn.setDisable(true);
                        seatBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-font-size: 11px;");
                    } else {
                        seatBtn.setOnAction(e -> handleSeatToggle(seatNo, seatBtn));
                        
                        // Add hover effect
                        seatBtn.setOnMouseEntered(e -> {
                            if (!seatBtn.isSelected()) {
                                seatBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-font-size: 11px;");
                            }
                        });
                        
                        seatBtn.setOnMouseExited(e -> {
                            if (!seatBtn.isSelected()) {
                                seatBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-font-size: 11px;");
                            }
                        });
                    }

                    int visualCol = j - 1;
                    if (j >= 3) visualCol++; // Adjust column for aisle gap

                    seatGrid.add(seatBtn, visualCol, visualRow);
                }
            }

            // Initialize labels
            seatCountLabel.setText("0");
            totalPriceLabel.setText("BDT 0");
            paymentButton.setDisable(true);
            paymentButton.setOnAction(e -> showPaymentOptions());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleSeatToggle(String seatNo, ToggleButton btn) {
        if (selectedSeatButtons.containsKey(seatNo)) {
            selectedSeatButtons.remove(seatNo);
            btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-font-size: 11px;");
        } else {
            selectedSeatButtons.put(seatNo, btn);
            btn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-font-size: 11px;");
        }

        selectedSeatCount = selectedSeatButtons.size();
        updateSeatAndPriceDisplay();
        paymentButton.setDisable(selectedSeatCount == 0);
    }

    private void updateSeatAndPriceDisplay() {
        seatCountLabel.setText(String.valueOf(selectedSeatCount));
        totalPriceLabel.setText("BDT " + (selectedSeatCount * seatPrice));
    }

    private void showPaymentOptions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/airlinesmanagement1/Payment.fxml"));
            Parent root = loader.load();

            PaymentController paymentController = loader.getController();
            
            // Pass all necessary data to the payment controller
            paymentController.setPaymentData(
                selectedSeatCount * seatPrice,
                getFlightFromCity(),
                getFlightToCity(),
                getFlightDate(),
                selectedSeatButtons.keySet(),
                currentUsername,
                flightId,
                seatPrice,
                selectedSeatButtons,
                this::onPaymentSuccess
            );

            Stage paymentStage = new Stage();
            paymentStage.setTitle("Secure Payment Gateway");
            paymentStage.setScene(new Scene(root));
            paymentStage.setResizable(false);
            paymentStage.show();

        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Payment Error", "Could not load payment interface: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void onPaymentSuccess() {
        // Clear selections and update UI after successful payment
        selectedSeatButtons.clear();
        selectedSeatCount = 0;
        updateSeatAndPriceDisplay();
        paymentButton.setDisable(true);
        loadSeats();
    }

    private String getFlightFromCity() {
        return fromCity;
    }

    private String getFlightToCity() {
        return toCity;
    }

    private String getFlightDate() {
        return flightDate;
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
        System.out.println("Set currentUsername to: " + currentUsername); // Debug log
    }
}