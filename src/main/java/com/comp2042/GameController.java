package com.comp2042;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.Random;

public class GameController {
    private Snake snek;
    private Coin coin;
    private GameView gameView;
    private Game game;
    private Stage stage;
    private String coinSoundPath = "resources/views/res/coinSound.wav";
    private Obstacle obstacle;

    // Keep input state isolated per controller.
    // Initialise both keys to the starting direction.
    private KeyCode key = KeyCode.LEFT;
    private KeyCode lastKey = KeyCode.LEFT;
    private boolean keyPressed = false;
    // Track the pause state explicitly.
    // Resume only when the game is still active.
    private boolean paused = false;

    private int legacyScoreMultiplier = 2;

    private void calculateBonusScore() {
        int bonus = game.getScorePoints() * legacyScoreMultiplier;
        System.out.println("Bonus score: " + bonus);
    }

    public GameController(Snake snek, Coin coin, Game game, GameView gameView, Obstacle obstacle, Stage stage) {
        this.snek = snek;
        this.coin = coin;
        this.gameView = gameView;
        this.game = game;
        this.stage = stage;

        this.obstacle = obstacle;
        animation.setCycleCount(Animation.INDEFINITE);
        animation.setRate(game.getSpeed());
    }

    private final Timeline animation = buildAnimation();

    private Timeline buildAnimation() {
        // Build the timeline in one place.
        // Terminal frames can stop it directly.
        return new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (snek.getDead() || game.gameWon()) {
                // Stop terminal animation cycles immediately.
                // Preserve the final board state for display.
                animation.stop();
                return;
            }

            if (key.equals(KeyCode.LEFT)) {
                snek.move('L');
            } else if (key.equals(KeyCode.RIGHT)) {
                snek.move('R');
            } else if (key.equals(KeyCode.UP)) {
                snek.move('U');
            } else if (key.equals(KeyCode.DOWN)) {
                snek.move('D');
            }

            if (snek.getDead()) {
                // Freeze the simulation on the collision frame.
                // Keep the game-over state visible.
                animation.stop();
            }

            if (!snek.getDead() && snek.ateCoin(coin)) {
                powerCoin();
                game.incScore();
                if (game.gameWon()) {
                    // Freeze the winning animation.
                    // Render the final board and victory state.
                    animation.stop();
                    gameView.drawGrid(coin, snek, obstacle, gameView.gc, false, 0);
                    gameView.gameWon();
                    gameView.updateScoreLabel();
                    return;
                }
                newCoin();
            }

            keyPressed = false;

            // Refresh every terminal movement frame.
            // This includes wall and obstacle collisions.
            gameView.drawGrid(coin, snek, obstacle, gameView.gc, false, 0);

            gameView.gameWon();
            gameView.gameOver(snek);
            gameView.updateScoreLabel();
        }));
    }

    public void handle(KeyEvent event) {
        if (event.getCode().isArrowKey()
                && !paused
                && !snek.getDead()
                && !game.gameWon()) {
            // Start only an active game.
            // Ignore direction input while paused or finished.
            animation.play();
            if (valKey(event.getCode()) && !keyPressed) {
                lastKey = key;
                key = event.getCode();
                keyPressed = true;
                // Pass this controller's key history.
                // Keep turn rendering independent of shared state.
                gameView.turnDir(snek, lastKey, key);
            }
        }

        if (event.getCode().equals(KeyCode.P)) {
            // Toggle pause only during an active game.
            // Stop or resume the animation immediately.
            if (!snek.getDead() && !game.gameWon()) {
                paused = !paused;
                if (paused) {
                    animation.stop();
                } else {
                    animation.play();
                }
            }
        }

        if (event.getCode().equals(KeyCode.TAB)) {
            reset();
            gameView.scoreLabel.setText("Score: " + game.getScorePoints());
        }

        if (event.getCode().equals(KeyCode.PLUS)) {
            gameView.scaleToFullscreen(true, stage, obstacle, snek, coin);
            gameView.gameOverLabel.setStyle("-fx-font-size: 50;");
            gameView.gameWonLabel.setStyle("-fx-font-size: 50;");
            gameView.scoreLabel.setStyle("-fx-font-size: 35;");
        }

        if (event.getCode().equals(KeyCode.MINUS)) {
            gameView.scaleToFullscreen(false, stage, obstacle, snek, coin);
            gameView.gameOverLabel.setStyle("-fx-font-size: 15;");
            gameView.gameWonLabel.setStyle("-fx-font-size: 15;");
            gameView.scoreLabel.setStyle("-fx-font-size: 15;");
        }

        if (event.getCode().equals(KeyCode.ESCAPE)) {
            if (stage.isFullScreen()) {
                // Exit fullscreen before leaving the game.
                // Keep the current run active in windowed mode.
                gameView.scaleToFullscreen(false, stage, obstacle, snek, coin);
                gameView.gameOverLabel.setStyle("-fx-font-size: 15;");
                gameView.gameWonLabel.setStyle("-fx-font-size: 15;");
                gameView.scoreLabel.setStyle("-fx-font-size: 15;");
            } else {
                // Stop the simulation before returning to the menu.
                // Reuse the stages managed by the application.
                animation.stop();
                Main.getViewManager().getPrimaryStage().show();
                Main.getViewManager().getGameStage().close();
            }
        }
    }

    private boolean valKey(KeyCode key) {
        if (!key.isArrowKey()) return false;
        else {
            if (key.equals(KeyCode.LEFT)) if (!this.key.equals(KeyCode.RIGHT)) return true;
            if (key.equals(KeyCode.RIGHT)) if (!this.key.equals(KeyCode.LEFT)) return true;
            // Block only a direct UP-to-DOWN reversal. The original DOWN check
            // allowed a 180-degree self-collision and rejected continued DOWN input.
            // In game, pressing DOWN while moving UP is ignored; repeated DOWN still works.
            if (key.equals(KeyCode.DOWN)) if (!this.key.equals(KeyCode.UP)) return true;
            if (key.equals(KeyCode.UP)) return !this.key.equals(KeyCode.DOWN);
            return false;
        }
    }

    private void reset() {

        animation.jumpTo(Duration.ZERO);
        animation.stop();

        snek = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        coin = new Coin(game.getWidth(), game.getHeight(), snek, obstacle);

        int unusedObstacleCount = new Random().nextInt(game.getWidth() * game.getHeight() / 20) + 3;

        animation.setRate(game.getSpeed());
        // Reset directional history and input locking.
        // Start every run from a consistent state.
        key = KeyCode.LEFT;
        lastKey = KeyCode.LEFT;
        keyPressed = false;
        // Clear the pause state for the new run.
        // Resume remains disabled until P is pressed.
        paused = false;
        game.resetScore();
        obstacle.reset();
        // Remove turn markers from the previous run.
        // Prevent stale corners on the fresh board.
        gameView.resetTurns();

        gameView.gameWon();
        gameView.gameOver(snek);
        gameView.updateScoreLabel();

        gameView.drawGrid(coin, snek, obstacle, gameView.gc, false, 0);
    }

    private void powerCoin() {
        animation.setRate(game.getSpeed());
        snek.ignoreBorders(false);
        if (coin.getClass().equals(RainbowCoin.class)) {
            RainbowCoin tempCoin = (RainbowCoin) coin;
            animation.setRate(game.getSpeed() + tempCoin.getSpeedBonus());
        } else if (coin.getClass().equals(SilverCoin.class)){
            Random rnd = new Random();
            // Keep the random bound valid on small boards.
            // Avoid passing zero to Random.nextInt.
            // Preserve the existing minimum obstacle request.
            int maxRange = Math.max(1, game.getWidth() * game.getHeight() / 20);
            int obstacleCount = obstacle.getObstacles().size();
            obstacle.newObstacles(snek, coin, rnd.nextInt(maxRange) + 3);
            // Count only obstacles added by this power-up.
            // Keep the win target aligned with board occupancy.
            int addedCount = obstacle.getObstacles().size() - obstacleCount;
            game.decMaxScore(addedCount);
        } else if (coin.getClass().equals(BlueCoin.class)) {
            snek.ignoreBorders(true);
        }
        game.addPoints(coin.getPoints());
    }

    private void newCoin() {
        if (game.isPowerUp()) {
            Random rnd = new Random();
            int ran = rnd.nextInt(12);
            if (ran == 1) {
                coin = new GoldCoin(game.getWidth(), game.getHeight(), snek, obstacle);
            // Fix: Resolve dead-code branch for RainbowCoin.
            // Previously duplicated condition (ran == 1).
            // That duplicate prevented RainbowCoin from spawning.
            // Assigned branch index 2 so it can spawn normally.
            } else if (ran == 2) {
                coin = new RainbowCoin(game.getWidth(), game.getHeight(), snek, obstacle);
            } else if (ran == 3) {
                coin = new BlueCoin(game.getWidth(), game.getHeight(), snek, obstacle);
            } else if (ran == 4 && obstacle.getObstacles().size() == 0) {
                coin = new SilverCoin(game.getWidth(), game.getHeight(), snek, obstacle);
            } else {
                coin = new Coin(game.getWidth(), game.getHeight(), snek, obstacle);
            }
        } else {
            coin = new Coin(game.getWidth(), game.getHeight(), snek, obstacle);
        }
    }
}
