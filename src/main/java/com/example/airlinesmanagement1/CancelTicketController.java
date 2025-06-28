package com.example.airlinesmanagement1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class CancelTicketController implements Initializable {

    @FXML
    private TableView<Ticket> ticketTable;

    @FXML
    private TableColumn<Ticket, String> flightIdCol;

    @FXML
    private TableColumn<Ticket, String> seatCol;

    @FXML
    private TableColumn<Ticket, LocalDate> dateCol;

    @FXML
    private TableColumn<Ticket, Double> priceCol;

    @FXML
    private TableColumn<Ticket, String> statusCol;

    @FXML
    private Button cancelButton;

    @FXML
    private Label statusLabel;

    private ObservableList<Ticket> ticketList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupColumns();
        loadTickets();
        setupTableSelection();
    }

    private void setupColumns() {
        flightIdCol.setCellValueFactory(new PropertyValueFactory<>("flightId"));
        seatCol.setCellValueFactory(new PropertyValueFactory<>("seat"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void setupTableSelection() {
        ticketTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                cancelButton.setDisable(false);
                statusLabel.setText("Selected: " + newSelection.getFlightId() + " - Seat " + newSelection.getSeat());
            } else {
                cancelButton.setDisable(true);
                statusLabel.setText("");
            }
        });
    }

    private void loadTickets() {
        String currentUser = Session.getInstance().getUsername();
        if (currentUser == null || currentUser.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No User", "No user is currently logged in.");
            return;
        }

        String query = "SELECT f.flight_number, t.seat, t.booking_date, t.price, " +
                "CASE WHEN t.booking_date < CURDATE() THEN 'Completed' " +
                "WHEN t.booking_date = CURDATE() THEN 'Today' " +
                "ELSE 'Upcoming' END as status " +
                "FROM tickets t JOIN flights f ON t.flight_id = f.flight_id " +
                "WHERE t.user_name = ? ORDER BY t.booking_date DESC";

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, currentUser);
            ResultSet rs = stmt.executeQuery();

            ticketList.clear();
            while (rs.next()) {
                String flightNumber = rs.getString("flight_number");
                String seat = rs.getString("seat");
                LocalDate date = rs.getDate("booking_date").toLocalDate();
                double price = rs.getDouble("price");
                String status = rs.getString("status");

                Ticket ticket = new Ticket(flightNumber, seat, date, price);
                ticket.setStatus(status);
                ticketList.add(ticket);
            }

            ticketTable.setItems(ticketList);
            cancelButton.setDisable(true);
            statusLabel.setText("");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading tickets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelTicket() {
        Ticket selectedTicket = ticketTable.getSelectionModel().getSelectedItem();
        if (selectedTicket == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a ticket to cancel.");
            return;
        }

        // Check if the flight is in the past
        if (selectedTicket.getBookingDate().isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, "Cannot Cancel", 
                "Cannot cancel a ticket for a flight that has already departed.");
            return;
        }

        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Cancellation");
        confirmDialog.setHeaderText("Cancel Ticket");
        confirmDialog.setContentText("Are you sure you want to cancel your ticket for flight " + 
            selectedTicket.getFlightId() + " (Seat " + selectedTicket.getSeat() + ")?\n" +
            "This action cannot be undone.");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                performCancellation(selectedTicket);
            }
        });
    }

    private void performCancellation(Ticket ticket) {
        String currentUser = Session.getInstance().getUsername();
        
        try (Connection conn = new DatabaseConnection().getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Delete the ticket
                String deleteQuery = "DELETE FROM tickets WHERE user_name = ? AND seat = ? AND booking_date = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                    deleteStmt.setString(1, currentUser);
                    deleteStmt.setString(2, ticket.getSeat());
                    deleteStmt.setDate(3, java.sql.Date.valueOf(ticket.getBookingDate()));
                    
                    int rowsAffected = deleteStmt.executeUpdate();
                    if (rowsAffected > 0) {
                        conn.commit();
                        showAlert(Alert.AlertType.INFORMATION, "Success", 
                            "Ticket cancelled successfully for flight " + ticket.getFlightId() + 
                            " (Seat " + ticket.getSeat() + ").");
                        loadTickets(); // Refresh the table
                    } else {
                        conn.rollback();
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to cancel ticket. Please try again.");
                    }
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error cancelling ticket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshTickets() {
        loadTickets();
        showAlert(Alert.AlertType.INFORMATION, "Refresh", "Ticket list refreshed.");
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 