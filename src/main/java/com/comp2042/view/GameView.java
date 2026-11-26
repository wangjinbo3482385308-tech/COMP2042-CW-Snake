package com.comp2042.view;

import com.comp2042.model.Coin;
import com.comp2042.model.Game;
import com.comp2042.model.GameState;
import com.comp2042.model.Obstacle;
import com.comp2042.model.Snake;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.awt.Point;
import java.util.List;

/**
 * Coordinates board rendering, fullscreen scaling, and delegation to specialized sprite,
 * vector-overlay, and HUD collaborators.
 */
public class GameView {
    private final Game game;
    private final Canvas gridCanvas;
    private final GraphicsContext gc;
    private final double initialWidth;
    private final double initialHeight;
    private final SnakeSpriteRenderer snakeRenderer;
    private final EffectOverlayRenderer effectRenderer;
    private final GameHudManager hudManager;

    private boolean fullsize;
    private double fullScale;

    /**
     * Creates a game view sized and styled for the supplied game model.
     *
     * @param game active game model providing board dimensions and HUD state
     */
    public GameView(Game game) {
        this.game = game;
        initialWidth = game.getWidth() * game.getMultiplier();
        initialHeight = game.getHeight() * game.getMultiplier();
        gridCanvas = new Canvas(initialWidth, initialHeight);
        gc = gridCanvas.getGraphicsContext2D();
        snakeRenderer = new SnakeSpriteRenderer(game.getWidth(), game.getHeight());
        effectRenderer = new EffectOverlayRenderer();
        hudManager = new GameHudManager(game);
        fullScale = 1;
        updateFullscreenFontSizes(false);
    }

    /**
     * Draws the initial board state for a newly assembled scene.
     *
     * @param snake active snake
     * @param coin initial coin
     * @param obstacle board obstacle manager
     */
    public void makeScene(Snake snake, Coin coin, Obstacle obstacle) {
        drawGrid(coin, snake, obstacle);
    }

    /**
     * Attaches and aligns the top HUD, board canvas, and centered overlay labels inside the root pane.
     *
     * @param gamePane root pane hosting the game scene
     */
    public void layoutGamePane(StackPane gamePane) {
        hudManager.layoutGamePane(gamePane, gridCanvas);
    }

    /**
     * Switches fullscreen mode, recalculates canvas and font scaling, and redraws the board.
     *
     * @param fullScreen {@code true} to enter fullscreen; {@code false} to restore windowed size
     * @param stage window hosting the game scene
     * @param obstacle board obstacle manager
     * @param snake active snake
     * @param coin active coin
     */
    public void scaleToFullscreen(
            boolean fullScreen, Stage stage, Obstacle obstacle, Snake snake, Coin coin
    ) {
        stage.setFullScreen(fullScreen);
        updateScale(fullScreen, stage);
        updateFullscreenFontSizes(fullScreen);
        drawGrid(coin, snake, obstacle);
    }

    // Calculates a uniform scale that fits both the HUD strip and board inside the active screen.
    private void updateScale(boolean fullScreen, Stage stage) {
        if (fullScreen && !fullsize) {
            List<Screen> screens = Screen.getScreensForRectangle(
                    stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()
            );
            Screen screen = screens.isEmpty() ? Screen.getPrimary() : screens.get(0);
            Rectangle2D bounds = screen.getVisualBounds();

            double padding = GameHudManager.PANE_PADDING * 2;
            double availableWidth = Math.max(1.0, bounds.getWidth() - padding);
            double availableHeight = Math.max(1.0, bounds.getHeight() - padding);
            double widthScale = availableWidth / initialWidth;
            double heightScale = availableHeight
                    / (initialHeight + GameHudManager.HUD_RESERVED_HEIGHT);

            fullScale = Math.min(widthScale, heightScale);
            if (fullScale <= 0) {
                fullScale = 1;
            }
            gridCanvas.setWidth(initialWidth * fullScale);
            gridCanvas.setHeight(initialHeight * fullScale);
            fullsize = true;
            return;
        }

        if (!fullScreen) {
            fullScale = 1;
            gridCanvas.setWidth(initialWidth);
            gridCanvas.setHeight(initialHeight);
            fullsize = false;
        }
    }

    /**
     * Scales overlay and HUD label font sizes to match windowed or fullscreen mode.
     *
     * @param isFullscreen {@code true} when rendering in fullscreen mode
     */
    public void updateFullscreenFontSizes(boolean isFullscreen) {
        double scale = isFullscreen ? fullScale : 1.0;
        hudManager.updateFontSizes(scale, gridCanvas.getWidth());
    }

    /**
     * Renders a full board frame including magnet area, portals, coin, snake, shield halo, obstacles, and timer.
     *
     * @param coin active coin on the board
     * @param snake active snake
     * @param obstacle board obstacle manager
     */
    public void drawGrid(Coin coin, Snake snake, Obstacle obstacle) {
        double scale = currentScale();
        double cellSize = currentCellSize();

        gc.setFill(AssetManager.BACKGROUND_COLOR);
        gc.fillRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());

        if (game.getInventory().isMagnetActive()) {
            effectRenderer.drawMagnetRange(
                    gc, snake, game.getWidth(), game.getHeight(), cellSize, scale
            );
        }
        if (game.getPortalPair().isActive()) {
            effectRenderer.drawPortals(
                    gc, game.getPortalPair(), game.getWidth(), game.getHeight(), cellSize, scale
            );
        }
        if (!game.gameWon()) {
            drawCoin(coin, cellSize, scale);
        }

        snakeRenderer.drawSnake(gc, snake, game.getPortalPair(), cellSize);

        if (game.getInventory().getShieldCharges() > 0) {
            effectRenderer.drawShieldHalo(gc, snake, cellSize, scale);
        }

        drawObstacles(obstacle, cellSize);
        effectRenderer.drawMagnetTimer(
                gc, game.getInventory(), gridCanvas.getWidth(), cellSize, scale
        );
    }

    private void drawCoin(Coin coin, double cellSize, double scale) {
        Point position = coin.getPos();
        if (!snakeRenderer.isInsideBoard(position)) {
            return;
        }
        gc.drawImage(
                AssetManager.getCoinImage(coin.getType()),
                position.x * cellSize,
                position.y * cellSize,
                cellSize - 3 * scale,
                cellSize - 3 * scale
        );
    }

    private void drawObstacles(Obstacle obstacle, double cellSize) {
        gc.setFill(AssetManager.OBSTACLE_COLOR);
        for (Point point : obstacle.getObstacles()) {
            gc.fillRect(point.x * cellSize, point.y * cellSize, cellSize, cellSize);
        }
    }

    private double currentScale() {
        return fullsize ? fullScale : 1.0;
    }

    private double currentCellSize() {
        return game.getMultiplier() * currentScale();
    }

    /**
     * Records a visual turn marker at the snake head cell.
     *
     * @param snake active snake providing the head cell
     * @param previousKey direction key before the turn
     * @param currentKey direction key after the turn
     */
    public void turnDir(Snake snake, KeyCode previousKey, KeyCode currentKey) {
        snakeRenderer.turnDir(snake, previousKey, currentKey);
    }

    /**
     * Records a visual turn marker at a specific board cell.
     *
     * @param position board cell where the turn occurred
     * @param previousKey direction key before the turn
     * @param currentKey direction key after the turn
     */
    public void turnDir(Point position, KeyCode previousKey, KeyCode currentKey) {
        snakeRenderer.turnDir(position, previousKey, currentKey);
    }

    /**
     * Clears all stored body corner-turn markers for a new run or revival.
     */
    public void resetTurns() {
        snakeRenderer.resetTurns();
    }

    /**
     * Updates visibility of the game-over overlay based on the snake's alive state.
     *
     * @param snake active snake
     */
    public void gameOver(Snake snake) {
        hudManager.updateGameOver(snake);
    }

    /**
     * Updates visibility of the victory overlay based on the game's win state.
     */
    public void gameWon() {
        hudManager.updateGameWon();
    }

    /**
     * Updates visibility of the centered paused overlay based on the current game state.
     */
    public void updatePaused() {
        hudManager.updatePaused(game.getState() == GameState.PAUSED);
    }

    /**
     * Refreshes the two-line HUD label with current score, speed tier, shop stock, and portal status.
     */
    public void updateScoreLabel() {
        hudManager.updateScoreLabel();
    }

    /**
     * Returns the vertical height reserved above the board canvas for the top HUD bar at 1x scale.
     *
     * @return reserved HUD height in pixels
     */
    public double getHudReservedHeight() {
        return GameHudManager.HUD_RESERVED_HEIGHT;
    }

    /**
     * Reports whether the canvas is currently scaled to fullscreen size.
     *
     * @return {@code true} if fullscreen scaling is active
     */
    public boolean isFullsize() {
        return fullsize;
    }

    /**
     * Returns the canvas used to render the game board.
     *
     * @return board {@link Canvas}
     */
    public Canvas getGridCanvas() {
        return gridCanvas;
    }

    /**
     * Returns the 2D graphics context bound to the board canvas.
     *
     * @return board {@link GraphicsContext}
     */
    public GraphicsContext getGraphicsContext() {
        return gc;
    }

    /**
     * Returns the game-over overlay label.
     *
     * @return game-over {@link Label}
     */
    public Label getGameOverLabel() {
        return hudManager.getGameOverLabel();
    }

    /**
     * Returns the victory overlay label.
     *
     * @return victory {@link Label}
     */
    public Label getGameWonLabel() {
        return hudManager.getGameWonLabel();
    }

    /**
     * Returns the paused overlay label.
     *
     * @return pause {@link Label}
     */
    public Label getPausedLabel() {
        return hudManager.getPausedLabel();
    }

    /**
     * Returns the top HUD label showing score, speed tier, and shop status.
     *
     * @return HUD {@link Label}
     */
    public Label getScoreLabel() {
        return hudManager.getScoreLabel();
    }
}
