package com.comp2042.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

// Coordinates application windows and menu loading.
public class ViewManager {
    private final Stage primaryStage;
    private AnchorPane root;
    private Stage gameStage;

    // Reuses the JavaFX application's primary stage.
    public ViewManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    // Provides a standalone view manager if needed.
    public ViewManager() {
        this(new Stage());
    }

    // Loads and displays the main menu from FXML.
    public void initializeMainMenu() {
        URL menuLocation = ViewManager.class.getResource(
                "/resources/views/MainMenu.fxml"
        );
        if (menuLocation == null) {
            throw new IllegalStateException("Failed to load MainMenu.fxml");
        }

        try {
            FXMLLoader loader = new FXMLLoader(menuLocation);
            root = loader.load();
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to load MainMenu.fxml",
                    exception
            );
        }

        if (root == null) {
            throw new IllegalStateException("Failed to load MainMenu.fxml");
        }

        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(AssetManager.getSnakeLogo());
        primaryStage.setTitle("Snake Game");
        primaryStage.show();
    }

    // Creates the stage used by one game session.
    public void initGameStage() {
        gameStage = new Stage();
        gameStage.setTitle("Snake Game");
        gameStage.getIcons().add(AssetManager.getSnakeLogo());
        gameStage.setResizable(false);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Stage getGameStage() {
        return gameStage;
    }
}
