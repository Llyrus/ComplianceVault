package com.compliancevault;

import com.compliancevault.database.DatabaseManager;
import com.compliancevault.service.AuthService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ComplianceVaultApp extends Application {

    // Single shared AuthService
    //  holds session state across screens
    private static final AuthService authService = new AuthService();

    public static AuthService getAuthService() {
        return authService;
    }

    @Override
    public void init() {
        // Runs before start()
        // good place for DB setup
        DatabaseManager.initialiseDatabase();
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(
                getClass().getResource("/com/compliancevault/view/login.fxml")
        );

        stage.setTitle("ComplianceVault");
        stage.setScene(new Scene(root, 400, 360));
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}