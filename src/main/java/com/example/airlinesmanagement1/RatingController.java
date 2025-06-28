package com.example.airlinesmanagement1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class RatingController implements Initializable {

    @FXML private Button star1, star2, star3, star4, star5;
    @FXML private TextArea feedbackText;
    @FXML private TextField flightNumberField;
    @FXML private Button submitButton;
    @FXML private Label submitMessage;
    @FXML private VBox ratingsContainer;
    @FXML private Label averageRating;
    @FXML private Label totalRatings;
    @FXML private Label ratingText;

    private int selectedRating = 0;
    private Button[] stars;

    private final String DB_URL = "jdbc:mysql://localhost:3306/airlines_management1?useSSL=false";
    private final String DB_USER = "root";
    private final String DB_PASSWORD = "";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupStars();
        createRatingsTableIfNotExists();
        loadRatings();
        calculateAverageRating();
    }

    private void setupStars() {
        stars = new Button[]{star1, star2, star3, star4, star5};
        
        // Set up hover effects for stars
        for (int i = 0; i < stars.length; i++) {
            final int starIndex = i + 1;
            Button star = stars[i];
            
            star.setOnMouseEntered(e -> highlightStars(starIndex));
            star.setOnMouseExited(e -> updateStarDisplay());
        }
    }

    @FXML
    private void selectRating(ActionEvent event) {
        Button clickedStar = (Button) event.getSource();
        
        // Find which star was clicked
        for (int i = 0; i < stars.length; i++) {
            if (stars[i] == clickedStar) {
                selectedRating = i + 1;
                break;
            }
        }
        
        updateStarDisplay();
        updateRatingText();
    }

    private void highlightStars(int rating) {
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setStyle("-fx-font-size: 24px; -fx-text-fill: #FFD700; -fx-background-color: transparent; -fx-border-color: transparent;");
            } else {
                stars[i].setStyle("-fx-font-size: 24px; -fx-text-fill: #ddd; -fx-background-color: transparent; -fx-border-color: transparent;");
            }
        }
    }

    private void updateStarDisplay() {
        highlightStars(selectedRating);
    }

    private void updateRatingText() {
        String[] ratingDescriptions = {
            "Click on a star to rate",
            "Poor",
            "Fair", 
            "Good",
            "Very Good",
            "Excellent"
        };
        
        if (selectedRating >= 0 && selectedRating < ratingDescriptions.length) {
            ratingText.setText(ratingDescriptions[selectedRating]);
        }
    }

    private void createRatingsTableIfNotExists() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS ratings (
                id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) NOT NULL,
                rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
                feedback TEXT NOT NULL,
                flight_number VARCHAR(20),
                rating_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY unique_user_rating (username)
            )
            """;
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Ratings table created/verified successfully.");
        } catch (SQLException e) {
            System.err.println("Error creating ratings table: " + e.getMessage());
        }
    }

    @FXML
    private void submitRating() {
        String currentUser = Session.getInstance().getUsername();
        if (currentUser == null || currentUser.isEmpty()) {
            showAlert("Error", "Please log in to submit a rating.");
            return;
        }

        if (selectedRating == 0) {
            showAlert("Error", "Please select a rating.");
            return;
        }

        String feedback = feedbackText.getText().trim();
        String flightNumber = flightNumberField.getText().trim();
        
        if (feedback.isEmpty()) {
            showAlert("Error", "Please provide feedback.");
            return;
        }

        // Check if user has already rated
        if (hasUserRated(currentUser)) {
            showAlert("Error", "You have already submitted a rating. You can only rate once.");
            return;
        }

        // Save rating to database
        if (saveRating(currentUser, selectedRating, feedback, flightNumber)) {
            submitMessage.setText("Thank you for your feedback! Your rating has been submitted.");
            submitMessage.setStyle("-fx-text-fill: green;");
            
            // Clear form
            selectedRating = 0;
            updateStarDisplay();
            updateRatingText();
            feedbackText.clear();
            flightNumberField.clear();
            
            // Refresh ratings display
            loadRatings();
            calculateAverageRating();
        } else {
            submitMessage.setText("Error submitting rating. Please try again.");
            submitMessage.setStyle("-fx-text-fill: red;");
        }
    }

    private boolean hasUserRated(String username) {
        String query = "SELECT COUNT(*) FROM ratings WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking user rating: " + e.getMessage());
        }
        return false;
    }

    private boolean saveRating(String username, int rating, String feedback, String flightNumber) {
        String query = "INSERT INTO ratings (username, rating, feedback, flight_number, rating_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setInt(2, rating);
            stmt.setString(3, feedback);
            stmt.setString(4, flightNumber.isEmpty() ? null : flightNumber);
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving rating: " + e.getMessage());
            return false;
        }
    }

    private void loadRatings() {
        ratingsContainer.getChildren().clear();
        
        String query = "SELECT username, rating, feedback, flight_number, rating_date FROM ratings ORDER BY rating_date DESC LIMIT 20";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                String username = rs.getString("username");
                int rating = rs.getInt("rating");
                String feedback = rs.getString("feedback");
                String flightNumber = rs.getString("flight_number");
                Timestamp ratingDate = rs.getTimestamp("rating_date");
                
                VBox ratingCard = createRatingCard(username, rating, feedback, flightNumber, ratingDate);
                ratingsContainer.getChildren().add(ratingCard);
            }
        } catch (SQLException e) {
            System.err.println("Error loading ratings: " + e.getMessage());
        }
    }

    private VBox createRatingCard(String username, int rating, String feedback, String flightNumber, Timestamp ratingDate) {
        VBox card = new VBox(6);
        card.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-background-radius: 6; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);");
        card.setPrefWidth(320);

        // Header with username and rating
        HBox header = new HBox(8);
        Label userLabel = new Label("User: " + username);
        userLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        
        Label ratingLabel = new Label(getStars(rating));
        ratingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #FFD700;");
        
        header.getChildren().addAll(userLabel, ratingLabel);
        
        // Feedback text
        Label feedbackLabel = new Label(feedback);
        feedbackLabel.setWrapText(true);
        feedbackLabel.setStyle("-fx-font-size: 11px;");
        
        // Footer with flight number and date
        HBox footer = new HBox(12);
        if (flightNumber != null && !flightNumber.isEmpty()) {
            Label flightLabel = new Label("Flight: " + flightNumber);
            flightLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
            footer.getChildren().add(flightLabel);
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        Label dateLabel = new Label(ratingDate.toLocalDateTime().format(formatter));
        dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        footer.getChildren().add(dateLabel);
        
        card.getChildren().addAll(header, feedbackLabel, footer);
        return card;
    }

    private String getStars(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) {
            stars.append("★");
        }
        return stars.toString();
    }

    private void calculateAverageRating() {
        String query = "SELECT AVG(rating) as avg_rating, COUNT(*) as total FROM ratings";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                double avgRating = rs.getDouble("avg_rating");
                int total = rs.getInt("total");
                
                if (total > 0) {
                    averageRating.setText(String.format("Average Rating: %s (%.1f)", getStars((int)Math.round(avgRating)), avgRating));
                } else {
                    averageRating.setText("Average Rating: No ratings yet");
                }
                totalRatings.setText("Total Reviews: " + total);
            }
        } catch (SQLException e) {
            System.err.println("Error calculating average rating: " + e.getMessage());
        }
    }

    @FXML
    private void refreshRatings() {
        loadRatings();
        calculateAverageRating();
        showAlert("Success", "Ratings refreshed successfully!");
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 