package com.comp2042.model;

import java.awt.Point;
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

    // Selects a free board cell within a bounded number of attempts.
    private Point findAvailablePosition(
            int width,
            int height,
            Snake snake,
            Obstacle obstacle
    ) {
        Random random = new Random();
        int maxAttempts = Math.max(1, width * height * 2);
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Point candidate = new Point(
                    random.nextInt(width),
                    random.nextInt(height)
            );
            if (isAvailable(candidate, snake, obstacle)) {
                return candidate;
            }
        }
        return new Point(-1, -1);
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
        game.addPoints(getPoints());
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
