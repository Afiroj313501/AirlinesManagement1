package com.example.airlinesmanagement1;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ToggleButton;

import java.sql.*;
import java.util.*;

public class SeatSelectionController {

    @FXML
    private GridPane seatGrid;

    private int flightId;
    private int seatPrice = 0;
    private int selectedSeatCount = 0;

    // Remove hardcoded default, rely on setCurrentUsername
    private String currentUsername;

    private final Label seatCountLabel = new Label("Selected Seats: 0");
    private final Label totalPriceLabel = new Label("Total: 0 BDT");
    private final Button paymentButton = new Button("Proceed to Payment");

    private final Map<String, ToggleButton> selectedSeatButtons = new HashMap<>();

    public void setFlightInfo(String from, String to, String date) {
        this.flightId = getFlightId(from, to, date);
        if (this.flightId > 0) {
            this.seatPrice = getSeatPrice(flightId);
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
        seatGrid.setHgap(15);
        seatGrid.setVgap(15);
        seatGrid.setPadding(new Insets(20));

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
                    seatBtn.setPrefWidth(60);
                    seatBtn.setPrefHeight(40);

                    if (rs.next() && "booked".equalsIgnoreCase(rs.getString("status"))) {
                        seatBtn.setDisable(true);
                        seatBtn.setStyle("-fx-background-color: gray;");
                    } else {
                        seatBtn.setOnAction(e -> handleSeatToggle(seatNo, seatBtn));
                    }

                    int visualCol = j - 1;
                    if (j >= 3) visualCol++; // Adjust column for aisle gap

                    seatGrid.add(seatBtn, visualCol, visualRow);
                }
            }

            // Add UI controls below the grid
            seatGrid.add(seatCountLabel, 0, rows.length + 2, 2, 1);
            seatGrid.add(totalPriceLabel, 2, rows.length + 2, 2, 1);
            seatGrid.add(paymentButton, 0, rows.length + 3, 4, 1);

            paymentButton.setDisable(true);
            paymentButton.setOnAction(e -> showPaymentOptions());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleSeatToggle(String seatNo, ToggleButton btn) {
        if (selectedSeatButtons.containsKey(seatNo)) {
            selectedSeatButtons.remove(seatNo);
            btn.setStyle("");
        } else {
            selectedSeatButtons.put(seatNo, btn);
            btn.setStyle("-fx-background-color: lightgreen;");
        }

        selectedSeatCount = selectedSeatButtons.size();
        updateSeatAndPriceDisplay();
        paymentButton.setDisable(selectedSeatCount == 0);
    }

    private void updateSeatAndPriceDisplay() {
        seatCountLabel.setText("Selected Seats: " + selectedSeatCount);
        totalPriceLabel.setText("Total: " + (selectedSeatCount * seatPrice) + " BDT");
    }

    private void showPaymentOptions() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("bKash",
                "bKash", "Rocket", "Nagad", "Eastern Bank", "Brac Bank");

        dialog.setTitle("Select Payment Method");
        dialog.setHeaderText("Total Amount: " + (selectedSeatCount * seatPrice) + " BDT");
        dialog.setContentText("Choose your payment option:");

        dialog.showAndWait().ifPresent(method -> {
            showAlert(AlertType.INFORMATION, "Payment Successful", "Paid using " + method);
            confirmSeatBookings();
        });
    }

    private void confirmSeatBookings() {
        if (currentUsername == null) {
            System.out.println("Warning: currentUsername is null, using 'baba_yaga' as fallback.");
            currentUsername = "baba_yaga"; // Temporary fallback
        }
        try (Connection conn = new DatabaseConnection().getConnection()) {
            conn.setAutoCommit(false);

            for (String seatNo : selectedSeatButtons.keySet()) {
                System.out.println("Attempting to book seat: " + seatNo + " for user: " + currentUsername + ", flightId: " + flightId);
                PreparedStatement seatStmt = conn.prepareStatement(
                        "UPDATE seats SET status='booked', user_name=?, booking_date=CURDATE() " +
                                "WHERE flight_id=? AND seat_number=? AND status='available'"
                );
                seatStmt.setString(1, currentUsername);
                seatStmt.setInt(2, flightId);
                seatStmt.setString(3, seatNo);
                int seatRows = seatStmt.executeUpdate();
                if (seatRows == 0) {
                    conn.rollback();
                    showAlert(AlertType.ERROR, "Booking Failed", "Seat " + seatNo + " is already booked.");
                    return;
                }

                System.out.println("Inserting ticket for seat: " + seatNo);
                PreparedStatement ticketStmt = conn.prepareStatement(
                        "INSERT INTO tickets (user_name, flight_id, seat, booking_date, price) " +
                                "VALUES (?, ?, ?, CURDATE(), ?)"
                );
                ticketStmt.setString(1, currentUsername);
                ticketStmt.setInt(2, flightId);
                ticketStmt.setString(3, seatNo);
                ticketStmt.setDouble(4, seatPrice);
                ticketStmt.executeUpdate();
            }

            conn.commit();
            selectedSeatButtons.clear();
            selectedSeatCount = 0;
            updateSeatAndPriceDisplay();
            paymentButton.setDisable(true);
            loadSeats();
            showAlert(AlertType.INFORMATION, "Success", "Booking confirmed! Tickets are available in your account.");
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            showAlert(AlertType.ERROR, "Booking Failed", "An error occurred while booking seats: " + e.getMessage());
        }
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