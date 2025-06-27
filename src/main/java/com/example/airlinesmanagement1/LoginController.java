package com.example.airlinesmanagement1;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML private Button registerButton; // renamed from cancelButton to registerButton
    @FXML private Button loginButton;
    @FXML private Label loginMessageLabel;
    @FXML private ImageView brandingImageView;
    @FXML private ImageView lockImageView;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        brandingImageView.setImage(
                new Image(new File("Images/Mainlogo.png").toURI().toString()));
        lockImageView.setImage(
                new Image(new File("Images/Keylogo.png").toURI().toString()));
    }

    // Handles login action
    public void loginButtonOnAction(ActionEvent event) {
        if (usernameField.getText().isBlank() || passwordField.getText().isBlank()) {
            loginMessageLabel.setText("Please enter username and password.");
            return;
        }
        validateLogin(event);
    }

    // NEW: Opens the Register.fxml page when clicked on "Register"
    public void goToRegisterPage(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Register.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Register Page");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            loginMessageLabel.setText("Failed to open registration page.");
        }
    }

    // Validates the login credentials
    private void validateLogin(ActionEvent event) {
        Connection conn;
        try {
            conn = new DatabaseConnection().getConnection();
        } catch (Exception ex) {
            loginMessageLabel.setText("Cannot connect to database.");
            ex.printStackTrace();
            return;
        }

        String sql = "SELECT COUNT(1) FROM user_account WHERE user_name = ? AND password = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usernameField.getText());
            ps.setString(2, passwordField.getText());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 1) {
                    loginMessageLabel.setText("Congratulations! Login successful.");
                    Session.getInstance().setUsername(usernameField.getText()); // Use Session instead of CurrentUser
                    loadDashboard(event);
                } else {
                    loginMessageLabel.setText("Invalid login. Please try again.");
                }
            }
        } catch (Exception ex) {
            loginMessageLabel.setText("Error during login.");
            ex.printStackTrace();
        } finally {
            try { conn.close(); } catch (Exception ignored) {}
        }
    }

    // Loads the dashboard page after a successful login
    private void loadDashboard(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Dashboard.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            loginMessageLabel.setText("Failed to load dashboard.");
        }
    }
}