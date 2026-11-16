package com.comp2042.controller;

import com.comp2042.Main;
import com.comp2042.model.Coin;
import com.comp2042.model.Game;
import com.comp2042.model.Obstacle;
import com.comp2042.model.Snake;
import com.comp2042.view.GameView;
import com.comp2042.view.ViewManager;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

// Handles main-menu actions and game-session assembly.
public class MainMenuController {
    private static final int X_VALUE = 20;
    private static final int Y_VALUE = 20;
    private static final boolean POWER_UP = true;
    private static final char DIFFICULTY = 'N';

    private final ViewManager viewManager;

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

    // Builds model, view, and controller collaborators.
    private Scene launchSnake(Stage gameStage) {
        Game game = new Game(X_VALUE, Y_VALUE, POWER_UP, DIFFICULTY);
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
