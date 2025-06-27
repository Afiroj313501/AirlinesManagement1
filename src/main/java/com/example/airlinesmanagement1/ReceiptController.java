package com.example.airlinesmanagement1;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReceiptController {

    @FXML private Label titleLabel;
    @FXML private Label dateLabel;
    @FXML private Label timeLabel;
    @FXML private Label priceLabel;
    @FXML private Label hallLabel;
    @FXML private Label rowLabel;
    @FXML private Label seatLabel;
    @FXML private Label classLabel;
    @FXML private Label locationLabel;

    private Stage primaryStage;

    public void setData(String flightNumber, String date, String time, String price,
                        String flightClass, String row, String seat, String passengerInfo, String location,
                        String logoPath, String qrPath) {
        // Set labels with null checks
        titleLabel.setText(flightNumber != null ? flightNumber : "Flight Not Specified");
        dateLabel.setText(date != null ? date : "N/A");
        timeLabel.setText(time != null ? time : "N/A");
        priceLabel.setText(price != null ? price : "0 BDT");
        hallLabel.setText(flightClass != null ? flightClass : "N/A");
        rowLabel.setText(row != null ? row : "N/A");
        seatLabel.setText(seat != null ? seat : "N/A");
        classLabel.setText(passengerInfo != null ? passengerInfo : "N/A");
        locationLabel.setText(location != null ? location : "N/A");

        // Trigger PDF generation (optional, can be removed if button handles it)
        // generatePdfTicket(null); // Uncomment if you want auto-trigger
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public void generatePdfTicket(javafx.event.ActionEvent event) {
        if (primaryStage == null) {
            System.out.println("Error: Primary stage not set. Cannot show FileChooser.");
            showAlert("Error", "Internal error: Stage not initialized. Please ensure the stage is set.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Ticket as PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file == null) {
            System.out.println("PDF generation cancelled by user or no file selected.");
            return; // Exit if no file is selected
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            // Create a text-based representation
            String pdfContent = "Airline Ticket\n" +
                    "----------------\n" +
                    "Flight Number: " + (titleLabel != null && titleLabel.getText() != null ? titleLabel.getText() : "") + "\n" +
                    "Date: " + (dateLabel != null && dateLabel.getText() != null ? dateLabel.getText() : "") + "\n" +
                    "Time: " + (timeLabel != null && timeLabel.getText() != null ? timeLabel.getText() : "") + "\n" +
                    "Price: " + (priceLabel != null && priceLabel.getText() != null ? priceLabel.getText() : "") + "\n" +
                    "Class: " + (hallLabel != null && hallLabel.getText() != null ? hallLabel.getText() : "") + "\n" +
                    "Row: " + (rowLabel != null && rowLabel.getText() != null ? rowLabel.getText() : "") + "\n" +
                    "Seat: " + (seatLabel != null && seatLabel.getText() != null ? seatLabel.getText() : "") + "\n" +
                    "Passenger: " + (classLabel != null && classLabel.getText() != null ? classLabel.getText() : "") + "\n" +
                    "Route: " + (locationLabel != null && locationLabel.getText() != null ? locationLabel.getText() : "") + "\n";
            fos.write(pdfContent.getBytes());
            fos.flush();

            // Safely get and log the absolute path
            String absolutePath = file.getAbsolutePath();
            if (absolutePath != null) {
                System.out.println("Ticket saved as text file with .pdf extension: " + absolutePath +
                        " (Not a true PDF due to no library support)");
                showAlert("Success", "Ticket saved as text file with .pdf extension: " + absolutePath +
                        "\nNote: This is not a valid PDF; consider using a library for proper formatting.");
            } else {
                System.out.println("Warning: Could not determine absolute path for saved file.");
                showAlert("Success", "Ticket saved, but absolute path could not be determined.");
            }
        } catch (IOException e) {
            System.out.println("Error generating PDF: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to save ticket: " + e.getMessage() +
                    "\nCheck file permissions or destination directory.");
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