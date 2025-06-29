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
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Label flightCountLabel;

    @FXML
    private Label bookingCountLabel;

    @FXML
    private Label ratingCountLabel;

    @FXML
    private HBox footerBox;

    @FXML
    private javafx.scene.control.Button ticketButton;

    @FXML
    private javafx.scene.control.Button adminPanelButton;

    private Scene currentScene;
    private Timeline clockTimeline;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentScene = welcomeLabel.getScene();
        if (currentScene == null) {
            System.err.println("Warning: currentScene is null during initialization. Check application setup.");
        } else {
            System.out.println("Initialized currentScene successfully.");
        }

        // Initialize dashboard components
        initializeClock();
        initializeStats();
        initializeWelcomeMessage();
        initializeFooterAnimation();
    }

    private void initializeClock() {
        if (timeLabel != null) {
            // Create a timeline that updates every second
            clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                LocalDateTime now = LocalDateTime.now();
                String formattedTime = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                String formattedDate = now.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
                timeLabel.setText(formattedTime + " | " + formattedDate);
            }));
            clockTimeline.setCycleCount(Timeline.INDEFINITE);
            clockTimeline.play();
        }
    }

    private void initializeStats() {
        // Load statistics from database
        loadFlightCount();
        loadBookingCount();
        loadRatingCount();
    }

    private void loadFlightCount() {
        if (flightCountLabel != null) {
            try {
                DatabaseConnection dbConnection = new DatabaseConnection();
                Connection conn = dbConnection.getConnection();
                if (conn != null) {
                    String query = "SELECT COUNT(*) as count FROM flights";
                    PreparedStatement pstmt = conn.prepareStatement(query);
                    ResultSet rs = pstmt.executeQuery();
                    
                    if (rs.next()) {
                        int count = rs.getInt("count");
                        flightCountLabel.setText(String.valueOf(count));
                    }
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error loading flight count: " + e.getMessage());
                flightCountLabel.setText("0");
            }
        }
    }

    private void loadBookingCount() {
        if (bookingCountLabel != null) {
            try {
                DatabaseConnection dbConnection = new DatabaseConnection();
                Connection conn = dbConnection.getConnection();
                if (conn != null) {
                    String query = "SELECT COUNT(*) as count FROM tickets WHERE user_name = ?";
                    PreparedStatement pstmt = conn.prepareStatement(query);
                    pstmt.setString(1, CurrentUser.getUsername());
                    ResultSet rs = pstmt.executeQuery();
                    
                    if (rs.next()) {
                        int count = rs.getInt("count");
                        bookingCountLabel.setText(String.valueOf(count));
                    }
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error loading booking count: " + e.getMessage());
                bookingCountLabel.setText("0");
            }
        }
    }

    private void loadRatingCount() {
        if (ratingCountLabel != null) {
            try {
                DatabaseConnection dbConnection = new DatabaseConnection();
                Connection conn = dbConnection.getConnection();
                if (conn != null) {
                    String query = "SELECT COUNT(*) as count FROM ratings WHERE username = ?";
                    PreparedStatement pstmt = conn.prepareStatement(query);
                    pstmt.setString(1, CurrentUser.getUsername());
                    ResultSet rs = pstmt.executeQuery();
                    
                    if (rs.next()) {
                        int count = rs.getInt("count");
                        ratingCountLabel.setText(String.valueOf(count));
                    }
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error loading rating count: " + e.getMessage());
                ratingCountLabel.setText("0");
            }
        }
    }

    private void initializeWelcomeMessage() {
        if (welcomeLabel != null) {
            String username = CurrentUser.getUsername();
            if (username != null && !username.isEmpty()) {
                welcomeLabel.setText("Welcome back, " + username + "!");
            } else {
                welcomeLabel.setText("Welcome back!");
            }
        }
    }

    private void initializeFooterAnimation() {
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
        // Stop the clock timeline
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
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
        openPopup(event, "Rating.fxml", "Rate Our Service");
    }

    // Method to refresh dashboard stats
    public void refreshStats() {
        loadFlightCount();
        loadBookingCount();
        loadRatingCount();
    }

    // Method to stop clock when leaving dashboard
    public void stopClock() {
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
    }
}