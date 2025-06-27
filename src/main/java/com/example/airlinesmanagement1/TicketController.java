package com.example.airlinesmanagement1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class TicketController implements Initializable {

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

    private ObservableList<Ticket> ticketList = FXCollections.observableArrayList();
    private Stage primaryStage;
    private Scene dashboardScene; // To store the dashboard scene for back navigation

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing TicketController...");
        try {
            if (ticketTable == null) {
                System.err.println("Error: ticketTable is null. Check FXML file.");
                showAlert("Initialization Error", "TableView not found in FXML.");
                return;
            }
            setupColumns();
            loadTickets();
            System.out.println("Initialization completed successfully.");
        } catch (Exception e) {
            System.err.println("Error during initialization: " + e.getMessage());
            e.printStackTrace();
            showAlert("Initialization Error", "Failed to initialize ticket table: " + e.getMessage());
        }
    }

    private void setupColumns() {
        if (flightIdCol == null || seatCol == null || dateCol == null || priceCol == null) {
            System.err.println("Error: One or more TableColumn fields are null. Check FXML file.");
            showAlert("Initialization Error", "Table columns not found in FXML.");
            return;
        }
        flightIdCol.setCellValueFactory(new PropertyValueFactory<>("flightId"));
        seatCol.setCellValueFactory(new PropertyValueFactory<>("seat"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
    }

    private void loadTickets() {
        String currentUser = Session.getInstance().getUsername();
        System.out.println("Current logged in user: " + currentUser);

        if (currentUser == null || currentUser.isEmpty()) {
            System.out.println("No user is logged in.");
            return;
        }

        String query = "SELECT f.flight_number, t.seat, t.booking_date, t.price " +
                "FROM tickets t JOIN flights f ON t.flight_id = f.flight_id WHERE t.user_name = ?";

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, currentUser);
            ResultSet rs = stmt.executeQuery();

            ticketList.clear();
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;
                String flightNumber = rs.getString("flight_number");
                String seat = rs.getString("seat");
                LocalDate date = rs.getDate("booking_date").toLocalDate();
                double price = rs.getDouble("price");

                System.out.println("Loaded ticket: " + flightNumber + ", " + seat + ", " + date + ", " + price);
                ticketList.add(new Ticket(flightNumber, seat, date, price));
            }

            if (!hasData) {
                System.out.println("No tickets found for user: " + currentUser);
            }

            ticketTable.setItems(ticketList);
        } catch (Exception e) {
            System.err.println("Error loading tickets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setPrimaryStage(Stage stage, Scene dashboardScene) {
        this.primaryStage = stage;
        this.dashboardScene = dashboardScene;
        System.out.println("PrimaryStage and dashboardScene set successfully.");
    }

    public void downloadPdfTicket(javafx.event.ActionEvent event) {
        if (primaryStage == null) {
            System.out.println("Error: Primary stage not set. Cannot show FileChooser.");
            showAlert("Error", "Internal error: Stage not initialized.");
            return;
        }

        Ticket selectedTicket = ticketTable.getSelectionModel().getSelectedItem();
        if (selectedTicket == null) {
            System.out.println("Error: No ticket selected.");
            showAlert("Error", "Please select a ticket from the table to download.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Download Specific Ticket");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                String content = "Ticket Details\n" +
                        "----------------\n" +
                        "Flight ID: " + selectedTicket.getFlightId() + "\n" +
                        "Seat Number: " + selectedTicket.getSeat() + "\n" +
                        "Booking Date: " + selectedTicket.getBookingDate() + "\n" +
                        "Price: $" + selectedTicket.getPrice() + "\n" +
                        "----------------";
                fos.write(content.getBytes());
                fos.flush();

                String absolutePath = file.getAbsolutePath();
                if (absolutePath != null) {
                    System.out.println("Ticket downloaded as text file: " + absolutePath);
                    showAlert("Success", "Specific ticket downloaded as text file: " + absolutePath +
                            "\nNote: PDF generation is not supported without additional libraries. " +
                            "Open this file with a text editor.");
                } else {
                    System.out.println("Warning: Could not determine absolute path for downloaded file.");
                    showAlert("Success", "Ticket downloaded, but absolute path could not be determined.");
                }
            } catch (IOException e) {
                System.out.println("Error downloading text file: " + e.getMessage());
                e.printStackTrace();
                showAlert("Error", "Failed to download ticket: " + e.getMessage() +
                        "\nCheck file permissions or destination directory. Error details: " + e.toString());
            }
        }
    }

    public void goBack(javafx.event.ActionEvent event) {
        if (primaryStage != null && dashboardScene != null) {
            primaryStage.setScene(dashboardScene); // Return to the dashboard scene
            System.out.println("Returned to Dashboard.");
        } else {
            System.out.println("Error: Primary stage or dashboard scene not set. Cannot navigate back.");
            showAlert("Error", "Unable to navigate back due to stage or scene initialization issue.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}