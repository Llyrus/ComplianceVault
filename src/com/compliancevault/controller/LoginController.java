package com.compliancevault.controller;

import com.compliancevault.ComplianceVaultApp;
import com.compliancevault.model.User;
import com.compliancevault.service.AuthService;
import com.compliancevault.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final AuthService authService = ComplianceVaultApp.getAuthService();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        errorLabel.setText("");

        try {
            boolean success = authService.login(username, password);

            if (success) {
                User user = authService.getCurrentUser();
                System.out.println("Logged in as: " + user.getUsername()
                        + " (" + user.getRole() + ")");
                try {
                    SceneManager.switchScene(loginButton,
                            "/com/compliancevault/view/dashboard.fxml",
                            "ComplianceVault — Dashboard", 900, 600);
                } catch (Exception e) {
                    showError("Could not open dashboard.");
                    e.printStackTrace();
                }
            } else {
                showError("Invalid username or password.");
            }
        } catch (SQLException e) {
            showError("Could not reach the database. Please try again.");
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        errorLabel.setStyle("-fx-text-fill: #c62828; -fx-font-size: 12px;");
        errorLabel.setText(msg);
    }

    private void showSuccess(String msg) {
        errorLabel.setStyle("-fx-text-fill: #1D9E75; -fx-font-size: 12px;");
        errorLabel.setText(msg);
    }
}