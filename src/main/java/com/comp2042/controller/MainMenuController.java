package com.comp2042.controller;

import com.comp2042.Main;
import com.comp2042.model.Coin;
import com.comp2042.model.Game;
import com.comp2042.model.LevelMap;
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

/**
 * Handles main-menu actions and assembles new gameplay sessions.
 */
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

    // Hold the selectable preset level maps.
    // Classic remains the default choice.
    @FXML
    private RadioButton classicLevelButton;
    @FXML
    private RadioButton pillarsLevelButton;
    @FXML
    private RadioButton arenaLevelButton;

    /**
     * Creates a menu controller bound to the application's shared view manager.
     */
    public MainMenuController() {
        viewManager = Main.getViewManager();
    }

    /**
     * Initializes a new game session from the selected speed tier and level map, then displays the game window.
     */
    public void handleStart() {
        viewManager.initGameStage();
        Stage gameStage = viewManager.getGameStage();
        Scene gameScene = launchSnake(gameStage);
        gameStage.setScene(gameScene);
        viewManager.getPrimaryStage().close();
        gameStage.show();
    }

    // Builds the game collaborators.
    // Loads preset level obstacles before creating the initial coin.
    private Scene launchSnake(Stage gameStage) {
        // Resolve menu selections before building the model.
        // Keep tier- and level-dependent rules inside Game and Obstacle.
        SpeedTier selectedTier = getSelectedSpeedTier();
        LevelMap selectedLevel = getSelectedLevelMap();
        Game game = new Game(
                X_VALUE,
                Y_VALUE,
                selectedTier,
                POWER_UP,
                selectedLevel
        );
        Obstacle obstacle = new Obstacle(
                game.getWidth(),
                game.getHeight()
        );
        obstacle.applyLevelMap(game.getLevelMap());
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
                game.getHeight() * game.getMultiplier()
                        + 40
                        + gameView.getHudReservedHeight()
        );
        // Capture shortcut presses.
        // Filter held-key repeats in the controller.
        gameScene.setOnKeyPressed(controller::handle);
        // Track every key release.
        // Restore shortcut availability.
        gameScene.setOnKeyReleased(controller::handleKeyReleased);
        // Stop the gameplay timeline when the game window is closed or hidden.
        gameStage.setOnHidden(e -> controller.stop());
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

    // Map the menu selection to a preset level map.
    // Missing choices safely default to classic.
    private LevelMap getSelectedLevelMap() {
        if (pillarsLevelButton != null && pillarsLevelButton.isSelected()) {
            return LevelMap.PILLARS;
        }
        if (arenaLevelButton != null && arenaLevelButton.isSelected()) {
            return LevelMap.ARENA;
        }
        if (classicLevelButton != null && classicLevelButton.isSelected()) {
            return LevelMap.CLASSIC;
        }
        return LevelMap.CLASSIC;
    }

    /**
     * Returns the view manager used to coordinate menu and game stages.
     *
     * @return shared {@link ViewManager} instance
     */
    public ViewManager getViewManager() {
        return viewManager;
    }

    /**
     * Returns the default board width in grid cells.
     *
     * @return board width in cells
     */
    public static int getXValue() {
        return X_VALUE;
    }

    /**
     * Returns the default board height in grid cells.
     *
     * @return board height in cells
     */
    public static int getYValue() {
        return Y_VALUE;
    }
}
