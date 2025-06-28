package com.example.airlinesmanagement1;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class PaymentController {

    @FXML private Label totalAmountLabel;
    @FXML private Label fromValueLabel;
    @FXML private Label toValueLabel;
    @FXML private Label dateValueLabel;
    @FXML private Label seatsLabel;
    
    private ToggleGroup paymentMethodGroup;
    @FXML private RadioButton bkashRadio, rocketRadio, nagadRadio, upayRadio;
    @FXML private RadioButton visaRadio, mastercardRadio, amexRadio;
    @FXML private RadioButton easternBankRadio, bracBankRadio, cityBankRadio, ucbRadio;
    
    @FXML private TextField cardNumberField, expiryField, cvvField, cardholderField, phoneField;
    @FXML private Button payButton, cancelButton;

    private double totalAmount;
    private String fromCity, toCity, flightDate;
    private Set<String> selectedSeats;
    private String currentUsername;
    private int flightId;
    private double seatPrice;
    private Map<String, javafx.scene.control.ToggleButton> selectedSeatButtons;
    private Runnable onPaymentSuccess;

    public void initialize() {
        // Set up radio button groups
        paymentMethodGroup = new ToggleGroup();
        bkashRadio.setToggleGroup(paymentMethodGroup);
        rocketRadio.setToggleGroup(paymentMethodGroup);
        nagadRadio.setToggleGroup(paymentMethodGroup);
        upayRadio.setToggleGroup(paymentMethodGroup);
        visaRadio.setToggleGroup(paymentMethodGroup);
        mastercardRadio.setToggleGroup(paymentMethodGroup);
        amexRadio.setToggleGroup(paymentMethodGroup);
        easternBankRadio.setToggleGroup(paymentMethodGroup);
        bracBankRadio.setToggleGroup(paymentMethodGroup);
        cityBankRadio.setToggleGroup(paymentMethodGroup);
        ucbRadio.setToggleGroup(paymentMethodGroup);

        // Add listeners for payment method changes
        paymentMethodGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updatePaymentFields();
            }
        });

        // Add input validation
        setupInputValidation();
    }

    public void setPaymentData(double totalAmount, String fromCity, String toCity, String flightDate, 
                              Set<String> selectedSeats, String currentUsername, int flightId, 
                              double seatPrice, Map<String, javafx.scene.control.ToggleButton> selectedSeatButtons,
                              Runnable onPaymentSuccess) {
        this.totalAmount = totalAmount;
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.flightDate = flightDate;
        this.selectedSeats = selectedSeats;
        this.currentUsername = currentUsername;
        this.flightId = flightId;
        this.seatPrice = seatPrice;
        this.selectedSeatButtons = selectedSeatButtons;
        this.onPaymentSuccess = onPaymentSuccess;

        updateUI();
    }

    private void updateUI() {
        totalAmountLabel.setText(String.format("Total Amount: %.2f BDT", totalAmount));
        fromValueLabel.setText(fromCity);
        toValueLabel.setText(toCity);
        dateValueLabel.setText(flightDate);
        seatsLabel.setText("Selected Seats: " + String.join(", ", selectedSeats));
    }

    private void updatePaymentFields() {
        RadioButton selected = (RadioButton) paymentMethodGroup.getSelectedToggle();
        if (selected == null) return;

        String method = selected.getUserData().toString();
        
        // Reset all fields
        cardNumberField.setVisible(true);
        expiryField.setVisible(true);
        cvvField.setVisible(true);
        cardholderField.setVisible(true);
        phoneField.setVisible(true);

        if (method.equals("bKash") || method.equals("Rocket") || method.equals("Nagad") || method.equals("Upay")) {
            // Mobile banking - show phone field prominently
            cardNumberField.setPromptText("Account Number");
            expiryField.setVisible(false);
            cvvField.setVisible(false);
            cardholderField.setVisible(false);
            phoneField.setPromptText("Mobile Number");
        } else if (method.equals("Visa") || method.equals("Mastercard") || method.equals("Amex")) {
            // Credit/Debit cards
            cardNumberField.setPromptText("Card Number");
            expiryField.setPromptText("MM/YY");
            cvvField.setPromptText("CVV");
            cardholderField.setPromptText("Cardholder Name");
            phoneField.setVisible(false);
        } else {
            // Internet banking
            cardNumberField.setPromptText("Account Number");
            expiryField.setVisible(false);
            cvvField.setVisible(false);
            cardholderField.setPromptText("Account Holder Name");
            phoneField.setPromptText("Phone Number");
        }
    }

    private void setupInputValidation() {
        // Card number validation (only numbers, max 16 digits)
        cardNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,16}")) {
                cardNumberField.setText(oldValue);
            }
        });

        // Expiry date validation (MM/YY format)
        expiryField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,2}/?\\d{0,2}")) {
                expiryField.setText(oldValue);
            }
            if (newValue.length() == 2 && !newValue.endsWith("/")) {
                expiryField.setText(newValue + "/");
            }
        });

        // CVV validation (3-4 digits)
        cvvField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,4}")) {
                cvvField.setText(oldValue);
            }
        });

        // Phone number validation
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,11}")) {
                phoneField.setText(oldValue);
            }
        });
    }

    @FXML
    private void handlePayment() {
        if (!validatePaymentForm()) {
            return;
        }

        // Show processing dialog
        showProcessingDialog();
        
        // Simulate payment processing
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Simulate processing time
                
                Platform.runLater(() -> {
                    hideProcessingDialog();
                    processPayment();
                });
            } catch (InterruptedException e) {
                Platform.runLater(this::hideProcessingDialog);
            }
        }).start();
    }

    private boolean validatePaymentForm() {
        RadioButton selected = (RadioButton) paymentMethodGroup.getSelectedToggle();
        if (selected == null) {
            showAlert(AlertType.WARNING, "Payment Method Required", "Please select a payment method.");
            return false;
        }

        String method = selected.getUserData().toString();
        
        if (method.equals("bKash") || method.equals("Rocket") || method.equals("Nagad") || method.equals("Upay")) {
            if (cardNumberField.getText().trim().isEmpty()) {
                showAlert(AlertType.WARNING, "Account Number Required", "Please enter your account number.");
                return false;
            }
            if (phoneField.getText().trim().isEmpty()) {
                showAlert(AlertType.WARNING, "Phone Number Required", "Please enter your mobile number.");
                return false;
            }
            if (!phoneField.getText().matches("\\d{11}")) {
                showAlert(AlertType.WARNING, "Invalid Phone Number", "Please enter a valid 11-digit phone number.");
                return false;
            }
        } else if (method.equals("Visa") || method.equals("Mastercard") || method.equals("Amex")) {
            if (cardNumberField.getText().trim().isEmpty()) {
                showAlert(AlertType.WARNING, "Card Number Required", "Please enter your card number.");
                return false;
            }
            if (cardNumberField.getText().length() < 13 || cardNumberField.getText().length() > 19) {
                showAlert(AlertType.WARNING, "Invalid Card Number", "Please enter a valid card number.");
                return false;
            }
            if (expiryField.getText().trim().isEmpty()) {
                showAlert(AlertType.WARNING, "Expiry Date Required", "Please enter card expiry date.");
                return false;
            }
            if (!expiryField.getText().matches("\\d{2}/\\d{2}")) {
                showAlert(AlertType.WARNING, "Invalid Expiry Date", "Please enter expiry date in MM/YY format.");
                return false;
            }
            if (cvvField.getText().trim().isEmpty()) {
                showAlert(AlertType.WARNING, "CVV Required", "Please enter CVV.");
                return false;
            }
            if (cvvField.getText().length() < 3 || cvvField.getText().length() > 4) {
                showAlert(AlertType.WARNING, "Invalid CVV", "Please enter a valid CVV.");
                return false;
            }
            if (cardholderField.getText().trim().isEmpty()) {
                showAlert(AlertType.WARNING, "Cardholder Name Required", "Please enter cardholder name.");
                return false;
            }
        } else {
            // Internet banking
            if (cardNumberField.getText().trim().isEmpty()) {
                showAlert(AlertType.WARNING, "Account Number Required", "Please enter your account number.");
                return false;
            }
            if (cardholderField.getText().trim().isEmpty()) {
                showAlert(AlertType.WARNING, "Account Holder Name Required", "Please enter account holder name.");
                return false;
            }
        }

        return true;
    }

    private Dialog<Void> processingDialog;

    private void showProcessingDialog() {
        processingDialog = new Dialog<>();
        processingDialog.setTitle("Processing Payment");
        processingDialog.setHeaderText(null);
        processingDialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        DialogPane dialogPane = processingDialog.getDialogPane();
        dialogPane.getButtonTypes().add(ButtonType.CANCEL);

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        ProgressIndicator progress = new ProgressIndicator();
        progress.setPrefSize(50, 50);

        Label message = new Label("Processing your payment...\nPlease wait.");
        message.setStyle("-fx-font-size: 14px;");

        content.getChildren().addAll(progress, message);
        dialogPane.setContent(content);

        processingDialog.show();
    }

    private void hideProcessingDialog() {
        if (processingDialog != null) {
            processingDialog.close();
        }
    }

    private void processPayment() {
        RadioButton selected = (RadioButton) paymentMethodGroup.getSelectedToggle();
        String paymentMethod = selected.getUserData().toString();

        try {
            // Process the booking
            confirmSeatBookings(paymentMethod);
            
            // Show success dialog
            showSuccessDialog(paymentMethod);
            
            // Call the success callback
            if (onPaymentSuccess != null) {
                onPaymentSuccess.run();
            }
            
            // Close the payment window
            ((Stage) payButton.getScene().getWindow()).close();
            
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Payment Failed", "An error occurred during payment processing: " + e.getMessage());
        }
    }

    private void confirmSeatBookings(String paymentMethod) throws SQLException {
        if (currentUsername == null) {
            currentUsername = "baba_yaga"; // Fallback
        }

        try (Connection conn = new DatabaseConnection().getConnection()) {
            conn.setAutoCommit(false);

            for (String seatNo : selectedSeats) {
                // Update seat status
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
                    throw new SQLException("Seat " + seatNo + " is already booked.");
                }

                // Insert ticket record
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
        }
    }

    private void showSuccessDialog(String paymentMethod) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Payment Successful");
        alert.setHeaderText("🎉 Payment Completed Successfully!");
        alert.setContentText(String.format(
                "Payment Method: %s\n" +
                "Amount Paid: %.2f BDT\n" +
                "Transaction ID: %s\n\n" +
                "Your tickets have been booked successfully!\n" +
                "You can view your tickets in your account.",
                paymentMethod, totalAmount, generateTransactionId()
        ));
        alert.showAndWait();
    }

    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis();
    }

    @FXML
    private void handleCancel() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Cancel Payment");
        alert.setHeaderText("Cancel Payment?");
        alert.setContentText("Are you sure you want to cancel the payment? Your seat selection will be lost.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ((Stage) cancelButton.getScene().getWindow()).close();
            }
        });
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 