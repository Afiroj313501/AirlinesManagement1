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
    private Label completedFlightsLabel;

    @FXML
    private Label upcomingFlightsLabel;

    @FXML
    private Label pendingFlightsLabel;

    @FXML
    private Label cancelledFlightsLabel;

    @FXML
    private Label ratingCountLabel;

    @FXML
    private Label nextFlightLabel;

    @FXML
    private Label totalFlightsLabel;

    @FXML
    private Label totalSpentLabel;

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
        loadCompletedFlights();
        loadUpcomingFlights();
        loadPendingFlights();
        loadCancelledFlights();
        loadNextFlight();
        loadTotalFlights();
        loadTotalSpent();
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
                    String currentUser = Session.getInstance().getUsername();
                    if (currentUser != null && !currentUser.isEmpty()) {
                        String query = "SELECT COUNT(*) as count FROM tickets WHERE user_name = ?";
                        PreparedStatement pstmt = conn.prepareStatement(query);
                        pstmt.setString(1, currentUser);
                        ResultSet rs = pstmt.executeQuery();
                        
                        if (rs.next()) {
                            int count = rs.getInt("count");
                            bookingCountLabel.setText(String.valueOf(count));
                            System.out.println("Found " + count + " bookings for user: " + currentUser);
                        }
                    } else {
                        bookingCountLabel.setText("0");
                        System.out.println("No user logged in for booking count");
                    }
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error loading booking count: " + e.getMessage());
                bookingCountLabel.setText("0");
            }
        }
    }

    private void loadCompletedFlights() {
        if (completedFlightsLabel != null) {
            try {
                DatabaseConnection dbConnection = new DatabaseConnection();
                Connection conn = dbConnection.getConnection();
                if (conn != null) {
                    String currentUser = Session.getInstance().getUsername();
                    if (currentUser != null && !currentUser.isEmpty()) {
                        // Count flights that are completed (past date or status = completed)
                        String query = "SELECT COUNT(DISTINCT t.flight_id) as count " +
                                     "FROM tickets t " +
                                     "JOIN flights f ON t.flight_id = f.flight_id " +
                                     "WHERE t.user_name = ? AND (f.flight_date < CURDATE() OR f.status = 'completed')";
                        PreparedStatement pstmt = conn.prepareStatement(query);
                        pstmt.setString(1, currentUser);
                        ResultSet rs = pstmt.executeQuery();
                        
                        if (rs.next()) {
                            int count = rs.getInt("count");
                            completedFlightsLabel.setText(String.valueOf(count));
                            System.out.println("Found " + count + " completed flights for user: " + currentUser);
                        }
                    } else {
                        completedFlightsLabel.setText("0");
                        System.out.println("No user logged in for completed flights");
                    }
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error loading completed flights: " + e.getMessage());
                completedFlightsLabel.setText("0");
            }
        }
    }

    private void loadUpcomingFlights() {
        if (upcomingFlightsLabel != null) {
            try {
                DatabaseConnection dbConnection = new DatabaseConnection();
                Connection conn = dbConnection.getConnection();
                if (conn != null) {
                    String currentUser = Session.getInstance().getUsername();
                    if (currentUser != null && !currentUser.isEmpty()) {
                        // Count flights that are upcoming (future date and not cancelled)
                        String query = "SELECT COUNT(DISTINCT t.flight_id) as count " +
                                     "FROM tickets t " +
                                     "JOIN flights f ON t.flight_id = f.flight_id " +
                                     "WHERE t.user_name = ? AND f.flight_date > CURDATE() AND f.status != 'cancelled'";
                        PreparedStatement pstmt = conn.prepareStatement(query);
                        pstmt.setString(1, currentUser);
                        ResultSet rs = pstmt.executeQuery();
                        
                        if (rs.next()) {
                            int count = rs.getInt("count");
                            upcomingFlightsLabel.setText(String.valueOf(count));
                            System.out.println("Found " + count + " upcoming flights for user: " + currentUser);
                        }
                    } else {
                        upcomingFlightsLabel.setText("0");
                        System.out.println("No user logged in for upcoming flights");
                    }
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error loading upcoming flights: " + e.getMessage());
                upcomingFlightsLabel.setText("0");
            }
        }
    }

    private void loadPendingFlights() {
        if (pendingFlightsLabel != null) {
            try {
                DatabaseConnection dbConnection = new DatabaseConnection();
                Connection conn = dbConnection.getConnection();
                if (conn != null) {
                    String currentUser = Session.getInstance().getUsername();
                    if (currentUser != null && !currentUser.isEmpty()) {
                        // Count flights that are pending (today's date or status = pending)
                        String query = "SELECT COUNT(DISTINCT t.flight_id) as count " +
                                     "FROM tickets t " +
                                     "JOIN flights f ON t.flight_id = f.flight_id " +
                                     "WHERE t.user_name = ? AND (f.flight_date = CURDATE() OR f.status = 'pending')";
                        PreparedStatement pstmt = conn.prepareStatement(query);
                        pstmt.setString(1, currentUser);
                        ResultSet rs = pstmt.executeQuery();
                        
                        if (rs.next()) {
                            int count = rs.getInt("count");
                            pendingFlightsLabel.setText(String.valueOf(count));
                            System.out.println("Found " + count + " pending flights for user: " + currentUser);
                        }
                    } else {
                        pendingFlightsLabel.setText("0");
                        System.out.println("No user logged in for pending flights");
                    }
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error loading pending flights: " + e.getMessage());
                pendingFlightsLabel.setText("0");
            }
        }
    }

    private void loadCancelledFlights() {
        if (cancelledFlightsLabel != null) {
            try {
                DatabaseConnection dbConnection = new DatabaseConnection();
                Connection conn = dbConnection.getConnection();
                if (conn != null) {
                    String currentUser = Session.getInstance().getUsername();
                    if (currentUser != null && !currentUser.isEmpty()) {
                        // Count flights that are cancelled (status = cancelled)
                        String query = "SELECT COUNT(DISTINCT t.flight_id) as count " +
                                     "FROM tickets t " +
                                     "JOIN flights f ON t.flight_id = f.flight_id " +
                                     "WHERE t.user_name = ? AND f.status = 'cancelled'";
                        PreparedStatement pstmt = conn.prepareStatement(query);
                        pstmt.setString(1, currentUser);
                        ResultSet rs = pstmt.executeQuery();
                        
                        if (rs.next()) {
                            int count = rs.getInt("count");
                            cancelledFlightsLabel.setText(String.valueOf(count));
                            System.out.println("Found " + count + " cancelled flights for user: " + currentUser);
                        }
                    } else {
                        cancelledFlightsLabel.setText("0");
                        System.out.println("No user logged in for cancelled flights");
                    }
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error loading cancelled flights: " + e.getMessage());
                cancelledFlightsLabel.setText("0");
            }
        }
    }

    private void loadRatingCount() {
        if (ratingCountLabel != null) {
            try {
                DatabaseConnection dbConnection = new DatabaseConnection();
                Connection conn = dbConnection.getConnection();
                if (conn != null) {
                    String currentUser = Session.getInstance().getUsername();
                    if (currentUser != null && !currentUser.isEmpty()) {
                        String query = "SELECT COUNT(*) as count FROM ratings WHERE username = ?";
                        PreparedStatement pstmt = conn.prepareStatement(query);
                        pstmt.setString(1, currentUser);
                        ResultSet rs = pstmt.executeQuery();
                        
                        if (rs.next()) {
                            int count = rs.getInt("count");
                            ratingCountLabel.setText(String.valueOf(count));
                            System.out.println("Found " + count + " ratings for user: " + currentUser);
                        }
                    } else {
                        ratingCountLabel.setText("0");
                        System.out.println("No user logged in for rating count");
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
            String username = Session.getInstance().getUsername();
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

    @FXML
    private void refreshStats(ActionEvent event) {
        System.out.println("Manual refresh requested...");
        refreshStats();
    }

    // Method to refresh dashboard stats
    public void refreshStats() {
        loadFlightCount();
        loadBookingCount();
        loadCompletedFlights();
        loadUpcomingFlights();
        loadPendingFlights();
        loadCancelledFlights();
        loadNextFlight();
        loadTotalFlights();
        loadTotalSpent();
    }

    // Method to refresh stats when dashboard is shown
    public void onDashboardShown() {
        System.out.println("Dashboard shown - refreshing stats...");
        refreshStats();
    }

    // Method to stop clock when leaving dashboard
    public void stopClock() {
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
    }

    private void loadNextFlight() {
        if (nextFlightLabel != null) {
            try {
                DatabaseConnection dbConnection = new DatabaseConnection();
                Connection conn = dbConnection.getConnection();
                if (conn != null) {
                    String currentUser = Session.getInstance().getUsername();
                    if (currentUser != null && !currentUser.isEmpty()) {
                        // Get the next upcoming flight
                        String query = "SELECT f.flight_number, f.from_city, f.to_city, f.flight_date " +
                                     "FROM tickets t " +
                                     "JOIN flights f ON t.flight_id = f.flight_id " +
                                     "WHERE t.user_name = ? AND f.flight_date >= CURDATE() " +
                                     "ORDER BY f.flight_date ASC LIMIT 1";
                        PreparedStatement pstmt = conn.prepareStatement(query);
                        pstmt.setString(1, currentUser);
                        ResultSet rs = pstmt.executeQuery();
                        
                        if (rs.next()) {
                            String flightNumber = rs.getString("flight_number");
                            String fromCity = rs.getString("from_city");
                            String toCity = rs.getString("to_city");
                            String flightDate = rs.getDate("flight_date").toString();
                            nextFlightLabel.setText(flightNumber + "\n" + fromCity + " → " + toCity + "\n" + flightDate);
                            System.out.println("Next flight: " + flightNumber + " on " + flightDate);
                        } else {
                            nextFlightLabel.setText("No upcoming flights");
                            System.out.println("No upcoming flights found for user: " + currentUser);
                        }
                    } else {
                        nextFlightLabel.setText("No upcoming flights");
                        System.out.println("No user logged in for next flight");
                    }
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error loading next flight: " + e.getMessage());
                nextFlightLabel.setText("No upcoming flights");
            }
        }
    }

    private void loadTotalFlights() {
        if (totalFlightsLabel != null) {
            try {
                DatabaseConnection dbConnection = new DatabaseConnection();
                Connection conn = dbConnection.getConnection();
                if (conn != null) {
                    String currentUser = Session.getInstance().getUsername();
                    if (currentUser != null && !currentUser.isEmpty()) {
                        // Count total unique flights user has booked
                        String query = "SELECT COUNT(DISTINCT t.flight_id) as count " +
                                     "FROM tickets t " +
                                     "WHERE t.user_name = ?";
                        PreparedStatement pstmt = conn.prepareStatement(query);
                        pstmt.setString(1, currentUser);
                        ResultSet rs = pstmt.executeQuery();
                        
                        if (rs.next()) {
                            int count = rs.getInt("count");
                            totalFlightsLabel.setText(String.valueOf(count));
                            System.out.println("Total flights for user: " + currentUser + " = " + count);
                        }
                    } else {
                        totalFlightsLabel.setText("0");
                        System.out.println("No user logged in for total flights");
                    }
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error loading total flights: " + e.getMessage());
                totalFlightsLabel.setText("0");
            }
        }
    }

    private void loadTotalSpent() {
        if (totalSpentLabel != null) {
            try {
                DatabaseConnection dbConnection = new DatabaseConnection();
                Connection conn = dbConnection.getConnection();
                if (conn != null) {
                    String currentUser = Session.getInstance().getUsername();
                    if (currentUser != null && !currentUser.isEmpty()) {
                        // Calculate total amount spent on tickets
                        String query = "SELECT SUM(t.price) as total " +
                                     "FROM tickets t " +
                                     "WHERE t.user_name = ?";
                        PreparedStatement pstmt = conn.prepareStatement(query);
                        pstmt.setString(1, currentUser);
                        ResultSet rs = pstmt.executeQuery();
                        
                        if (rs.next()) {
                            double total = rs.getDouble("total");
                            if (total > 0) {
                                totalSpentLabel.setText("BDT " + String.format("%.2f", total));
                                System.out.println("Total spent by user: " + currentUser + " = BDT " + total);
                            } else {
                                totalSpentLabel.setText("BDT 0");
                                System.out.println("No spending found for user: " + currentUser);
                            }
                        }
                    } else {
                        totalSpentLabel.setText("BDT 0");
                        System.out.println("No user logged in for total spent");
                    }
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error loading total spent: " + e.getMessage());
                totalSpentLabel.setText("BDT 0");
            }
        }
    }
}