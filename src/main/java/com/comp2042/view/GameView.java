package com.comp2042.view;

import com.comp2042.model.Coin;
import com.comp2042.model.Game;
import com.comp2042.model.Obstacle;
import com.comp2042.model.Snake;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.awt.Point;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Renders the board and owns all JavaFX view nodes.
// Exposes behavior instead of mutable layout fields.
public class GameView {
    private final Game game;
    private final Canvas gridCanvas;
    private final GraphicsContext gc;
    private final Label gameOverLabel;
    private final Label gameWonLabel;
    private final Label scoreLabel;
    private final double initialWidth;
    private final double initialHeight;
    private final int[][] turnArray;

    private final Map<Integer, Image> headRotationCache;
    private final Map<Integer, Image> bodyRotationCache;
    private final Map<Integer, Image> turnRotationCache;
    private final Map<Integer, Image> tailRotationCache;

    private boolean gameOverVisible;
    private boolean gameWonVisible;
    private boolean fullsize;
    private double fullScale;

    // Builds a reusable scene with private controls.
    public GameView(Game game) {
        this.game = game;
        initialWidth = game.getWidth() * game.getMultiplier();
        initialHeight = game.getHeight() * game.getMultiplier();
        gridCanvas = new Canvas(initialWidth, initialHeight);
        gc = gridCanvas.getGraphicsContext2D();
        gameOverLabel = createLabel("GAME OVER", "gameOverLabel");
        gameWonLabel = createLabel("YOU WON!", "gameWonLabel");
        scoreLabel = createLabel("Score: 0", "scoreLabel");
        scoreLabel.setVisible(true);
        turnArray = new int[game.getWidth()][game.getHeight()];
        headRotationCache = new HashMap<>();
        bodyRotationCache = new HashMap<>();
        turnRotationCache = new HashMap<>();
        tailRotationCache = new HashMap<>();
        fullScale = 1;
        applyStylesheet();
        updateFullscreenFontSizes(false);
    }

    // Creates a hidden label with a stable CSS identifier.
    private Label createLabel(String text, String id) {
        Label label = new Label(text);
        label.setId(id);
        label.setVisible(false);
        return label;
    }

    // Resolves the stylesheet from the classpath.
    private void applyStylesheet() {
        URL cssLocation = GameView.class.getResource(
                "/resources/views/styling.css"
        );
        if (cssLocation == null) {
            return;
        }

        String stylesheet = cssLocation.toExternalForm();
        gameOverLabel.getStylesheets().add(stylesheet);
        gameWonLabel.getStylesheets().add(stylesheet);
        scoreLabel.getStylesheets().add(stylesheet);
    }

    // Draws the initial board for a scene.
    // Draws the initial board state.
    public void makeScene(Snake snake, Coin coin, Obstacle obstacle) {
        drawGrid(coin, snake, obstacle);
    }

    // Adds all view nodes to the supplied game pane.
    // Keeps sizing and alignment inside the view layer.
    public void layoutGamePane(StackPane gamePane) {
        gamePane.getChildren().addAll(
                gridCanvas,
                gameOverLabel,
                gameWonLabel,
                scoreLabel
        );
        gamePane.setPadding(new Insets(20));
        gamePane.setAlignment(gameOverLabel, Pos.TOP_CENTER);
        gamePane.setAlignment(gameWonLabel, Pos.TOP_CENTER);
        gamePane.setAlignment(scoreLabel, Pos.BOTTOM_CENTER);
    }

    // Toggles fullscreen scaling and redraws the board.
    public void scaleToFullscreen(
            boolean fullScreen,
            Stage stage,
            Obstacle obstacle,
            Snake snake,
            Coin coin
    ) {
        stage.setFullScreen(fullScreen);
        updateScale(fullScreen, stage);
        updateFullscreenFontSizes(fullScreen);
        drawGrid(coin, snake, obstacle);
    }

    // Calculates a scale that fits the current screen.
    private void updateScale(boolean fullScreen, Stage stage) {
        if (fullScreen && !fullsize) {
            List<Screen> screens = Screen.getScreensForRectangle(
                    stage.getX(),
                    stage.getY(),
                    stage.getWidth(),
                    stage.getHeight()
            );
            Screen screen = screens.isEmpty()
                    ? Screen.getPrimary()
                    : screens.get(0);
            Rectangle2D bounds = screen.getVisualBounds();

            // Reserve the pane padding.
            // Fit the board in the content area.
            double availableWidth = Math.max(1.0, bounds.getWidth() - 40);
            double availableHeight = Math.max(1.0, bounds.getHeight() - 40);
            double widthScale = availableWidth / initialWidth;
            double heightScale = availableHeight / initialHeight;

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

    // Updates every font consistently for the current mode.
    public void updateFullscreenFontSizes(boolean isFullscreen) {
        double scale = isFullscreen ? fullScale : 1;

        // Use inline font sizes.
        // Override the fixed author CSS.
        gameOverLabel.setStyle(
                "-fx-font-size: " + 25 * scale + "px;"
        );
        gameWonLabel.setStyle(
                "-fx-font-size: " + 25 * scale + "px;"
        );
        scoreLabel.setStyle(
                "-fx-font-size: " + 20 * scale + "px;"
        );
    }

    // Renders one complete board frame.
    public void drawGrid(Coin coin, Snake snake, Obstacle obstacle) {
        gc.setFill(AssetManager.BACKGROUND_COLOR);
        gc.fillRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());

        // Hide the consumed final coin.
        // Keep the victory frame clean.
        if (!game.gameWon()) {
            drawCoin(coin);
        }

        List<Point> body = snake.getBody();
        for (int index = 0; index < body.size(); index++) {
            drawBody(snake, body, body.get(index), index);
        }
        drawHead(snake);
        drawObstacles(obstacle);
    }

    // Draws the active coin when it has a valid position.
    private void drawCoin(Coin coin) {
        Point position = coin.getPos();
        if (!isInsideBoard(position)) {
            return;
        }

        double cellSize = currentCellSize();
        gc.drawImage(
                AssetManager.getCoinImage(coin.getType()),
                position.x * cellSize,
                position.y * cellSize,
                cellSize - 3 * fullScale,
                cellSize - 3 * fullScale
        );
    }

    // Draws the snake head with a cached rotation.
    private void drawHead(Snake snake) {
        Image rotatedImage = getRotatedImage(
                AssetManager.getSnakeHead(),
                headRotationCache,
                snake.getSnakeDir()
        );
        double cellSize = currentCellSize();
        Point head = snake.getHead();
        gc.drawImage(
                rotatedImage,
                head.x * cellSize,
                head.y * cellSize,
                cellSize,
                cellSize
        );
    }

    // Selects a body image and draws one segment.
    private void drawBody(
            Snake snake,
            List<Point> body,
            Point point,
            int index
    ) {
        Image rotatedImage;
        if (body.size() <= 1) {
            rotatedImage = getRotatedImage(
                    AssetManager.getSnakeTail(),
                    tailRotationCache,
                    snake.getSnakeDir()
            );
        } else if (index == 0) {
            rotatedImage = getRotatedImage(
                    AssetManager.getSnakeTail(),
                    tailRotationCache,
                    getTailRotation(body)
            );
        } else if (getTurn(point) != 0) {
            rotatedImage = getRotatedImage(
                    AssetManager.getSnakeTurn(),
                    turnRotationCache,
                    90 * getTurn(point)
            );
        } else {
            rotatedImage = getRotatedImage(
                    AssetManager.getSnakeBody(),
                    bodyRotationCache,
                    snake.getBodyDir(index)
            );
        }

        double cellSize = currentCellSize();
        gc.drawImage(
                rotatedImage,
                point.x * cellSize,
                point.y * cellSize,
                cellSize,
                cellSize
        );

        if (index == 0) {
            setTurn(point, 0);
        }
    }

    // Derive the tail direction.
    // Support wrapped adjacent cells.
    private int getTailRotation(List<Point> body) {
        Point tail = body.get(0);
        Point next = body.get(1);
        int deltaX = next.x - tail.x;
        int deltaY = next.y - tail.y;

        // Detect upward adjacency.
        // Include the top wrapping edge.
        if (deltaY == -1 || deltaY == game.getHeight() - 1) {
            return 0;
        }

        // Detect downward adjacency.
        // Include the bottom wrapping edge.
        if (deltaY == 1 || deltaY == -(game.getHeight() - 1)) {
            return 180;
        }

        // Detect leftward adjacency.
        // Include the left wrapping edge.
        if (deltaX == -1 || deltaX == game.getWidth() - 1) {
            return 270;
        }

        // Handle the remaining right edge.
        // Use the right-facing sprite.
        return 90;
    }

    // Draws every obstacle using the shared color.
    private void drawObstacles(Obstacle obstacle) {
        gc.setFill(AssetManager.OBSTACLE_COLOR);
        double cellSize = currentCellSize();
        for (Point point : obstacle.getObstacles()) {
            gc.fillRect(
                    point.x * cellSize,
                    point.y * cellSize,
                    cellSize,
                    cellSize
            );
        }
    }

    // Returns one grid cell in canvas pixels.
    private double currentCellSize() {
        return game.getMultiplier() * fullScale;
    }

    // Caches a transparent rotated image for each angle.
    private Image getRotatedImage(
            Image image,
            Map<Integer, Image> cache,
            int rotation
    ) {
        int normalizedRotation = Math.floorMod(rotation, 360);
        return cache.computeIfAbsent(normalizedRotation, angle -> {
            ImageView imageView = new ImageView(image);
            imageView.setRotate(angle);
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            return imageView.snapshot(parameters, null);
        });
    }

    // Checks whether a point belongs to the game board.
    private boolean isInsideBoard(Point point) {
        if (point == null) {
            return false;
        }
        return point.x >= 0
                && point.x < turnArray.length
                && point.y >= 0
                && point.y < turnArray[point.x].length;
    }

    private int getTurn(Point point) {
        if (!isInsideBoard(point)) {
            return 0;
        }
        return turnArray[point.x][point.y];
    }

    private void setTurn(Point point, int value) {
        if (isInsideBoard(point)) {
            turnArray[point.x][point.y] = value;
        }
    }

    // Records a visual turn marker for the head.
    public void turnDir(
            Snake snake,
            KeyCode previousKey,
            KeyCode currentKey
    ) {
        Point head = snake.getHead();
        int turn = 0;
        if (isTurn(previousKey, currentKey, KeyCode.LEFT, KeyCode.UP)
                || isTurn(
                previousKey,
                currentKey,
                KeyCode.DOWN,
                KeyCode.RIGHT
        )) {
            turn = 2;
        } else if (isTurn(
                previousKey,
                currentKey,
                KeyCode.LEFT,
                KeyCode.DOWN
        ) || isTurn(
                previousKey,
                currentKey,
                KeyCode.UP,
                KeyCode.RIGHT
        )) {
            turn = 3;
        } else if (isTurn(
                previousKey,
                currentKey,
                KeyCode.RIGHT,
                KeyCode.UP
        ) || isTurn(
                previousKey,
                currentKey,
                KeyCode.DOWN,
                KeyCode.LEFT
        )) {
            turn = 1;
        } else if (isTurn(
                previousKey,
                currentKey,
                KeyCode.UP,
                KeyCode.LEFT
        ) || isTurn(
                previousKey,
                currentKey,
                KeyCode.RIGHT,
                KeyCode.DOWN
        )) {
            turn = 4;
        }
        setTurn(head, turn);
    }

    // Tests a directional turn pair.
    private boolean isTurn(
            KeyCode previous,
            KeyCode current,
            KeyCode expectedPrevious,
            KeyCode expectedCurrent
    ) {
        return previous == expectedPrevious && current == expectedCurrent;
    }

    // Clears all turn markers for a new run.
    public void resetTurns() {
        for (int x = 0; x < turnArray.length; x++) {
            for (int y = 0; y < turnArray[x].length; y++) {
                turnArray[x][y] = 0;
            }
        }
    }

    // Synchronizes the game-over overlay.
    public void gameOver(Snake snake) {
        if (snake.getDead() && !gameOverVisible) {
            gameOverVisible = true;
            gameOverLabel.setVisible(true);
        } else if (!snake.getDead() && gameOverVisible) {
            gameOverVisible = false;
            gameOverLabel.setVisible(false);
        }
    }

    // Synchronizes the victory overlay.
    public void gameWon() {
        if (game.gameWon() && !gameWonVisible) {
            gameWonVisible = true;
            gameWonLabel.setVisible(true);
        } else if (!game.gameWon() && gameWonVisible) {
            gameWonVisible = false;
            gameWonLabel.setVisible(false);
        }
    }

    // Refreshes the score text from model data.
    public void updateScoreLabel() {
        scoreLabel.setText("Score: " + game.getScorePoints());
    }

    public boolean isFullsize() {
        return fullsize;
    }

    public Canvas getGridCanvas() {
        return gridCanvas;
    }

    public GraphicsContext getGraphicsContext() {
        return gc;
    }

    public Label getGameOverLabel() {
        return gameOverLabel;
    }

    public Label getGameWonLabel() {
        return gameWonLabel;
    }

    public Label getScoreLabel() {
        return scoreLabel;
    }
}
