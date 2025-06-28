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

    @FXML
    private javafx.scene.control.Button ticketButton;

    @FXML
    private javafx.scene.control.Button adminPanelButton;

    private Scene currentScene;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentScene = welcomeLabel.getScene();
        if (currentScene == null) {
            System.err.println("Warning: currentScene is null during initialization. Check application setup.");
        } else {
            System.out.println("Initialized currentScene successfully.");
        }

        if (dashboardImageView != null) {
            try {
                dashboardImageView.setImage(
                        new Image(new File("Images/Dash.png").toURI().toString())
                );
                System.out.println("Dashboard image loaded successfully.");
            } catch (Exception e) {
                System.err.println("Failed to load dashboard image: " + e.getMessage());
            }
        } else {
            System.err.println("Warning: dashboardImageView is null. Check Dashboard.fxml for fx:id='dashboardImageView'.");
        }

        if (footerBox != null) {
            FadeTransition ft = new FadeTransition(Duration.millis(1000), footerBox);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

            TranslateTransition tt = new TranslateTransition(Duration.millis(1000), footerBox);
            tt.setFromY(20);
            tt.setToY(0);
            tt.play();
            System.out.println("Footer animations applied.");
        } else {
            System.err.println("Warning: footerBox is null. Check Dashboard.fxml for fx:id='footerBox'.");
        }

        if (ticketButton == null) {
            System.err.println("Warning: ticketButton is null. Check Dashboard.fxml for fx:id='ticketButton'.");
        }
        if (adminPanelButton == null) {
            System.err.println("Warning: adminPanelButton is null. Check Dashboard.fxml for fx:id='adminPanelButton'.");
        }
    }

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
            currentScene = scene; // Update currentScene after loading

            // Pass the current scene to SupportController if navigating to Support
            if ("Support.fxml".equals(fxmlFile)) {
                SupportController controller = loader.getController();
                if (controller != null) {
                    controller.setPreviousScene(currentScene); // Pass the dashboard scene
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openPopup(ActionEvent event, String fxmlFile, String title) {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/airlinesmanagement1/" + fxmlFile);
            if (fxmlUrl == null) {
                throw new IOException("FXML file not found: " + fxmlFile);
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(loader.load());
            Stage popupStage = new Stage();
            
            // Make support popup non-modal to prevent blocking admin panel
            if (fxmlFile.equals("Support.fxml")) {
                popupStage.initModality(Modality.NONE); // Non-modal for support
            } else {
                popupStage.initModality(Modality.APPLICATION_MODAL); // Modal for other popups
            }
            
            popupStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            popupStage.setScene(scene);
            popupStage.setTitle(title);
            popupStage.show(); // Use show() instead of showAndWait()
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
        System.out.println("Opening Manage Bookings popup...");
        openPopup(event, "FlightBook.fxml", "Manage Bookings");
        System.out.println("Manage Bookings action completed.");
    }

    @FXML
    private void goToManageFlight(ActionEvent event) {
        System.out.println("Opening Cancel Ticket popup...");
        openPopup(event, "CancelTicket.fxml", "Cancel Ticket");
        System.out.println("Cancel Ticket action completed.");
    }

    @FXML
    private void goToBookingHistory(ActionEvent event) {
        System.out.println("Navigating to Booking History...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/airlinesmanagement1/TicketView.fxml"));
            Scene scene = new Scene(loader.load());
            TicketController controller = loader.getController();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            if (currentScene == null) {
                System.err.println("Error: currentScene is null in goToBookingHistory. Using current scene as fallback.");
                currentScene = ((Node) event.getSource()).getScene();
            }
            controller.setPrimaryStage(stage, currentScene);
            stage.setScene(scene);
            stage.setTitle("My Tickets");
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading TicketView.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goToSupport(ActionEvent event) {
        System.out.println("Opening Support popup...");
        openPopup(event, "Support.fxml", "Customer Support Chat");
    }

    @FXML
    private void goToSettings(ActionEvent event) {
        System.out.println("Opening Rating and Feedback...");
        openPopup(event, "Rating.fxml", "Rate Your Experience");
    }
}