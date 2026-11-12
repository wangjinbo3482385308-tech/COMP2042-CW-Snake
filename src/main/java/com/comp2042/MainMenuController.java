package com.comp2042;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainMenuController {

    private static final int X_VALUE = 20;
    private static final int Y_VALUE = 20;
    private static final boolean POWER_UP = true;
    private static final char DIFFICULTY = 'N';

    private Scene gameScene;

    private ViewManager viewManager = Main.getViewManager();

    public void handleStart() {
        viewManager.initGameStage();

        Stage gameStage = viewManager.getGameStage();
        launchSnake(gameStage);
        gameStage.setScene(gameScene);
        viewManager.getPrimaryStage().close();
        gameStage.show();
    }

    private void launchSnake(Stage gameStage) {
        Game game = new Game(X_VALUE, Y_VALUE, POWER_UP, DIFFICULTY);
        Obstacle obstacle = new Obstacle(game.getWidth(), game.getHeight());
        Snake snek = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        Coin coin = new Coin(game.getWidth(), game.getHeight(), snek, obstacle);

        GameView gameView = new GameView(game);
        GameController controller = new GameController(snek, coin, game, gameView, obstacle, gameStage);

        StackPane gamePane = new StackPane();
        // Reserve room for the twenty-pixel padding on every side.
        // Keep the scene large enough to contain the full board canvas.
        gameScene = new Scene(
                gamePane,
                game.getWidth() * game.getMultiplier() + 40,
                game.getHeight() * game.getMultiplier() + 40
        );

        gameScene.setOnKeyPressed(controller::handle);

        gameView.makeScene(gameScene, snek, coin);
        gamePane.getChildren().addAll(gameView.gridCanvas, gameView.gameOverLabel, gameView.gameWonLabel, gameView.scoreLabel);
        // Apply the same padding used to size the scene.
        // Prevent the canvas edges from being clipped.
        gamePane.setPadding(new Insets(20));
        gamePane.setAlignment(gameView.gameOverLabel, Pos.TOP_CENTER);
        gamePane.setAlignment(gameView.gameWonLabel, Pos.TOP_CENTER);
        gamePane.setAlignment(gameView.scoreLabel, Pos.BOTTOM_CENTER);
    }

    public ViewManager getViewManager() {
        return viewManager;
    }

    public static int getxValue() {
        return X_VALUE;
    }

    public static int getyValue() {
        return Y_VALUE;
    }
}
