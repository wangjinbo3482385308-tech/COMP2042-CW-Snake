package com.comp2042.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Owns portal spawning and traversal state.
// Keeps this feature independent from JavaFX.
public class PortalPair {
    public static final int SCORE_THRESHOLD = 100;
    public static final double SPAWN_INTERVAL_SECONDS = 10.0;
    public static final int COMPLETION_BONUS = 10;
    public static final int MIN_PORTAL_DISTANCE = 3;

    private final Random random = new Random();

    private Point portalA;
    private Point portalB;
    private boolean active;
    private boolean traversing;
    private int remainingSegments;
    private double cooldownSeconds = 0.0;

    // Advances portal spawning after the score threshold.
    // Selects only free board cells.
    // Prefers exits with a safe separation.
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

    // Places portals at deterministic test coordinates.
    // Rejects missing or identical coordinates.
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

    // Handles entry and tail progress after snake movement.
    // Returns true only when traversal has completed.
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

    // Clears all portal state for a fresh run.
    // The first eligible score can spawn immediately.
    public void reset() {
        portalA = null;
        portalB = null;
        active = false;
        traversing = false;
        remainingSegments = 0;
        cooldownSeconds = 0.0;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isTraversing() {
        return traversing;
    }

    public Point getPortalA() {
        return portalA == null ? null : new Point(portalA);
    }

    public Point getPortalB() {
        return portalB == null ? null : new Point(portalB);
    }

    public int getRemainingSegments() {
        return remainingSegments;
    }

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
