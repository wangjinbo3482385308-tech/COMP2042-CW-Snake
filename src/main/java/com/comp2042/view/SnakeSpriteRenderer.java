package com.comp2042.view;

import com.comp2042.model.PortalPair;
import com.comp2042.model.Snake;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders snake head, body, corner turn, and tail sprites using cached rotated images
 * and grid-geometry orientation rules that support wrapped borders and portal jumps.
 */
public class SnakeSpriteRenderer {
    private final int boardWidth;
    private final int boardHeight;
    private final int[][] turnArray;

    private final Map<Integer, Image> headRotationCache;
    private final Map<Integer, Image> bodyRotationCache;
    private final Map<Integer, Image> turnRotationCache;
    private final Map<Integer, Image> tailRotationCache;

    /**
     * Creates a snake sprite renderer sized for the given board dimensions.
     *
     * @param boardWidth board width in grid cells
     * @param boardHeight board height in grid cells
     */
    public SnakeSpriteRenderer(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        turnArray = new int[boardWidth][boardHeight];
        headRotationCache = new HashMap<>();
        bodyRotationCache = new HashMap<>();
        turnRotationCache = new HashMap<>();
        tailRotationCache = new HashMap<>();
    }

    /**
     * Draws all snake body segments and the snake head onto the graphics context.
     *
     * @param gc target canvas graphics context
     * @param snake active snake model
     * @param portalPair active portal pair used for portal-split tail orientation fallback
     * @param cellSize rendered size of one board cell in canvas pixels
     */
    public void drawSnake(
            GraphicsContext gc,
            Snake snake,
            PortalPair portalPair,
            double cellSize
    ) {
        List<Point> body = snake.getBody();
        for (int index = 0; index < body.size(); index++) {
            drawBodySegment(gc, snake, portalPair, body, body.get(index), index, cellSize);
        }
        drawHead(gc, snake, cellSize);
    }

    // Draws the snake head with a cached rotation.
    private void drawHead(GraphicsContext gc, Snake snake, double cellSize) {
        Image rotatedImage = getRotatedImage(
                AssetManager.getSnakeHead(),
                headRotationCache,
                snake.getSnakeDir()
        );
        Point head = snake.getHead();
        gc.drawImage(
                rotatedImage,
                head.x * cellSize,
                head.y * cellSize,
                cellSize,
                cellSize
        );
    }

    // Selects a body sprite and draws one segment, clearing expired turn markers at the tail.
    private void drawBodySegment(
            GraphicsContext gc,
            Snake snake,
            PortalPair portalPair,
            List<Point> body,
            Point point,
            int index,
            double cellSize
    ) {
        Image rotatedImage;
        if (index == 0) {
            rotatedImage = getRotatedImage(
                    AssetManager.getSnakeTail(),
                    tailRotationCache,
                    computeTailRotation(
                            body,
                            snake.getHead(),
                            snake.getSnakeDir(),
                            portalPair,
                            boardWidth,
                            boardHeight
                    )
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
                    computeBodyRotation(
                            body,
                            snake.getHead(),
                            index,
                            snake.getSnakeDir(),
                            boardWidth,
                            boardHeight
                    )
            );
        }

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

    /**
     * Records a visual corner-turn marker at the snake's current head cell.
     *
     * @param snake active snake providing the head cell
     * @param previousKey direction key before the turn
     * @param currentKey direction key after the turn
     */
    public void turnDir(Snake snake, KeyCode previousKey, KeyCode currentKey) {
        turnDir(snake.getHead(), previousKey, currentKey);
    }

    /**
     * Records a visual corner-turn marker at a specific board cell.
     *
     * @param position board cell where the turn occurred
     * @param previousKey direction key before the turn
     * @param currentKey direction key after the turn
     */
    public void turnDir(Point position, KeyCode previousKey, KeyCode currentKey) {
        setTurn(position, computeTurnCode(previousKey, currentKey));
    }

    /**
     * Clears all stored corner-turn markers for a new run or revival.
     */
    public void resetTurns() {
        for (int x = 0; x < turnArray.length; x++) {
            for (int y = 0; y < turnArray[x].length; y++) {
                turnArray[x][y] = 0;
            }
        }
    }

    /**
     * Checks whether the given point lies within the renderer's board bounds.
     *
     * @param point board cell coordinate to check
     * @return {@code true} if the point is non-null and within the board bounds
     */
    public boolean isInsideBoard(Point point) {
        if (point == null) {
            return false;
        }
        return point.x >= 0
                && point.x < turnArray.length
                && point.y >= 0
                && point.y < turnArray[point.x].length;
    }

    // Returns the turn marker at the given cell, or 0 when out of bounds.
    int getTurn(Point point) {
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

    // Maps a pair of consecutive direction keys to a 90-degree turn sprite index (0..4).
    static int computeTurnCode(KeyCode previousKey, KeyCode currentKey) {
        if (isTurn(previousKey, currentKey, KeyCode.LEFT, KeyCode.UP)
                || isTurn(previousKey, currentKey, KeyCode.DOWN, KeyCode.RIGHT)) {
            return 2;
        }
        if (isTurn(previousKey, currentKey, KeyCode.LEFT, KeyCode.DOWN)
                || isTurn(previousKey, currentKey, KeyCode.UP, KeyCode.RIGHT)) {
            return 3;
        }
        if (isTurn(previousKey, currentKey, KeyCode.RIGHT, KeyCode.UP)
                || isTurn(previousKey, currentKey, KeyCode.DOWN, KeyCode.LEFT)) {
            return 1;
        }
        if (isTurn(previousKey, currentKey, KeyCode.UP, KeyCode.LEFT)
                || isTurn(previousKey, currentKey, KeyCode.RIGHT, KeyCode.DOWN)) {
            return 4;
        }
        return 0;
    }

    private static boolean isTurn(
            KeyCode previous,
            KeyCode current,
            KeyCode expectedPrevious,
            KeyCode expectedCurrent
    ) {
        return previous == expectedPrevious && current == expectedCurrent;
    }

    /**
     * Derives the straight body segment rotation angle ({@code 0} for vertical or {@code 270}
     * for horizontal), falling forward to the next segment or head heading across portal jumps.
     *
     * @param body ordered list of snake body segments from tail to neck
     * @param head current snake head position
     * @param index body segment index to orient (must be at least {@code 1})
     * @param headDir current snake head heading angle in degrees
     * @param boardWidth board width in grid cells
     * @param boardHeight board height in grid cells
     * @return {@code 270} for horizontal orientation or {@code 0} for vertical orientation
     */
    public static int computeBodyRotation(
            List<Point> body,
            Point head,
            int index,
            int headDir,
            int boardWidth,
            int boardHeight
    ) {
        Point current = body.get(index);
        Point previous = body.get(index - 1);
        if (computeAdjacentDirection(previous, current, boardWidth, boardHeight) >= 0) {
            return current.y == previous.y ? 270 : 0;
        }

        Point next = index + 1 < body.size() ? body.get(index + 1) : head;
        if (computeAdjacentDirection(current, next, boardWidth, boardHeight) >= 0) {
            return current.y == next.y ? 270 : 0;
        }

        return (headDir == 90 || headDir == 270) ? 270 : 0;
    }

    /**
     * Derives the tail sprite rotation angle, handling normal adjacency, wrapped board edges,
     * and non-adjacent portal-split segments.
     *
     * @param body ordered list of snake body segments from tail to neck
     * @param head current snake head position
     * @param headDir current snake head heading angle in degrees
     * @param portalPair active portal pair used for fallback orientation, or {@code null}
     * @param boardWidth board width in grid cells
     * @param boardHeight board height in grid cells
     * @return cardinal rotation angle in degrees ({@code 0}, {@code 90}, {@code 180}, or {@code 270})
     */
    public static int computeTailRotation(
            List<Point> body,
            Point head,
            int headDir,
            PortalPair portalPair,
            int boardWidth,
            int boardHeight
    ) {
        Point tail = body.get(0);
        Point next = body.size() > 1 ? body.get(1) : head;
        int adjacentDirection = computeAdjacentDirection(
                tail,
                next,
                boardWidth,
                boardHeight
        );
        if (adjacentDirection >= 0) {
            return adjacentDirection;
        }

        return computePortalFallbackRotation(
                tail,
                headDir,
                portalPair,
                boardWidth,
                boardHeight
        );
    }

    /**
     * Computes the cardinal rotation angle between two adjacent board cells,
     * supporting both direct neighbors and wrap-around across opposite edges.
     *
     * @param from source grid cell
     * @param to destination grid cell
     * @param boardWidth board width in grid cells
     * @param boardHeight board height in grid cells
     * @return {@code 0} (up), {@code 90} (right), {@code 180} (down), {@code 270} (left),
     *         or {@code -1} if the cells are not adjacent
     */
    public static int computeAdjacentDirection(
            Point from,
            Point to,
            int boardWidth,
            int boardHeight
    ) {
        if (from == null || to == null || boardWidth <= 0 || boardHeight <= 0) {
            return -1;
        }

        int deltaX = to.x - from.x;
        int deltaY = to.y - from.y;
        int lastColumn = boardWidth - 1;
        int lastRow = boardHeight - 1;

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

    /**
     * Points a portal-split tail segment toward its nearest active portal endpoint,
     * falling back to the default heading when portals are inactive or overlap the tail.
     *
     * @param tail tail grid cell
     * @param defaultDir fallback heading angle in degrees
     * @param portalPair active portal pair, or {@code null}
     * @param boardWidth board width in grid cells
     * @param boardHeight board height in grid cells
     * @return cardinal rotation angle in degrees
     */
    public static int computePortalFallbackRotation(
            Point tail,
            int defaultDir,
            PortalPair portalPair,
            int boardWidth,
            int boardHeight
    ) {
        if (tail == null || portalPair == null || !portalPair.isActive()) {
            return defaultDir;
        }

        Point nearest = selectNearestPortal(
                tail,
                portalPair.getPortalA(),
                portalPair.getPortalB()
        );
        if (nearest == null || tail.equals(nearest)) {
            return defaultDir;
        }

        int adjacentDirection = computeAdjacentDirection(
                tail,
                nearest,
                boardWidth,
                boardHeight
        );
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

    // Chooses the closer portal endpoint by Manhattan grid distance.
    static Point selectNearestPortal(Point origin, Point portalA, Point portalB) {
        if (portalA == null) {
            return portalB;
        }
        if (portalB == null) {
            return portalA;
        }

        int distA = Math.abs(portalA.x - origin.x) + Math.abs(portalA.y - origin.y);
        int distB = Math.abs(portalB.x - origin.x) + Math.abs(portalB.y - origin.y);
        return distA <= distB ? portalA : portalB;
    }

    // Caches a transparent rotated snapshot for each normalized angle.
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
}
