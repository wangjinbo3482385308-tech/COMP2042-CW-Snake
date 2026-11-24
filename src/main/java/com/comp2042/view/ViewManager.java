package com.comp2042.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Coordinates the primary menu window, FXML menu loading, and active game stage lifecycle.
 */
public class ViewManager {
    private final Stage primaryStage;
    private AnchorPane root;
    private Stage gameStage;

    /**
     * Creates a view manager reusing the JavaFX application's primary stage.
     *
     * @param primaryStage primary application window
     */
    public ViewManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Creates a standalone view manager backed by a newly constructed primary stage.
     */
    public ViewManager() {
        this(new Stage());
    }

    /**
     * Loads the main menu layout from FXML and displays it in the primary stage.
     *
     * @throws IllegalStateException if {@code MainMenu.fxml} cannot be located or loaded
     */
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

    /**
     * Creates and configures a new game stage with default fullscreen Escape exit disabled.
     */
    public void initGameStage() {
        gameStage = new Stage();
        gameStage.setTitle("Snake Game");
        gameStage.getIcons().add(AssetManager.getSnakeLogo());
        gameStage.setResizable(false);
        gameStage.setFullScreenExitKeyCombination(
                KeyCombination.NO_MATCH
        );
    }

    /**
     * Returns the primary application stage hosting the main menu.
     *
     * @return primary {@link Stage}
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Returns the most recently initialized gameplay window stage.
     *
     * @return game {@link Stage}, or {@code null} before {@link #initGameStage()} is called
     */
    public Stage getGameStage() {
        return gameStage;
    }
}
