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
    private final List<Point> baseObstacles;
    private final List<Point> obstacles;
    private final List<Point> dynamicObstacles;
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
        baseObstacles = new ArrayList<>();
        obstacles = new ArrayList<>();
        dynamicObstacles = new ArrayList<>();
        random = new Random();
    }

    /**
     * Creates an obstacle manager preloaded with the static layout of the supplied level map.
     *
     * @param x board width in grid cells
     * @param y board height in grid cells
     * @param levelMap preset level map to load, or {@code null} for {@link LevelMap#CLASSIC}
     */
    public Obstacle(int x, int y, LevelMap levelMap) {
        this(x, y);
        applyLevelMap(levelMap);
    }

    /**
     * Loads a preset level map's static obstacles as the baseline layout and clears dynamic obstacles.
     * Defaults to {@link LevelMap#CLASSIC} when {@code levelMap} is {@code null}.
     *
     * @param levelMap preset level map to apply, or {@code null} for {@link LevelMap#CLASSIC}
     */
    public void applyLevelMap(LevelMap levelMap) {
        LevelMap resolvedMap = levelMap == null ? LevelMap.CLASSIC : levelMap;
        baseObstacles.clear();
        for (Point point : resolvedMap.buildObstacles(width, height)) {
            baseObstacles.add(new Point(point));
        }
        reset();
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
            dynamicObstacles.add(new Point(candidate));
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
        List<Point> copy = new ArrayList<>(obstacles.size());
        for (Point point : obstacles) {
            copy.add(new Point(point));
        }
        return copy;
    }

    /**
     * Reports whether any dynamically generated obstacles are currently active on the board.
     *
     * @return {@code true} if at least one dynamic obstacle is present
     */
    public boolean hasDynamicObstacles() {
        return !dynamicObstacles.isEmpty();
    }

    /**
     * Returns the number of dynamically generated obstacles currently active on the board.
     *
     * @return active dynamic obstacle count
     */
    public int getDynamicObstacleCount() {
        return dynamicObstacles.size();
    }

    /**
     * Returns the number of static preset obstacles configured for the active level map.
     *
     * @return preset level obstacle count
     */
    public int getBaseObstacleCount() {
        return baseObstacles.size();
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

        dynamicObstacles.removeIf(point::equals);
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

        dynamicObstacles.removeIf(point -> Math.abs(point.x - center.x) <= radius
                && Math.abs(point.y - center.y) <= radius);
        obstacles.removeIf(point -> Math.abs(point.x - center.x) <= radius
                && Math.abs(point.y - center.y) <= radius);
    }

    /**
     * Clears all dynamically generated obstacles and restores the current level's preset obstacles.
     */
    public void reset() {
        obstacles.clear();
        dynamicObstacles.clear();
        for (Point point : baseObstacles) {
            obstacles.add(new Point(point));
        }
    }
}
