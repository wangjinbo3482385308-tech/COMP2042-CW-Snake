package com.comp2042.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Provides shared coin placement and scoring behavior on the board.
 * Subclasses override {@link #applyEffect(Game, Snake, Obstacle)} or {@link #getPoints()} for special coin effects.
 */
public class Coin {
    private Point pos;
    private final CoinType type;

    /**
     * Creates a standard red coin on a random unoccupied board cell.
     *
     * @param x board width in grid cells
     * @param y board height in grid cells
     * @param snake active snake occupying board cells
     * @param obstacle obstacle manager occupying board cells
     */
    public Coin(int x, int y, Snake snake, Obstacle obstacle) {
        this(x, y, snake, obstacle, CoinType.RED);
    }

    /**
     * Creates a coin of the specified type on a random unoccupied board cell.
     *
     * @param x board width in grid cells
     * @param y board height in grid cells
     * @param snake active snake occupying board cells
     * @param obstacle obstacle manager occupying board cells
     * @param type logical coin type used by the view layer
     */
    protected Coin(
            int x,
            int y,
            Snake snake,
            Obstacle obstacle,
            CoinType type
    ) {
        this.type = type;
        pos = findAvailablePosition(x, y, snake, obstacle);
    }

    // Collects every cell.
    // Selects only an unoccupied cell.
    private Point findAvailablePosition(
            int width,
            int height,
            Snake snake,
            Obstacle obstacle
    ) {
        List<Point> availableCells = new ArrayList<>();
        for (int column = 0; column < width; column++) {
            for (int row = 0; row < height; row++) {
                Point candidate = new Point(column, row);
                if (isAvailable(candidate, snake, obstacle)) {
                    availableCells.add(candidate);
                }
            }
        }

        // Handle an exhausted board safely.
        // Return an invalid coin position.
        if (availableCells.isEmpty()) {
            return new Point(-1, -1);
        }

        // Pick one free cell.
        // Avoid retry starvation.
        Random random = new Random();
        int index = random.nextInt(availableCells.size());
        return availableCells.get(index);
    }

    // Checks every board occupant before accepting a position.
    private boolean isAvailable(
            Point candidate,
            Snake snake,
            Obstacle obstacle
    ) {
        return !snake.getHead().equals(candidate)
                && !snake.getBody().contains(candidate)
                && !obstacle.getObstacles().contains(candidate);
    }

    /**
     * Applies this coin's gameplay effect by awarding its base point value.
     *
     * @param game active game model
     * @param snake active snake that collected the coin
     * @param obstacle obstacle manager for the board
     */
    public void applyEffect(Game game, Snake snake, Obstacle obstacle) {
        game.addCoinPoints(getPoints());
    }

    /**
     * Returns a defensive copy of the coin's grid position.
     *
     * @return coin coordinates, or {@code (-1, -1)} if no free cell existed
     */
    public Point getPos() {
        return new Point(pos);
    }

    /**
     * Returns the base score value awarded when this coin is collected.
     *
     * @return point value of this coin
     */
    public int getPoints() {
        return 5;
    }

    /**
     * Returns the logical type of this coin.
     *
     * @return coin variant identifier
     */
    public CoinType getType() {
        return type;
    }
}
