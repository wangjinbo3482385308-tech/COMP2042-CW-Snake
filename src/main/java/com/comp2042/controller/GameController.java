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

        // Advance active powers using real gameplay time.
        // A stopped timeline freezes this countdown.
        double deltaSeconds = 1.0 / Math.max(
                0.1,
                animation.getRate()
        );
        game.getInventory().tickMagnet(deltaSeconds);

        moveSnake();
        if (snek.getDead()) {
            // Spend a held revive stone before ending the run.
            // Restore the snake at the safe board center.
            if (game.getInventory().consumeReviveStone()) {
                snek.reviveAtCenter(
                        game.getWidth(),
                        game.getHeight(),
                        obstacle
                );
                // Resume with the default leftward direction.
                // Clear the held direction state as well.
                key = KeyCode.LEFT;
                lastKey = KeyCode.LEFT;
                keyPressed = false;
                // Remove turn markers from the previous body.
                // Keep the revived snake visually clean.
                gameView.resetTurns();
            } else {
                game.setState(GameState.GAME_OVER);
                renderFrame();
                animation.stop();
                return;
            }
        }

        // Collect by direct contact or active magnet range.
        // Both paths share scoring and growth behavior.
        if (snek.collectCoin(coin, game.getInventory())) {
            powerCoin();
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
    // Shield protection participates in every movement.
    private void moveSnake() {
        if (key == KeyCode.LEFT) {
            snek.move('L', game.getInventory());
        } else if (key == KeyCode.RIGHT) {
            snek.move('R', game.getInventory());
        } else if (key == KeyCode.UP) {
            snek.move('U', game.getInventory());
        } else if (key == KeyCode.DOWN) {
            snek.move('D', game.getInventory());
        }
    }

    // Resets temporary powers before applying the new coin.
    // Magnet collection uses the same power pipeline.
    private void powerCoin() {
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

        // Detect stateful and shop shortcuts.
        // Ignore their automatic repeats.
        boolean shortcutKey = eventCode == KeyCode.P
                || eventCode == KeyCode.TAB
                || eventCode == KeyCode.ESCAPE
                || isShopShortcut(eventCode);
        if (shortcutKey && !pressedKeys.add(eventCode)) {
            return;
        }

        if (eventCode.isArrowKey()) {
            handleDirection(eventCode);
        } else if (isShopShortcut(eventCode)) {
            handleShopPurchase(eventCode);
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

    // Reports whether a key buys a shop item.
    // Supports main and keypad number rows.
    private boolean isShopShortcut(KeyCode eventCode) {
        return eventCode == KeyCode.DIGIT1
                || eventCode == KeyCode.NUMPAD1
                || eventCode == KeyCode.DIGIT2
                || eventCode == KeyCode.NUMPAD2
                || eventCode == KeyCode.DIGIT3
                || eventCode == KeyCode.NUMPAD3;
    }

    // Buys the item selected by a number key.
    // Reject purchases after the run has ended.
    private void handleShopPurchase(KeyCode eventCode) {
        if (snek.getDead() || game.gameWon()) {
            return;
        }

        boolean purchased;
        if (eventCode == KeyCode.DIGIT1 || eventCode == KeyCode.NUMPAD1) {
            purchased = game.getInventory().buyShield();
        } else if (eventCode == KeyCode.DIGIT2
                || eventCode == KeyCode.NUMPAD2) {
            purchased = game.getInventory().buyMagnet();
        } else {
            purchased = game.getInventory().buyReviveStone();
        }

        // Refresh the board immediately after a purchase.
        // Update the HUD with the new stock and points.
        if (purchased) {
            gameView.drawGrid(coin, snek, obstacle);
            gameView.updateScoreLabel();
        }
    }

    // Clears held shortcut keys.
    // Re-arms shop and session shortcuts after release.
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
