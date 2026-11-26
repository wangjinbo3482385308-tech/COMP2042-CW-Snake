package com.comp2042.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages obstacle positions, generation, and clearing on the game board.
 */
public class Obstacle {
    private static final int MAX_OBSTACLES = 50;

    private final int width;
    private final int height;
    private final List<Point> obstacles;
    private final Random random;

    /**
     * Creates an empty obstacle manager for a board of the given dimensions.
     *
     * @param x board width in grid cells
     * @param y board height in grid cells
     */
    public Obstacle(int x, int y) {
        width = x;
        height = y;
        obstacles = new ArrayList<>();
        random = new Random();
    }

    /**
     * Spawns up to the requested number of obstacles on unoccupied cells.
     * Bounds search attempts per obstacle and stops at the board obstacle cap.
     *
     * @param snake active snake occupying board cells
     * @param coin active coin occupying a board cell
     * @param amount maximum number of new obstacles to place
     */
    public void newObstacles(Snake snake, Coin coin, int amount) {
        newObstacles(snake, coin, amount, null);
    }

    /**
     * Spawns up to the requested number of obstacles on unoccupied cells while avoiding active portals.
     * Bounds search attempts per obstacle and stops at the board obstacle cap.
     *
     * @param snake active snake occupying board cells
     * @param coin active coin occupying a board cell
     * @param amount maximum number of new obstacles to place
     * @param portalPair active portal pair to avoid, or {@code null} when unused
     */
    public void newObstacles(
            Snake snake,
            Coin coin,
            int amount,
            PortalPair portalPair
    ) {
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
                if (isAvailable(possible, snake, coin, portalPair)) {
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

    // Checks board occupants and active portals before adding a cell.
    private boolean isAvailable(
            Point point,
            Snake snake,
            Coin coin,
            PortalPair portalPair
    ) {
        return !obstacles.contains(point)
                && !snake.getHead().equals(point)
                && !snake.getBody().contains(point)
                && !coin.getPos().equals(point)
                && !isPortalCell(point, portalPair);
    }

    // Reports whether a cell is occupied by an active portal endpoint.
    private boolean isPortalCell(Point point, PortalPair portalPair) {
        if (portalPair == null || !portalPair.isActive()) {
            return false;
        }
        return point.equals(portalPair.getPortalA())
                || point.equals(portalPair.getPortalB());
    }

    /**
     * Returns a copy of all active obstacle coordinates.
     *
     * @return list of obstacle cell positions
     */
    public List<Point> getObstacles() {
        return new ArrayList<>(obstacles);
    }

    /**
     * Removes any obstacle occupying the specified grid cell.
     *
     * @param point target cell coordinate
     * @return {@code true} if at least one obstacle was removed
     */
    public boolean removeObstacleAt(Point point) {
        if (point == null) {
            return false;
        }

        return obstacles.removeIf(point::equals);
    }

    /**
     * Removes all obstacles within an inclusive square radius around a center cell.
     *
     * @param center center grid cell
     * @param radius Chebyshev distance in cells to clear
     */
    public void clearAreaAround(Point center, int radius) {
        if (center == null || radius < 0) {
            return;
        }

        obstacles.removeIf(point -> Math.abs(point.x - center.x) <= radius
                && Math.abs(point.y - center.y) <= radius);
    }

    /**
     * Clears all obstacles for a new game run.
     */
    public void reset() {
        obstacles.clear();
    }
}
