package com.comp2042;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class ViewManager {

    private final Stage primaryStage;
    private AnchorPane root;

    private Stage gameStage;

    private Image snakeLogo = new Image("resources/views/res/SnakeLogoWhite.png");

    public ViewManager(Stage primaryStage) {
        // Reuse the stage supplied by the JavaFX runtime.
        // Avoid creating an extra unmanaged primary window.
        this.primaryStage = primaryStage;
    }

    public ViewManager() {
        // Keep the old constructor for compatibility.
        // Use a fallback stage only when no caller provides one.
        this(new Stage());
    }

    public void initalizeMainMenu() {
        // Resolve the menu from the classpath.
        // Fail before creating a scene without content.
        URL menuLocation = Main.class.getResource("/resources/views/MainMenu.fxml");
        if (menuLocation == null) {
            throw new IllegalStateException("Failed to load MainMenu.fxml");
        }

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(menuLocation);
            root = loader.load();
        } catch (IOException exception) {
            // Preserve the loader failure as the cause.
            // Stop before the scene can receive a null root.
            throw new IllegalStateException("Failed to load MainMenu.fxml", exception);
        }

        if (root == null) {
            // Reject an empty loader result.
            // Keep the menu failure message consistent.
            throw new IllegalStateException("Failed to load MainMenu.fxml");
        }

        Scene mainScene = new Scene(root);

        primaryStage.setScene(mainScene);
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(snakeLogo);
        primaryStage.setTitle("Snake Game");
        primaryStage.show();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void initGameStage() {
        gameStage = new Stage();
        gameStage.setTitle("Snake Game");
        gameStage.getIcons().add(snakeLogo);
        gameStage.setResizable(false);
    }

    public Stage getGameStage() {
        return gameStage;
    }
}
