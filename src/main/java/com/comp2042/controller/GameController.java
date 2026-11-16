package com.comp2042.controller;

import com.comp2042.model.Coin;
import com.comp2042.model.CoinFactory;
import com.comp2042.model.Game;
import com.comp2042.model.GameState;
import com.comp2042.model.Obstacle;
import com.comp2042.model.Snake;
import com.comp2042.view.GameView;
import com.comp2042.view.ViewManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashSet;
import java.util.Set;

// Coordinates input, gameplay timing, and view updates.
// Delegates coin effects to model polymorphism.
public class GameController {
    private final Game game;
    private final GameView gameView;
    private final Obstacle obstacle;
    private final Stage stage;
    private final ViewManager viewManager;
    private final CoinFactory coinFactory;
    private final Timeline animation;
    // Track held shortcut keys.
    // Ignore operating-system key repeats.
    private final Set<KeyCode> pressedKeys;

    private Snake snek;
    private Coin coin;
    private KeyCode key;
    private KeyCode lastKey;
    private boolean keyPressed;

    // Wires all gameplay collaborators through explicit dependencies.
    public GameController(
            Snake snek,
            Coin coin,
            Game game,
            GameView gameView,
            Obstacle obstacle,
            Stage stage,
            ViewManager viewManager
    ) {
        this.snek = snek;
        this.coin = coin;
        this.game = game;
        this.gameView = gameView;
        this.obstacle = obstacle;
        this.stage = stage;
        this.viewManager = viewManager;
        coinFactory = new CoinFactory();
        key = KeyCode.LEFT;
        lastKey = KeyCode.LEFT;
        // Initialise held-key state.
        // Start with every shortcut available.
        pressedKeys = new HashSet<>();
        animation = buildAnimation();
        animation.setCycleCount(Animation.INDEFINITE);
        animation.setRate(game.getCurrentSpeed());
    }

    // Creates the fixed-rate gameplay loop.
    private Timeline buildAnimation() {
        return new Timeline(new KeyFrame(
                Duration.seconds(1),
                event -> runTick()
        ));
    }

    // Advances one gameplay frame.
    private void runTick() {
        if (isFinished()) {
            animation.stop();
            return;
        }

        moveSnake();
        if (snek.getDead()) {
            game.setState(GameState.GAME_OVER);
            renderFrame();
            animation.stop();
            return;
        }

        if (snek.ateCoin(coin)) {
            applyCoinEffect();
            game.incScore();

            if (game.gameWon()) {
                game.setState(GameState.WON);
                renderFrame();
                animation.stop();
                return;
            }
            coin = coinFactory.createCoin(game, snek, obstacle);
        }

        keyPressed = false;
        renderFrame();
    }

    // Maps the current key to a model movement command.
    private void moveSnake() {
        if (key == KeyCode.LEFT) {
            snek.move('L');
        } else if (key == KeyCode.RIGHT) {
            snek.move('R');
        } else if (key == KeyCode.UP) {
            snek.move('U');
        } else if (key == KeyCode.DOWN) {
            snek.move('D');
        }
    }

    // Resets temporary powers before applying the new coin.
    private void applyCoinEffect() {
        game.resetCurrentSpeed();
        snek.ignoreBorders(false);
        coin.applyEffect(game, snek, obstacle);
        animation.setRate(game.getCurrentSpeed());
    }

    // Redraws all board and overlay state.
    private void renderFrame() {
        gameView.drawGrid(coin, snek, obstacle);
        gameView.gameWon();
        gameView.gameOver(snek);
        gameView.updateScoreLabel();
    }

    // Reports whether the current session is over.
    private boolean isFinished() {
        return game.getState() == GameState.GAME_OVER
                || game.getState() == GameState.WON
                || snek.getDead()
                || game.gameWon();
    }

    // Routes keyboard commands to controller behavior.
    // Filter automatic repeats for stateful shortcuts.
    public void handle(KeyEvent event) {
        KeyCode eventCode = event.getCode();

        // Detect stateful shortcuts.
        // Ignore their automatic repeats.
        boolean shortcutKey = eventCode == KeyCode.P
                || eventCode == KeyCode.TAB
                || eventCode == KeyCode.ESCAPE;
        if (shortcutKey && !pressedKeys.add(eventCode)) {
            return;
        }

        if (eventCode.isArrowKey()) {
            handleDirection(eventCode);
        } else if (eventCode == KeyCode.P) {
            togglePause();
        } else if (eventCode == KeyCode.TAB) {
            reset();
        // Accept every plus key variant.
        // Cover main and keypad input.
        } else if (eventCode == KeyCode.PLUS
                || eventCode == KeyCode.EQUALS
                || eventCode == KeyCode.ADD) {
            setFullscreen(true);
        // Accept every minus key variant.
        // Cover main and keypad input.
        } else if (eventCode == KeyCode.MINUS
                || eventCode == KeyCode.SUBTRACT) {
            setFullscreen(false);
        } else if (eventCode == KeyCode.ESCAPE) {
            leaveGame();
        }
    }

    // Clears held shortcut keys.
    // Re-arms shortcuts after release.
    public void handleKeyReleased(KeyEvent event) {
        pressedKeys.remove(event.getCode());
    }

    // Accepts valid directions while a game can run.
    private void handleDirection(KeyCode eventCode) {
        // Ignore repeats of the active direction.
        // Preserve the next turn opportunity.
        if (!canRun()
                || !valKey(eventCode)
                || keyPressed
                || eventCode.equals(key)) {
            return;
        }

        lastKey = key;
        key = eventCode;
        keyPressed = true;
        game.setState(GameState.RUNNING);
        gameView.turnDir(snek, lastKey, key);
        animation.play();
    }

    // Toggles between paused and running states.
    private void togglePause() {
        if (game.getState() == GameState.RUNNING) {
            game.setState(GameState.PAUSED);
            animation.stop();
        } else if (game.getState() == GameState.PAUSED) {
            game.setState(GameState.RUNNING);
            animation.play();
        }
    }

    // Reports whether direction input is currently allowed.
    private boolean canRun() {
        return !snek.getDead()
                && !game.gameWon()
                && (game.getState() == GameState.READY
                || game.getState() == GameState.RUNNING);
    }

    // Rejects direct reversals on the same axis.
    private boolean valKey(KeyCode nextKey) {
        return switch (nextKey) {
            case LEFT -> key != KeyCode.RIGHT;
            case RIGHT -> key != KeyCode.LEFT;
            case UP -> key != KeyCode.DOWN;
            case DOWN -> key != KeyCode.UP;
            default -> false;
        };
    }

    // Enters or leaves fullscreen without changing the run.
    private void setFullscreen(boolean fullScreen) {
        gameView.scaleToFullscreen(
                fullScreen,
                stage,
                obstacle,
                snek,
                coin
        );
    }

    // Returns to the menu or leaves fullscreen first.
    private void leaveGame() {
        if (stage.isFullScreen()) {
            setFullscreen(false);
            return;
        }

        animation.stop();
        viewManager.getPrimaryStage().show();
        viewManager.getGameStage().close();
    }

    // Starts a clean run in the same stage.
    private void reset() {
        animation.jumpTo(Duration.ZERO);
        animation.stop();
        game.resetScore();
        game.resetCurrentSpeed();
        obstacle.reset();

        snek = new Snake(
                game.getWidth(),
                game.getHeight(),
                game.isNoBorder(),
                obstacle
        );
        coin = new Coin(
                game.getWidth(),
                game.getHeight(),
                snek,
                obstacle
        );

        key = KeyCode.LEFT;
        lastKey = KeyCode.LEFT;
        keyPressed = false;
        animation.setRate(game.getCurrentSpeed());
        gameView.resetTurns();
        renderFrame();
    }
}
