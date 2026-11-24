package com.comp2042.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages paired portal spawning, head teleportation, and full-body traversal progress.
 * Remains independent of JavaFX rendering.
 */
public class PortalPair {
    /** Minimum score points required before portals can spawn. */
    public static final int SCORE_THRESHOLD = 100;
    /** Cooldown delay in seconds between a completed warp and the next portal spawn. */
    public static final double SPAWN_INTERVAL_SECONDS = 10.0;
    /** Flat bonus points awarded when the snake completes a portal traversal. */
    public static final int COMPLETION_BONUS = 10;
    /** Preferred minimum Manhattan distance in grid cells between paired portals. */
    public static final int MIN_PORTAL_DISTANCE = 3;

    private final Random random = new Random();

    private Point portalA;
    private Point portalB;
    private boolean active;
    private boolean traversing;
    private int remainingSegments;
    private double cooldownSeconds = 0.0;

    /**
     * Creates an inactive portal pair with zero initial spawn cooldown.
     */
    public PortalPair() {
    }

    /**
     * Advances the portal spawn cooldown and spawns a pair on free cells when eligible.
     * Prefers cell pairs separated by at least {@link #MIN_PORTAL_DISTANCE}.
     *
     * @param currentScorePoints player's current score points
     * @param deltaSeconds elapsed gameplay time in seconds
     * @param width board width in grid cells
     * @param height board height in grid cells
     * @param snake active snake occupying board cells
     * @param coin active coin occupying a board cell
     * @param obstacle obstacle manager occupying board cells
     * @return {@code true} if a new portal pair was spawned
     */
    public boolean updateSpawnTimer(
            int currentScorePoints,
            double deltaSeconds,
            int width,
            int height,
            Snake snake,
            Coin coin,
            Obstacle obstacle
    ) {
        if (currentScorePoints < SCORE_THRESHOLD || active) {
            return false;
        }

        if (deltaSeconds > 0.0) {
            cooldownSeconds = Math.max(
                    0.0,
                    cooldownSeconds - deltaSeconds
            );
        }

        if (cooldownSeconds > 0.0) {
            return false;
        }

        if (snake == null || coin == null || obstacle == null) {
            return false;
        }

        List<Point> freeCells = collectFreeCells(
                width,
                height,
                snake,
                coin,
                obstacle
        );
        if (freeCells.size() < 2) {
            return false;
        }

        selectPortalCells(freeCells);
        active = true;
        traversing = false;
        remainingSegments = 0;
        return true;
    }

    /**
     * Places the portal pair at explicit coordinates, typically for deterministic testing.
     *
     * @param a grid position of portal A
     * @param b grid position of portal B
     * @throws IllegalArgumentException if either coordinate is {@code null} or both are equal
     */
    public void spawnAt(Point a, Point b) {
        if (a == null || b == null || a.equals(b)) {
            throw new IllegalArgumentException(
                    "Portal coordinates must be distinct and non-null."
            );
        }

        portalA = new Point(a);
        portalB = new Point(b);
        active = true;
        traversing = false;
        remainingSegments = 0;
    }

    /**
     * Processes portal entry or ongoing tail traversal after the snake moves one cell.
     * Awards {@link #COMPLETION_BONUS} and clears the pair once the tail finishes exiting.
     *
     * @param game active game model receiving completion rewards
     * @param snake active snake that just moved
     * @param obstacle obstacle manager checked at the teleport destination
     * @param snakeGrew {@code true} if the snake grew on the current tick
     * @return {@code true} if the snake completed portal traversal on this tick
     */
    public boolean onSnakeMoved(
            Game game,
            Snake snake,
            Obstacle obstacle,
            boolean snakeGrew
    ) {
        if (!active || snake == null || game == null || snake.getDead()) {
            return false;
        }

        if (traversing) {
            if (!snakeGrew) {
                remainingSegments--;
            }

            if (remainingSegments <= 0) {
                completeTraversal(game);
                return true;
            }
            return false;
        }

        Point destination;
        Point head = snake.getHead();
        if (head.equals(portalA)) {
            destination = portalB;
        } else if (head.equals(portalB)) {
            destination = portalA;
        } else {
            return false;
        }

        snake.teleportHeadTo(
                destination,
                obstacle,
                game.getInventory()
        );
        if (snake.getDead()) {
            return false;
        }

        if (snake.getBody().isEmpty()) {
            completeTraversal(game);
            return true;
        }

        traversing = true;
        remainingSegments = snake.getBody().size();
        return false;
    }

    /**
     * Clears active portals, traversal progress, and spawn cooldown for a fresh run.
     */
    public void reset() {
        portalA = null;
        portalB = null;
        active = false;
        traversing = false;
        remainingSegments = 0;
        cooldownSeconds = 0.0;
    }

    /**
     * Reports whether a portal pair is currently active on the board.
     *
     * @return {@code true} if portals are active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Reports whether the snake is currently traversing the active portal pair.
     *
     * @return {@code true} while body segments are still passing through
     */
    public boolean isTraversing() {
        return traversing;
    }

    /**
     * Returns a defensive copy of portal A's coordinates.
     *
     * @return portal A position, or {@code null} when inactive
     */
    public Point getPortalA() {
        return portalA == null ? null : new Point(portalA);
    }

    /**
     * Returns a defensive copy of portal B's coordinates.
     *
     * @return portal B position, or {@code null} when inactive
     */
    public Point getPortalB() {
        return portalB == null ? null : new Point(portalB);
    }

    /**
     * Returns the number of body segments left to exit the portal.
     *
     * @return remaining body segments during traversal
     */
    public int getRemainingSegments() {
        return remainingSegments;
    }

    /**
     * Returns the remaining spawn cooldown before the next portal pair can appear.
     *
     * @return remaining cooldown in seconds
     */
    public double getCooldownSeconds() {
        return cooldownSeconds;
    }

    // Collects every unoccupied board cell.
    // Checks all moving and static occupants.
    private List<Point> collectFreeCells(
            int width,
            int height,
            Snake snake,
            Coin coin,
            Obstacle obstacle
    ) {
        List<Point> freeCells = new ArrayList<>();
        Point head = snake.getHead();
        List<Point> body = snake.getBody();
        Point coinPosition = coin.getPos();
        List<Point> obstacles = obstacle.getObstacles();

        for (int column = 0; column < width; column++) {
            for (int row = 0; row < height; row++) {
                Point candidate = new Point(column, row);
                if (candidate.equals(head)
                        || body.contains(candidate)
                        || candidate.equals(coinPosition)
                        || obstacles.contains(candidate)) {
                    continue;
                }
                freeCells.add(candidate);
            }
        }
        return freeCells;
    }

    // Selects random cells with a preferred separation.
    // Falls back when the board is too compact.
    private void selectPortalCells(List<Point> freeCells) {
        List<Point[]> distantPairs = new ArrayList<>();
        for (int firstIndex = 0;
                firstIndex < freeCells.size();
                firstIndex++) {
            for (int secondIndex = firstIndex + 1;
                    secondIndex < freeCells.size();
                    secondIndex++) {
                Point first = freeCells.get(firstIndex);
                Point second = freeCells.get(secondIndex);
                if (manhattanDistance(first, second)
                        >= MIN_PORTAL_DISTANCE) {
                    distantPairs.add(new Point[] {first, second});
                }
            }
        }

        if (!distantPairs.isEmpty()) {
            Point[] selected = distantPairs.get(
                    random.nextInt(distantPairs.size())
            );
            setPortals(selected[0], selected[1]);
            return;
        }

        int firstIndex = random.nextInt(freeCells.size());
        int secondIndex = random.nextInt(freeCells.size() - 1);
        if (secondIndex >= firstIndex) {
            secondIndex++;
        }
        setPortals(
                freeCells.get(firstIndex),
                freeCells.get(secondIndex)
        );
    }

    // Stores copied portal coordinates.
    // Prevents caller-side mutations.
    private void setPortals(Point a, Point b) {
        portalA = new Point(a);
        portalB = new Point(b);
    }

    // Measures grid separation.
    // Uses Manhattan distance.
    private int manhattanDistance(Point first, Point second) {
        return Math.abs(first.x - second.x)
                + Math.abs(first.y - second.y);
    }

    // Clears the pair after the tail completes traversal.
    // Starts the next spawn cooldown.
    private void completeTraversal(Game game) {
        active = false;
        traversing = false;
        portalA = null;
        portalB = null;
        remainingSegments = 0;
        cooldownSeconds = SPAWN_INTERVAL_SECONDS;
        game.addBonusPoints(COMPLETION_BONUS);
    }
}
