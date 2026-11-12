package com.comp2042;

import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.awt.Point;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameView {

    public Canvas gridCanvas;
    public GraphicsContext gc;

    public Label gameOverLabel;
    public Label gameWonLabel;
    public Label scoreLabel;
    private boolean gameOverVis = false;
    private boolean gameWonVis = false;

    private Game game;
    private double initX;
    private double initY;
    private double fullScale = 1;
    private boolean fullsize = false;
    private int[][] turnArray;

    private final Map<Integer, Image> headRotationCache = new HashMap<>();
    private final Map<Integer, Image> bodyRotationCache = new HashMap<>();
    private final Map<Integer, Image> turnRotationCache = new HashMap<>();
    private final Map<Integer, Image> tailRotationCache = new HashMap<>();

    public GameView(Game game) {
        this.game = game;
        // Allocate turn markers from the actual board dimensions.
        // Keep every access aligned with game coordinates.
        turnArray = new int[game.getWidth()][game.getHeight()];
    }

    public void makeScene(Scene scene, Snake snek, Coin coin) {
        // Size the canvas from the board's own pixels.
        // Keep the surrounding menu padding outside the canvas.
        double boardWidth = game.getWidth() * game.getMultiplier();
        double boardHeight = game.getHeight() * game.getMultiplier();
        gridCanvas = new Canvas(boardWidth, boardHeight);
        initX = boardWidth;
        initY = boardHeight;
        gc = gridCanvas.getGraphicsContext2D();
        drawGrid(coin, snek, gc);

        gameOverLabel = new Label("GAME OVER");
        gameOverLabel.setId("gameOverLabel");
        gameOverLabel.setVisible(false);

        gameWonLabel = new Label("YOU WON!");
        gameWonLabel.setId("gameWonLabel");
        gameWonLabel.setVisible(false);

        scoreLabel = new Label("Score: " + game.getScorePoints());
        scoreLabel.setId("scoreLabel");
        scoreLabel.setFont(new Font("Arial", 20));
        scoreLabel.setVisible(true);

        // Resolve the stylesheet from the classpath.
        // Replace the invalid working-directory relative path.
        URL cssLocation = GameView.class.getResource("/resources/views/styling.css");
        if (cssLocation != null) {
            String css = cssLocation.toExternalForm();
            // Apply the resolved stylesheet to every game label.
            // Keep all overlay text visually consistent.
            gameOverLabel.getStylesheets().add(css);
            gameWonLabel.getStylesheets().add(css);
            scoreLabel.getStylesheets().add(css);
        }
    }

    public void scaleToFullscreen(boolean fullScreen, Stage primaryStage, Obstacle obstacle, Snake snek, Coin coin) {
        primaryStage.setFullScreen(fullScreen);

        if (fullScreen && !fullsize) {
            // Locate the screen that contains the game window.
            // Fall back to the primary screen when it cannot be found.
            List<Screen> screens = Screen.getScreensForRectangle(
                    primaryStage.getX(),
                    primaryStage.getY(),
                    primaryStage.getWidth(),
                    primaryStage.getHeight()
            );
            Screen screen = screens.isEmpty() ? Screen.getPrimary() : screens.get(0);
            Rectangle2D bounds = screen.getVisualBounds();

            // Match the width ratio to the board width.
            // Match the height ratio to the board height.
            double widthScaling = bounds.getWidth() / initX;
            double heightScaling = bounds.getHeight() / initY;
            fullScale = Math.min(widthScaling, heightScaling);
            if (fullScale <= 0) {
                fullScale = 1;
            }

            gridCanvas.setWidth(initX * fullScale);
            gridCanvas.setHeight(initY * fullScale);
            fullsize = true;
        } else if (!fullScreen) {
            fullScale = 1;
            gridCanvas.setWidth(initX);
            gridCanvas.setHeight(initY);
            fullsize = false;
        }

        gameOverLabel.setFont(new Font("Arial", 25 * fullScale));
        gameWonLabel.setFont(new Font("Arial", 25 * fullScale));
        drawGrid(coin, snek, obstacle, gc, false, 0);
    }

    public void drawGrid(Coin coin, Snake snek, Obstacle obstacle, GraphicsContext gc, boolean a, int b) {
        gc.setFill(game.getColor());
        gc.fillRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());

        gc.drawImage(coin.getImage(), (coin.getPos().x * game.getMultiplier()) * fullScale, (coin.getPos().y * game.getMultiplier()) * fullScale, game.getMultiplier() * fullScale - 3 * fullScale, game.getMultiplier() * fullScale - 3 * fullScale);

        // Fetch the body list only once per frame.
        // Avoid allocating a new list for every body segment.
        List<Point> body = snek.getBody();
        int bodyIndex = 0;
        for (Point point : body) {
            drawBody(snek, body, point, bodyIndex);
            bodyIndex++;
        }

        drawHead(snek);

        gc.setFill(obstacle.getColor());
        for (Point point : obstacle.getObstacles()) {
            gc.fillRect(point.x * game.getMultiplier() * fullScale, point.y * game.getMultiplier() * fullScale, game.getMultiplier() * fullScale, game.getMultiplier() * fullScale);
        }
    }

    private void drawGrid(Coin coin, Snake snek, GraphicsContext gc) {
        gc.setFill(game.getColor());
        gc.fillRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());

        gc.drawImage(coin.getImage(), (coin.getPos().x * game.getMultiplier()) * fullScale, (coin.getPos().y * game.getMultiplier()) * fullScale, game.getMultiplier() * fullScale - 3 * fullScale, game.getMultiplier() * fullScale - 3 * fullScale);

        // Fetch the body list only once for the initial frame.
        // Reuse it while drawing every body segment.
        List<Point> body = snek.getBody();
        int bodyIndex = 0;
        for (Point point : body) {
            drawBody(snek, body, point, bodyIndex);
            bodyIndex++;
        }

        drawHead(snek);
    }

    private void drawHead(Snake snek) {
        // Reuse the cached head snapshot for this direction.
        // Keep the cell size proportional to the fullscreen scale.
        Image rotatedImage = getRotatedImage(
                snek.getImageViewHead(),
                headRotationCache,
                snek.getSnakeDir()
        );
        double cellSize = game.getMultiplier() * fullScale;
        Point head = snek.getHead();
        gc.drawImage(
                rotatedImage,
                head.x * game.getMultiplier() * fullScale,
                head.y * game.getMultiplier() * fullScale,
                cellSize,
                cellSize
        );
    }

    private void drawBody(Snake snek, List<Point> body, Point point, int index) {
        Image rotatedImage;
        if (body.size() <= 1) {
            // Cache the single tail segment by direction.
            // Reuse the image on later frames.
            rotatedImage = getRotatedImage(
                    snek.getImageViewTail(),
                    tailRotationCache,
                    snek.getSnakeDir()
            );
        } else if (index == 0) {
            int rotateTail;
            if (body.get(1).getX() == body.get(0).getX() && body.get(1).getY() == body.get(0).getY() - 1) {
                rotateTail = 0;
            } else if (body.get(1).getX() == body.get(0).getX() && body.get(1).getY() == body.get(0).getY() + 1) {
                rotateTail = 180;
            } else if (body.get(1).getX() == body.get(0).getX() - 1 && body.get(1).getY() == body.get(0).getY()) {
                rotateTail = 270;
            } else {
                rotateTail = 90;
            }
            // Reuse the cached tail snapshot for this angle.
            // Avoid rebuilding it during every animation frame.
            rotatedImage = getRotatedImage(
                    snek.getImageViewTail(),
                    tailRotationCache,
                    rotateTail
            );
        } else if (index > 0 && getTurn(point) != 0) {
            // Reuse the cached turn snapshot for this angle.
            // Keep the rotate lookup within the board bounds.
            rotatedImage = getRotatedImage(
                    snek.getImageViewTurn(),
                    turnRotationCache,
                    90 * getTurn(point)
            );
        } else {
            // Reuse the cached body snapshot for this direction.
            // Avoid repeated snapshots for straight segments.
            rotatedImage = getRotatedImage(
                    snek.getImageViewBody(),
                    bodyRotationCache,
                    snek.getBodyDir(index)
            );
        }

        // Draw the body cell with the corrected scaled size.
        // Keep width and height based on one multiplier.
        double cellSize = game.getMultiplier() * fullScale;
        gc.drawImage(
                rotatedImage,
                point.x * game.getMultiplier() * fullScale,
                point.y * game.getMultiplier() * fullScale,
                cellSize,
                cellSize
        );

        if (index == 0) {
            // Clear the tail turn marker after drawing.
            // Avoid erasing it in the middle of the body loop.
            setTurn(point, 0);
        }
    }

    private Image getRotatedImage(ImageView imageView, Map<Integer, Image> cache, int rotation) {
        // Normalize all rotations to a fixed angle key.
        // Reuse an existing snapshot whenever one is available.
        int normalizedRotation = Math.floorMod(rotation, 360);
        return cache.computeIfAbsent(normalizedRotation, angle -> {
            // Rotate the shared image view only when creating a snapshot.
            // Store the result for later frames at the same angle.
            imageView.setRotate(angle);
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            return imageView.snapshot(params, null);
        });
    }

    private boolean isInsideBoard(Point point) {
        if (point == null) {
            return false;
        }
        int x = (int) point.getX();
        int y = (int) point.getY();
        // Check both dimensions before accessing the array.
        // Treat off-board coordinates as invalid markers.
        return x >= 0
                && x < turnArray.length
                && y >= 0
                && y < turnArray[x].length;
    }

    private int getTurn(Point point) {
        if (!isInsideBoard(point)) {
            return 0;
        }
        return turnArray[(int) point.getX()][(int) point.getY()];
    }

    private void setTurn(Point point, int value) {
        if (isInsideBoard(point)) {
            turnArray[(int) point.getX()][(int) point.getY()] = value;
        }
    }

    public void turnDir(Snake snek) {
        // Keep the legacy signature for existing callers.
        // Use a neutral key pair so no stale turn marker is added.
        turnDir(snek, KeyCode.LEFT, KeyCode.LEFT);
    }

    public void turnDir(Snake snek, KeyCode previousKey, KeyCode currentKey) {
        // Use the supplied key history for this turn.
        // Avoid shared static input state between controllers.
        Point head = snek.getHead();
        int turn = 0;
        if ((previousKey == KeyCode.LEFT && currentKey == KeyCode.UP) || (previousKey == KeyCode.DOWN && currentKey == KeyCode.RIGHT)) {
            turn = 2;
        } else if ((previousKey == KeyCode.LEFT && currentKey == KeyCode.DOWN) || (previousKey == KeyCode.UP && currentKey == KeyCode.RIGHT)) {
            turn = 3;
        } else if ((previousKey == KeyCode.RIGHT && currentKey == KeyCode.UP) || (previousKey == KeyCode.DOWN && currentKey == KeyCode.LEFT)) {
            turn = 1;
        } else if ((previousKey == KeyCode.UP && currentKey == KeyCode.LEFT) || (previousKey == KeyCode.RIGHT && currentKey == KeyCode.DOWN)) {
            turn = 4;
        }
        // Guard the marker write against off-board coordinates.
        // Keep unchanged cells safe during border wrapping.
        setTurn(head, turn);
    }

    public void resetTurns() {
        // Clear turn markers from the previous run.
        // This avoids stale corner rendering after reset.
        for (int x = 0; x < turnArray.length; x++) {
            for (int y = 0; y < turnArray[x].length; y++) {
                turnArray[x][y] = 0;
            }
        }
    }

    public boolean isFullsize() {
        // Expose the current fullscreen scaling state.
        // Callers can check this without changing the stage.
        return fullsize;
    }

    public void gameOver(Snake snek) {
        if (snek.getDead() && !gameOverVis) {
            gameOverVis = true;
            gameOverLabel.setVisible(true);
        } else if (!snek.getDead() && gameOverVis) {
            gameOverLabel.setVisible(false);
            gameOverVis = false;
        }
    }

    public void gameWon() {
        if (game.gameWon() && !gameWonVis) {
            gameWonVis = true;
            gameWonLabel.setVisible(true);
        } else if (!game.gameWon() && gameWonVis) {
            gameWonVis = false;
            gameWonLabel.setVisible(false);
        }
    }

    public void updateScoreLabel() {
        scoreLabel.setText("Score: " + game.getScorePoints());
    }

}
