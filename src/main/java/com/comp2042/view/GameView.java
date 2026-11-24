package com.comp2042.view;

import com.comp2042.model.Coin;
import com.comp2042.model.Game;
import com.comp2042.model.Obstacle;
import com.comp2042.model.PlayerInventory;
import com.comp2042.model.PortalPair;
import com.comp2042.model.PowerUpType;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.awt.Point;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
        // Initialize the HUD with the active tier.
        // Include shop stock in the same label.
        scoreLabel = createLabel(formatScoreText(0), "scoreLabel");
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
        gamePane.setAlignment(scoreLabel, Pos.TOP_LEFT);
        // Keep the HUD at the top of the play area.
        // Use the full width for two compact rows.
        scoreLabel.setMaxWidth(Double.MAX_VALUE);
        scoreLabel.setAlignment(Pos.TOP_LEFT);
        scoreLabel.setTextAlignment(TextAlignment.LEFT);
        StackPane.setMargin(scoreLabel, new Insets(2, 2, 0, 2));
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

        // Use compact inline HUD sizing.
        // Scale it with the current canvas mode.
        gameOverLabel.setStyle(
                "-fx-font-size: " + 25 * scale + "px;"
        );
        gameWonLabel.setStyle(
                "-fx-font-size: " + 25 * scale + "px;"
        );
        scoreLabel.setStyle(
                "-fx-font-size: " + 12 * scale + "px;"
                        + " -fx-text-fill: #111111;"
                        + " -fx-padding: 2 6;"
        );
    }

    // Renders one complete board frame.
    public void drawGrid(Coin coin, Snake snake, Obstacle obstacle) {
        gc.setFill(AssetManager.BACKGROUND_COLOR);
        gc.fillRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());

        // Show the active magnet area below all actors.
        // Clip the five-by-five grid to the board.
        if (game.getInventory().isMagnetActive()) {
            drawMagnetRange(snake);
        }

        // Draw paired portals below the snake.
        // Use vector rings instead of external assets.
        if (game.getPortalPair().isActive()) {
            drawPortals();
        }

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

        // Mark an available shield around the head.
        // Draw it with the shared canvas scale.
        if (game.getInventory().getShieldCharges() > 0) {
            drawShieldHalo(snake);
        }

        drawObstacles(obstacle);
        drawMagnetTimer();
    }

    // Draws the magnet's five-by-five influence area.
    // Uses a translucent gold fill and a crisp border.
    private void drawMagnetRange(Snake snake) {
        Point head = snake.getHead();
        int minX = Math.max(0, head.x - PlayerInventory.MAGNET_RADIUS);
        int maxX = Math.min(
                game.getWidth() - 1,
                head.x + PlayerInventory.MAGNET_RADIUS
        );
        int minY = Math.max(0, head.y - PlayerInventory.MAGNET_RADIUS);
        int maxY = Math.min(
                game.getHeight() - 1,
                head.y + PlayerInventory.MAGNET_RADIUS
        );
        double cellSize = currentCellSize();
        double x = minX * cellSize;
        double y = minY * cellSize;
        double width = (maxX - minX + 1) * cellSize;
        double height = (maxY - minY + 1) * cellSize;
        double lineWidth = Math.max(1.0, 1.5 * fullScale);

        gc.setFill(Color.rgb(255, 215, 0, 0.20));
        gc.fillRect(x, y, width, height);
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(lineWidth);
        gc.strokeRect(
                x + lineWidth / 2,
                y + lineWidth / 2,
                Math.max(0, width - lineWidth),
                Math.max(0, height - lineWidth)
        );
    }

    // Draws the active portal pair as vector rings.
    // Blue marks portal A and orange marks portal B.
    private void drawPortals() {
        PortalPair portalPair = game.getPortalPair();
        Point portalA = portalPair.getPortalA();
        Point portalB = portalPair.getPortalB();
        boolean traversing = portalPair.isTraversing();
        double scale = fullsize ? fullScale : 1.0;
        double cellSize = game.getMultiplier() * scale;

        if (isInsideBoard(portalA)) {
            drawPortalRing(
                    portalA,
                    Color.DEEPSKYBLUE,
                    Color.LIGHTCYAN,
                    Color.rgb(0, 191, 255, 0.25),
                    traversing,
                    scale,
                    cellSize
            );
        }

        if (isInsideBoard(portalB)) {
            drawPortalRing(
                    portalB,
                    Color.DARKORANGE,
                    Color.GOLD,
                    Color.rgb(255, 140, 0, 0.25),
                    traversing,
                    scale,
                    cellSize
            );
        }
    }

    // Draws concentric rings inside one portal cell.
    // Adds a soft glow and thicker strokes during warp.
    private void drawPortalRing(
            Point cell,
            Color outerColor,
            Color innerColor,
            Color glowColor,
            boolean traversing,
            double scale,
            double cellSize
    ) {
        double x = cell.x * cellSize;
        double y = cell.y * cellSize;

        if (traversing) {
            gc.setFill(glowColor);
            gc.fillOval(x, y, cellSize, cellSize);
        }

        double outerStroke = Math.max(
                1.5,
                (traversing ? 2.8 : 2.0) * scale
        );
        double outerInset = Math.max(1.0, cellSize * 0.10);
        double outerDiameter = Math.max(1.0, cellSize - outerInset * 2);
        gc.setStroke(outerColor);
        gc.setLineWidth(outerStroke);
        gc.strokeOval(
                x + outerInset,
                y + outerInset,
                outerDiameter,
                outerDiameter
        );

        double innerStroke = Math.max(
                1.2,
                (traversing ? 2.2 : 1.5) * scale
        );
        double innerInset = Math.max(2.5, cellSize * 0.27);
        double innerDiameter = Math.max(1.0, cellSize - innerInset * 2);
        gc.setStroke(innerColor);
        gc.setLineWidth(innerStroke);
        gc.strokeOval(
                x + innerInset,
                y + innerInset,
                innerDiameter,
                innerDiameter
        );
    }

    // Draws a cyan ring around the protected head cell.
    // Keeps the ring inside its cell at every scale.
    private void drawShieldHalo(Snake snake) {
        Point head = snake.getHead();
        double cellSize = currentCellSize();
        double inset = Math.max(1.5, cellSize * 0.08);
        double diameter = Math.max(1.0, cellSize - inset * 2);

        gc.setStroke(Color.CYAN);
        gc.setLineWidth(Math.max(1.5, 2.0 * fullScale));
        gc.strokeOval(
                head.x * cellSize + inset,
                head.y * cellSize + inset,
                diameter,
                diameter
        );
    }

    // Draws a visible live magnet countdown at the top right.
    // Positions it below the two-line top HUD.
    private void drawMagnetTimer() {
        if (!game.getInventory().isMagnetActive()) {
            return;
        }

        double cellSize = currentCellSize();
        double bannerWidth = Math.min(
                gridCanvas.getWidth() - cellSize * 2,
                9 * cellSize
        );
        double bannerHeight = Math.max(cellSize, 18 * fullScale);
        double x = gridCanvas.getWidth() - bannerWidth - cellSize * 0.35;
        double y = cellSize * 2.35;
        String text = String.format(
                Locale.ROOT,
                "MAGNET %.1fs",
                game.getInventory().getMagnetTimeRemaining()
        );

        gc.setFill(Color.rgb(255, 215, 0, 0.94));
        gc.fillRoundRect(
                x,
                y,
                bannerWidth,
                bannerHeight,
                cellSize * 0.35,
                cellSize * 0.35
        );
        gc.setStroke(Color.rgb(154, 107, 0));
        gc.setLineWidth(Math.max(1.0, fullScale));
        gc.strokeRoundRect(
                x,
                y,
                bannerWidth,
                bannerHeight,
                cellSize * 0.35,
                cellSize * 0.35
        );
        gc.setFill(Color.rgb(30, 24, 0));
        gc.setFont(Font.font(
                "Arial",
                FontWeight.BOLD,
                Math.max(8.0, 12.0 * fullScale)
        ));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(
                text,
                x + cellSize * 0.35,
                y + bannerHeight * 0.70
        );
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
    // Handles portal splits without twisted sprites.
    private void drawBody(
            Snake snake,
            List<Point> body,
            Point point,
            int index
    ) {
        Image rotatedImage;
        if (index == 0) {
            rotatedImage = getRotatedImage(
                    AssetManager.getSnakeTail(),
                    tailRotationCache,
                    getTailRotation(snake, body)
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
                    getBodyRotation(snake, body, index)
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

    // Derive the straight body orientation safely.
    // Fall forward to the next segment across portal jumps.
    private int getBodyRotation(
            Snake snake,
            List<Point> body,
            int index
    ) {
        Point current = body.get(index);
        Point previous = body.get(index - 1);
        if (getAdjacentDirection(previous, current) >= 0) {
            return snake.getBodyDir(index);
        }

        Point next = index + 1 < body.size()
                ? body.get(index + 1)
                : snake.getHead();
        if (getAdjacentDirection(current, next) >= 0) {
            return current.y == next.y ? 270 : 0;
        }

        int headDir = snake.getSnakeDir();
        return (headDir == 90 || headDir == 270) ? 270 : 0;
    }

    // Derive the tail direction.
    // Support wrapped cells and portal-split segments.
    private int getTailRotation(Snake snake, List<Point> body) {
        Point tail = body.get(0);
        Point next = body.size() > 1 ? body.get(1) : snake.getHead();
        int adjacentDirection = getAdjacentDirection(tail, next);
        if (adjacentDirection >= 0) {
            return adjacentDirection;
        }

        return getPortalFallbackRotation(tail, snake.getSnakeDir());
    }

    // Computes a cardinal angle between adjacent cells.
    // Supports normal steps and board wrap-around edges.
    // Returns -1 when the two points are not adjacent.
    private int getAdjacentDirection(Point from, Point to) {
        if (from == null || to == null) {
            return -1;
        }

        int deltaX = to.x - from.x;
        int deltaY = to.y - from.y;
        int lastColumn = game.getWidth() - 1;
        int lastRow = game.getHeight() - 1;

        if (deltaX == 0 && (deltaY == -1 || deltaY == lastRow)) {
            return 0;
        }
        if (deltaX == 0 && (deltaY == 1 || deltaY == -lastRow)) {
            return 180;
        }
        if (deltaY == 0 && (deltaX == -1 || deltaX == lastColumn)) {
            return 270;
        }
        if (deltaY == 0 && (deltaX == 1 || deltaX == -lastColumn)) {
            return 90;
        }
        return -1;
    }

    // Points a portal-split tail toward its entry portal.
    // Falls back to the current heading on portal cells.
    private int getPortalFallbackRotation(Point tail, int defaultDir) {
        PortalPair portalPair = game.getPortalPair();
        if (!portalPair.isActive()) {
            return defaultDir;
        }

        Point portalA = portalPair.getPortalA();
        Point portalB = portalPair.getPortalB();
        Point nearest = selectNearestPortal(tail, portalA, portalB);
        if (nearest == null || tail.equals(nearest)) {
            return defaultDir;
        }

        int adjacentDirection = getAdjacentDirection(tail, nearest);
        if (adjacentDirection >= 0) {
            return adjacentDirection;
        }

        int deltaX = nearest.x - tail.x;
        int deltaY = nearest.y - tail.y;
        if (Math.abs(deltaX) >= Math.abs(deltaY)) {
            return deltaX > 0 ? 90 : 270;
        }
        return deltaY > 0 ? 180 : 0;
    }

    // Chooses the closer portal endpoint by grid distance.
    private Point selectNearestPortal(
            Point origin,
            Point portalA,
            Point portalB
    ) {
        if (portalA == null) {
            return portalB;
        }
        if (portalB == null) {
            return portalA;
        }

        int distA = Math.abs(portalA.x - origin.x)
                + Math.abs(portalA.y - origin.y);
        int distB = Math.abs(portalB.x - origin.x)
                + Math.abs(portalB.y - origin.y);
        return distA <= distB ? portalA : portalB;
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

    /**
     * Records a visual turn marker at the snake head cell.
     *
     * @param snake active snake providing the head cell
     * @param previousKey direction key before the turn
     * @param currentKey direction key after the turn
     */
    public void turnDir(
            Snake snake,
            KeyCode previousKey,
            KeyCode currentKey
    ) {
        turnDir(snake.getHead(), previousKey, currentKey);
    }

    /**
     * Records a visual turn marker at a specific board cell.
     *
     * @param position board cell where the turn occurred
     * @param previousKey direction key before the turn
     * @param currentKey direction key after the turn
     */
    public void turnDir(
            Point position,
            KeyCode previousKey,
            KeyCode currentKey
    ) {
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
        setTurn(position, turn);
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

    // Refresh every HUD value from model data.
    // Include tier, points, and power-up availability.
    public void updateScoreLabel() {
        scoreLabel.setText(formatScoreText(game.getScorePoints()));
    }

    // Formats the compact two-line game HUD.
    // Shows live magnet time while the power is active.
    private String formatScoreText(int scorePoints) {
        PlayerInventory inventory = game.getInventory();
        String magnetStatus = inventory.isMagnetActive()
                ? String.format(
                        Locale.ROOT,
                        "%.1fs",
                        inventory.getMagnetTimeRemaining()
                )
                : PowerUpType.MAGNET.getCost() + "pt";
        // Hide the revive option after its single use.
        // Keep it visible while held or still purchasable.
        String reviveOption = "";
        if (!inventory.hasUsedReviveStone()) {
            String reviveStatus = inventory.hasReviveStone()
                    ? "ON"
                    : PowerUpType.REVIVE_STONE.getCost() + "pt";
            reviveOption = "  [3]Revive: " + reviveStatus;
        }

        // Show a compact portal state while active.
        // Keep the second HUD row clean when inactive.
        PortalPair portalPair = game.getPortalPair();
        String portalOption = "";
        if (portalPair.isActive()) {
            String portalStatus = portalPair.isTraversing()
                    ? "WARPING"
                    : "READY";
            portalOption = "  Portal: " + portalStatus;
        }

        return String.format(
                Locale.ROOT,
                "Score: %d | %s (%.1fx) | Pts: %d\n"
                        + "[1]Shield: %d/%d  "
                        + "[2]Magnet: %s%s%s",
                scorePoints,
                game.getSpeedTier().getDisplayName(),
                game.getSpeedTier().getScoreMultiplier(),
                inventory.getShopPoints(),
                inventory.getShieldCharges(),
                PlayerInventory.MAX_SHIELD_PURCHASES,
                magnetStatus,
                reviveOption,
                portalOption
        );
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
