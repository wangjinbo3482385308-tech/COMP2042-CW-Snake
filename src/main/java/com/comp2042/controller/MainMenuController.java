package com.comp2042.controller;

import com.comp2042.Main;
import com.comp2042.model.Coin;
import com.comp2042.model.Game;
import com.comp2042.model.Obstacle;
import com.comp2042.model.Snake;
import com.comp2042.model.SpeedTier;
import com.comp2042.view.GameView;
import com.comp2042.view.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

// Handles main-menu actions and game-session assembly.
public class MainMenuController {
    private static final int X_VALUE = 20;
    private static final int Y_VALUE = 20;
    private static final boolean POWER_UP = true;

    private final ViewManager viewManager;

    // Hold the selectable speed modes.
    // Normal remains the default choice.
    @FXML
    private RadioButton slowSpeedButton;
    @FXML
    private RadioButton normalSpeedButton;
    @FXML
    private RadioButton fastSpeedButton;

    public MainMenuController() {
        viewManager = Main.getViewManager();
    }

    // Creates and displays a fresh game session.
    public void handleStart() {
        viewManager.initGameStage();
        Stage gameStage = viewManager.getGameStage();
        Scene gameScene = launchSnake(gameStage);
        gameStage.setScene(gameScene);
        viewManager.getPrimaryStage().close();
        gameStage.show();
    }

    // Builds the game collaborators.
    // Passes the selected tier to the model.
    private Scene launchSnake(Stage gameStage) {
        // Resolve the choice before building the model.
        // Keep tier-dependent rules inside Game.
        SpeedTier selectedTier = getSelectedSpeedTier();
        Game game = new Game(
                X_VALUE,
                Y_VALUE,
                selectedTier,
                POWER_UP
        );
        Obstacle obstacle = new Obstacle(
                game.getWidth(),
                game.getHeight()
        );
        Snake snake = new Snake(
                game.getWidth(),
                game.getHeight(),
                game.isNoBorder(),
                obstacle
        );
        Coin coin = new Coin(
                game.getWidth(),
                game.getHeight(),
                snake,
                obstacle
        );

        GameView gameView = new GameView(game);
        GameController controller = new GameController(
                snake,
                coin,
                game,
                gameView,
                obstacle,
                gameStage,
                viewManager
        );

        StackPane gamePane = new StackPane();
        Scene gameScene = new Scene(
                gamePane,
                game.getWidth() * game.getMultiplier() + 40,
                game.getHeight() * game.getMultiplier() + 40
        );
        // Capture shortcut presses.
        // Filter held-key repeats in the controller.
        gameScene.setOnKeyPressed(controller::handle);
        // Track every key release.
        // Restore shortcut availability.
        gameScene.setOnKeyReleased(controller::handleKeyReleased);
        gameView.makeScene(snake, coin, obstacle);
        gameView.layoutGamePane(gamePane);
        return gameScene;
    }

    // Map the menu selection to a speed tier.
    // Missing choices safely use normal.
    private SpeedTier getSelectedSpeedTier() {
        if (slowSpeedButton != null && slowSpeedButton.isSelected()) {
            return SpeedTier.SLOW;
        }
        if (normalSpeedButton != null && normalSpeedButton.isSelected()) {
            return SpeedTier.NORMAL;
        }
        if (fastSpeedButton != null && fastSpeedButton.isSelected()) {
            return SpeedTier.FAST;
        }
        return SpeedTier.NORMAL;
    }

    public ViewManager getViewManager() {
        return viewManager;
    }

    public static int getXValue() {
        return X_VALUE;
    }

    public static int getYValue() {
        return Y_VALUE;
    }
}
