package com.example.airlinesmanagement1;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private Label welcomeLabel;

    @FXML
    private ImageView dashboardImageView;

    @FXML
    private HBox footerBox;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Load dashboard image
        if (dashboardImageView != null) {
            try {
                dashboardImageView.setImage(
                        new Image(new File("Images/Dash.png").toURI().toString())
                );
            } catch (Exception e) {
                System.err.println("Failed to load dashboard image: " + e.getMessage());
            }
        }

        // Apply footer animations
        if (footerBox != null) {
            FadeTransition ft = new FadeTransition(Duration.millis(1000), footerBox);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

            TranslateTransition tt = new TranslateTransition(Duration.millis(1000), footerBox);
            tt.setFromY(20);
            tt.setToY(0);
            tt.play();
        }
    }

    // Utility method to load new scene by FXML file name (for non-popup navigation)
    private void loadScene(ActionEvent event, String fxmlFile, String title) {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/airlinesmanagement1/" + fxmlFile);
            if (fxmlUrl == null) {
                throw new IOException("FXML file not found: " + fxmlFile);
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
            System.out.println("Successfully loaded " + fxmlFile);
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Utility method to open a popup window
    private void openPopup(ActionEvent event, String fxmlFile, String title) {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/airlinesmanagement1/" + fxmlFile);
            if (fxmlUrl == null) {
                throw new IOException("FXML file not found: " + fxmlFile);
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(loader.load());
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL); // Makes the popup modal
            popupStage.initOwner(((Node) event.getSource()).getScene().getWindow()); // Ties popup to parent window
            popupStage.setScene(scene);
            popupStage.setTitle(title);
            popupStage.showAndWait(); // Show and wait until popup is closed
            System.out.println("Successfully opened popup: " + fxmlFile);
        } catch (IOException e) {
            System.err.println("Error opening popup " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("Logging out...");
        loadScene(event, "Login.fxml", "Login Page");
    }

    @FXML
    private void goToFlightInfo(ActionEvent event) {
        System.out.println("Navigating to Flight Information...");
        loadScene(event, "FlightInfo.fxml", "Flight Information");
    }

    @FXML
    private void goToManageBookings(ActionEvent event) {
        System.out.println("Opening Book Flight popup...");
        openPopup(event, "FlightBook.fxml", "Book Flight");
    }

    @FXML
    private void goToBookingHistory(ActionEvent event) {
        System.out.println("Navigating to Booking History...");
        loadScene(event, "BookingHistory.fxml", "Booking History");
    }

    @FXML
    private void goToSupport(ActionEvent event) {
        System.out.println("Navigating to Support...");
        loadScene(event, "Support.fxml", "Support");
    }

    @FXML
    private void goToSettings(ActionEvent event) {
        System.out.println("Navigating to Settings...");
        loadScene(event, "Settings.fxml", "Settings");
    }
}