package com.example.airlinesmanagement1;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FlightBookController {

    @FXML
    private TextField fromField;

    @FXML
    private TextField toField;

    @FXML
    private DatePicker flightDatePicker;

    private final ContextMenu fromSuggestions = new ContextMenu();
    private final ContextMenu toSuggestions = new ContextMenu();

    @FXML
    public void initialize() {
        fromField.setOnKeyReleased(e -> showSuggestions(fromField, fromSuggestions));
        toField.setOnKeyReleased(e -> showSuggestions(toField, toSuggestions));
    }

    private Connection getConnection() throws SQLException {
        DatabaseConnection db = new DatabaseConnection();
        return db.getConnection();
    }

    private void showSuggestions(TextField textField, ContextMenu menu) {
        String input = textField.getText().trim().toLowerCase();
        if (input.length() < 2) {
            Platform.runLater(menu::hide);
            return;
        }

        new Thread(() -> {
            List<String> cityList = new ArrayList<>();

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT name FROM cities WHERE LOWER(name) LIKE ? LIMIT 5")) {
                stmt.setString(1, input + "%");
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    cityList.add(rs.getString("name"));
                }

            } catch (SQLException e) {
                System.err.println("Error fetching cities: " + e.getMessage());
            }

            Platform.runLater(() -> {
                menu.getItems().clear();
                for (String city : cityList) {
                    MenuItem item = new MenuItem(city);
                    item.setOnAction(e -> {
                        textField.setText(city);
                        menu.hide();
                    });
                    menu.getItems().add(item);
                }

                if (!menu.getItems().isEmpty()) {
                    menu.show(textField, Side.BOTTOM, 0, 0);
                } else {
                    menu.hide();
                }
            });
        }).start();
    }

    @FXML
    private void handleSearchFlight(ActionEvent event) {
        String from = fromField.getText().trim();
        String to = toField.getText().trim();
        LocalDate date = flightDatePicker.getValue();

        if (from.isEmpty() || to.isEmpty() || date == null) {
            showAlert(Alert.AlertType.WARNING, "Incomplete Input", "Please fill all fields to search flights.");
            return;
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT flight_number FROM flights WHERE from_city = ? AND to_city = ? AND flight_date = ?")) {
            stmt.setString(1, from);
            stmt.setString(2, to);
            stmt.setString(3, date.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Flight found, proceed to seat selection
                openSeatSelectionPage(event, from, to, date.toString());
            } else {
                showAlert(Alert.AlertType.INFORMATION, "No Flights Found", "No flights available for the selected criteria.");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error fetching flights: " + e.getMessage());
        }
    }

    private void openSeatSelectionPage(ActionEvent event, String from, String to, String date) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/airlinesmanagement1/SeatSelection.fxml"));
            Parent root = loader.load();

            SeatSelectionController controller = loader.getController();
            controller.setFlightInfo(from, to, date);
            controller.setCurrentUsername(Session.getInstance().getUsername()); // This should now return "baba_yaga"

            Stage stage = new Stage();
            stage.setTitle("Select Your Seat");
            stage.setScene(new Scene(root));
            stage.show();

            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "FXML Load Error", "Could not load seat selection page.");
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close(); // Close the popup window
    }
}
