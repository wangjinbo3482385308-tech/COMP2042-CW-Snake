package com.comp2042.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Owns obstacle positions and generation.
// Rendering colors are provided by the view layer.
public class Obstacle {
    private static final int MAX_OBSTACLES = 50;

    private final int width;
    private final int height;
    private final List<Point> obstacles;
    private final Random random;

    public Obstacle(int x, int y) {
        width = x;
        height = y;
        obstacles = new ArrayList<>();
        random = new Random();
    }

    // Adds only free obstacle cells.
    // Bounds every search to avoid an infinite loop.
    public void newObstacles(Snake snake, Coin coin, int amount) {
        int maxAttempts = Math.max(1, width * height * 4);

        for (int index = 0; index < amount; index++) {
            if (obstacles.size() >= MAX_OBSTACLES) {
                return;
            }

            Point candidate = null;
            for (int attempt = 0; attempt < maxAttempts; attempt++) {
                Point possible = new Point(
                        random.nextInt(width),
                        random.nextInt(height)
                );
                if (isAvailable(possible, snake, coin)) {
                    candidate = possible;
                    break;
                }
            }

            if (candidate == null) {
                return;
            }
            obstacles.add(candidate);
        }
    }

    // Checks board occupants before adding a cell.
    private boolean isAvailable(Point point, Snake snake, Coin coin) {
        return !obstacles.contains(point)
                && !snake.getHead().equals(point)
                && !snake.getBody().contains(point)
                && !coin.getPos().equals(point);
    }

    public List<Point> getObstacles() {
        return new ArrayList<>(obstacles);
    }

    // Removes every obstacle at one cell.
    // Reports whether the board changed.
    public boolean removeObstacleAt(Point point) {
        if (point == null) {
            return false;
        }

        return obstacles.removeIf(point::equals);
    }

    // Clears a square area around one center.
    // Includes cells exactly on the radius.
    public void clearAreaAround(Point center, int radius) {
        if (center == null || radius < 0) {
            return;
        }

        obstacles.removeIf(point -> Math.abs(point.x - center.x) <= radius
                && Math.abs(point.y - center.y) <= radius);
    }

    // Clears all obstacle cells for a new run.
    public void reset() {
        obstacles.clear();
    }
}
