package com.comp2042.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Provides shared coin placement and scoring behavior.
// Subclasses override effects without changing the controller.
public class Coin {
    private Point pos;
    private final CoinType type;

    // Creates a standard red coin.
    public Coin(int x, int y, Snake snake, Obstacle obstacle) {
        this(x, y, snake, obstacle, CoinType.RED);
    }

    // Creates a coin with a model-only type identifier.
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

    // Awards the coin's base points.
    // Special coins extend this behavior.
    public void applyEffect(Game game, Snake snake, Obstacle obstacle) {
        game.addCoinPoints(getPoints());
    }

    public Point getPos() {
        return new Point(pos);
    }

    public int getPoints() {
        return 5;
    }

    public CoinType getType() {
        return type;
    }
}
