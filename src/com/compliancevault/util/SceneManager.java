package com.compliancevault.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
centralises scene switching so controllers don't each reinvent it
 */
//!!Post-uni note!! this is a procedural helper. A more structured approach
// would be a Router class with named routes ("login", "dashboard", "supplier-detail")
// and history support. Overkill for the current screen count lol
public class SceneManager {

    /**
    Replace the scene on the Stage that owns the given node
     Useful when called from inside an event handler, pass any @FXML node
     and it'll find the Stage automatically
    */
    public static void switchScene(Node sourceNode, String fxmlPath, String title,
                                   double width, double height) throws IOException {
        Stage stage = (Stage) sourceNode.getScene().getWindow();
        Parent root = FXMLLoader.load(SceneManager.class.getResource(fxmlPath));
        stage.setTitle(title);
        stage.setScene(new Scene(root, width, height));
        stage.centerOnScreen();
    }
}