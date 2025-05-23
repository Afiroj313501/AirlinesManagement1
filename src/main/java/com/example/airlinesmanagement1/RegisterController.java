package com.example.airlinesmanagement1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private DatePicker dobPicker;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;
    @FXML private ImageView brandingImageView;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            File brandingImageFile = new File("Images/regi.png");
            Image brandingImage = new Image(brandingImageFile.toURI().toString());
            brandingImageView.setImage(brandingImage);
        } catch (Exception e) {
            System.out.println("Error loading regi.png:");
            e.printStackTrace();
        }
    }

    @FXML
    private void registerUser(ActionEvent event) {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String dob = (dobPicker.getValue() != null) ? dobPicker.getValue().toString() : "";

        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty() || dob.isEmpty()) {
            messageLabel.setText("Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match.");
            return;
        }

        DatabaseConnection dbConnection = new DatabaseConnection();
        Connection connectDB = dbConnection.getConnection();

        if (connectDB == null) {
            messageLabel.setText("Database connection failed.");
            return;
        }

        String insertQuery = "INSERT INTO user_account (first_name, last_name, user_name, password, dob) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connectDB.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, username);
            preparedStatement.setString(4, password);
            preparedStatement.setString(5, dob);

            int result = preparedStatement.executeUpdate();

            if (result > 0) {
                messageLabel.setText("Registration successful!");
            } else {
                messageLabel.setText("Registration failed, try again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            messageLabel.setText("SQL Error: " + e.getMessage());
        }
    }

    @FXML
    private void goToLoginPage(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login Page");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Failed to load login page.");
        }
    }
}
