package com.comp2042.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Checks portal spawning and traversal.
// Uses only deterministic model APIs.
// Verifies scoring and cooldown boundaries.
@DisplayName("Portal pair model")
class PortalPairTest {
    private static final int WIDTH = 20;
    private static final int HEIGHT = 20;

    private Obstacle obstacle;
    private Game game;

    // Creates a clean game and obstacle board.
    // Uses a multiplied tier for bonus checks.
    // Keeps each test independent.
    @BeforeEach
    void setUp() {
        obstacle = new Obstacle(WIDTH, HEIGHT);
        game = new Game(WIDTH, HEIGHT, SpeedTier.FAST, false);
    }

    // Starts below the score threshold.
    // Checks valid free-cell spawning at exactly 100.
    @Test
    @DisplayName("Portals spawn only after the score threshold")
    void spawnsWithinFreeCellsAtThreshold() {
        Snake snake = new Snake(WIDTH, HEIGHT, false, obstacle);
        Coin coin = new Coin(WIDTH, HEIGHT, snake, obstacle);
        obstacle.newObstacles(snake, coin, 8);
        PortalPair pair = game.getPortalPair();

        assertFalse(pair.updateSpawnTimer(
                99,
                1.0,
                WIDTH,
                HEIGHT,
                snake,
                coin,
                obstacle
        ));
        assertFalse(pair.isActive());

        assertTrue(pair.updateSpawnTimer(
                100,
                0.0,
                WIDTH,
                HEIGHT,
                snake,
                coin,
                obstacle
        ));

        Point portalA = pair.getPortalA();
        Point portalB = pair.getPortalB();
        assertNotNull(portalA);
        assertNotNull(portalB);
        assertNotEquals(portalA, portalB);
        assertTrue(isInsideBoard(portalA));
        assertTrue(isInsideBoard(portalB));
        assertFalse(portalA.equals(snake.getHead()));
        assertFalse(portalB.equals(snake.getHead()));
        assertFalse(snake.getBody().contains(portalA));
        assertFalse(snake.getBody().contains(portalB));
        assertFalse(portalA.equals(coin.getPos()));
        assertFalse(portalB.equals(coin.getPos()));
        assertFalse(obstacle.getObstacles().contains(portalA));
        assertFalse(obstacle.getObstacles().contains(portalB));
        assertTrue(distance(portalA, portalB)
                >= PortalPair.MIN_PORTAL_DISTANCE);
        assertTrue(pair.isActive());
        assertFalse(pair.isTraversing());
        assertEquals(0, pair.getRemainingSegments());
    }

    // Builds a snake with two body segments.
    // Enters one side of a deterministic pair.
    @Test
    @DisplayName("Traversal completes after every body segment")
    void traversalCompletesAfterAllBodySegments() {
        Snake snake = snakeWithTwoBodySegments();
        PortalPair pair = game.getPortalPair();
        Point portalA = new Point(2, 2);
        Point portalB = new Point(15, 15);
        pair.spawnAt(portalA, portalB);
        snake.teleportHeadTo(
                portalA,
                obstacle,
                game.getInventory()
        );

        assertFalse(pair.onSnakeMoved(
                game,
                snake,
                obstacle,
                false
        ));
        assertEquals(portalB, snake.getHead());
        assertTrue(pair.isActive());
        assertTrue(pair.isTraversing());
        assertEquals(2, pair.getRemainingSegments());
        assertEquals(0, game.getScorePoints());
        assertEquals(0, game.getInventory().getShopPoints());

        assertFalse(advanceNormalFrame(snake, 'R'));
        assertEquals(1, pair.getRemainingSegments());
        assertTrue(pair.isActive());
        assertTrue(pair.isTraversing());
        assertEquals(0, game.getScorePoints());

        assertTrue(advanceNormalFrame(snake, 'R'));
        assertFalse(pair.isActive());
        assertFalse(pair.isTraversing());
        assertNull(pair.getPortalA());
        assertNull(pair.getPortalB());
        assertEquals(0, pair.getRemainingSegments());
        assertEquals(
                PortalPair.SPAWN_INTERVAL_SECONDS,
                pair.getCooldownSeconds()
        );
        assertEquals(
                PortalPair.COMPLETION_BONUS,
                game.getScorePoints()
        );
        assertEquals(
                PortalPair.COMPLETION_BONUS,
                game.getInventory().getShopPoints()
        );
    }

    // Grows the snake on the frame after entry.
    // Checks that growth does not advance the tail count.
    @Test
    @DisplayName("Growth during traversal delays completion")
    void growthDuringTraversalDoesNotFinishEarly() {
        Snake snake = snakeWithTwoBodySegments();
        PortalPair pair = game.getPortalPair();
        Point portalA = new Point(2, 2);
        Point portalB = new Point(15, 10);
        pair.spawnAt(portalA, portalB);
        snake.teleportHeadTo(
                portalA,
                obstacle,
                game.getInventory()
        );
        assertFalse(pair.onSnakeMoved(
                game,
                snake,
                obstacle,
                false
        ));
        assertEquals(2, pair.getRemainingSegments());

        Coin growthCoin = fixedCoin(
                new Point(portalB.x + 1, portalB.y),
                snake,
                obstacle
        );
        snake.move('R', game.getInventory());
        assertTrue(snake.ateCoin(growthCoin));
        assertEquals(3, snake.getBody().size());

        assertFalse(pair.onSnakeMoved(
                game,
                snake,
                obstacle,
                true
        ));
        assertTrue(pair.isActive());
        assertTrue(pair.isTraversing());
        assertEquals(2, pair.getRemainingSegments());
        assertEquals(0, game.getScorePoints());

        assertFalse(advanceNormalFrame(snake, 'R'));
        assertEquals(1, pair.getRemainingSegments());
        assertTrue(pair.isActive());
        assertTrue(pair.isTraversing());

        assertTrue(advanceNormalFrame(snake, 'R'));
        assertFalse(pair.isActive());
        assertEquals(10, game.getScorePoints());
        assertEquals(10, game.getInventory().getShopPoints());
    }

    // Starts a traversal with active portal state.
    // Expects reset to clear all related fields.
    @Test
    @DisplayName("Reset clears portal and traversal state")
    void resetClearsAllPortalState() {
        Snake snake = snakeWithTwoBodySegments();
        PortalPair pair = game.getPortalPair();
        Point portalA = new Point(3, 3);
        Point portalB = new Point(14, 14);
        pair.spawnAt(portalA, portalB);
        snake.teleportHeadTo(
                portalA,
                obstacle,
                game.getInventory()
        );
        assertFalse(pair.onSnakeMoved(
                game,
                snake,
                obstacle,
                false
        ));
        assertTrue(pair.isTraversing());

        pair.reset();

        assertFalse(pair.isActive());
        assertFalse(pair.isTraversing());
        assertNull(pair.getPortalA());
        assertNull(pair.getPortalB());
        assertEquals(0, pair.getRemainingSegments());
        assertEquals(0.0, pair.getCooldownSeconds());
    }

    // Limits the board to two adjacent free cells.
    // Expects a valid fallback pair.
    @Test
    @DisplayName("Spawn falls back when no distant pair exists")
    void spawnFallsBackOnCompactBoard() {
        Obstacle fixedObstacle = new FixedObstacle(
                2,
                2,
                List.of(new Point(1, 0))
        );
        Snake snake = new Snake(2, 2, false, fixedObstacle);
        Coin coin = fixedCoin(
                new Point(-1, -1),
                snake,
                fixedObstacle
        );
        PortalPair pair = new PortalPair();

        assertTrue(pair.updateSpawnTimer(
                100,
                0.0,
                2,
                2,
                snake,
                coin,
                fixedObstacle
        ));
        assertNotEquals(pair.getPortalA(), pair.getPortalB());
        assertTrue(distance(pair.getPortalA(), pair.getPortalB())
                < PortalPair.MIN_PORTAL_DISTANCE);
    }

    // Supplies invalid deterministic coordinates.
    // Expects placement validation to reject each case.
    @Test
    @DisplayName("Deterministic spawn rejects invalid coordinates")
    void spawnAtRejectsInvalidCoordinates() {
        PortalPair pair = new PortalPair();
        Point first = new Point(1, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> pair.spawnAt(null, first)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> pair.spawnAt(first, null)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> pair.spawnAt(first, new Point(first))
        );
    }

    // Repeatedly spawns portals on Arena and Pillars maps.
    // Expects every endpoint to sit on an open cell with four unobstructed exits.
    @Test
    @DisplayName("Portals on Arena and Pillars avoid corners and wall-adjacent trap cells")
    void portalsOnPresetMapsAvoidWallsAndCorners() {
        for (LevelMap levelMap : List.of(LevelMap.PILLARS, LevelMap.ARENA)) {
            Obstacle levelObstacle = new Obstacle(WIDTH, HEIGHT, levelMap);
            List<Point> wallPoints = levelObstacle.getObstacles();
            Snake snake = new Snake(WIDTH, HEIGHT, false, levelObstacle);
            Coin coin = new Coin(WIDTH, HEIGHT, snake, levelObstacle);

            for (int trial = 0; trial < 60; trial++) {
                PortalPair pair = new PortalPair();
                assertTrue(pair.updateSpawnTimer(
                        100,
                        0.0,
                        WIDTH,
                        HEIGHT,
                        snake,
                        coin,
                        levelObstacle
                ));

                assertOpenFourExits(pair.getPortalA(), wallPoints);
                assertOpenFourExits(pair.getPortalB(), wallPoints);
                assertTrue(distance(pair.getPortalA(), pair.getPortalB())
                        >= PortalPair.MIN_PORTAL_DISTANCE);
            }
        }
    }

    // Verifies a portal cell is away from board edges and all orthogonal walls.
    private static void assertOpenFourExits(Point cell, List<Point> walls) {
        assertTrue(cell.x > 0 && cell.x < WIDTH - 1);
        assertTrue(cell.y > 0 && cell.y < HEIGHT - 1);
        assertFalse(walls.contains(new Point(cell.x + 1, cell.y)));
        assertFalse(walls.contains(new Point(cell.x - 1, cell.y)));
        assertFalse(walls.contains(new Point(cell.x, cell.y + 1)));
        assertFalse(walls.contains(new Point(cell.x, cell.y - 1)));
    }

    // Grows a snake once through a coin.
    // Returns one head and two body segments.
    private Snake snakeWithTwoBodySegments() {
        Obstacle snakeObstacle = new Obstacle(WIDTH, HEIGHT);
        Snake snake = new Snake(
                WIDTH,
                HEIGHT,
                false,
                snakeObstacle
        );
        Coin growthCoin = fixedCoin(
                new Point(WIDTH / 2, HEIGHT / 2 - 1),
                snake,
                snakeObstacle
        );

        snake.move('U');
        assertTrue(snake.ateCoin(growthCoin));
        assertEquals(2, snake.getBody().size());
        return snake;
    }

    // Moves one frame without growth.
    // Reports traversal completion.
    private boolean advanceNormalFrame(
            Snake snake,
            char direction
    ) {
        snake.move(direction, game.getInventory());
        assertFalse(snake.getDead());

        Coin missedCoin = fixedCoin(
                new Point(-1, -1),
                snake,
                obstacle
        );
        assertFalse(snake.ateCoin(missedCoin));
        return game.getPortalPair().onSnakeMoved(
                game,
                snake,
                obstacle,
                false
        );
    }

    // Creates a coin at an exact coordinate.
    // Keeps random generation out of assertions.
    private static Coin fixedCoin(
            Point position,
            Snake snake,
            Obstacle boardObstacle
    ) {
        return new FixedCoin(
                WIDTH,
                HEIGHT,
                snake,
                boardObstacle,
                position
        );
    }

    // Checks a point against board limits.
    // Requires both axes to be valid.
    private static boolean isInsideBoard(Point point) {
        return point.x >= 0
                && point.x < WIDTH
                && point.y >= 0
                && point.y < HEIGHT;
    }

    // Measures grid separation.
    // Uses Manhattan distance.
    private static int distance(Point first, Point second) {
        return Math.abs(first.x - second.x)
                + Math.abs(first.y - second.y);
    }

    // Supplies controlled obstacle coordinates.
    // Avoids random placement in compact-board tests.
    private static final class FixedObstacle extends Obstacle {
        private final List<Point> positions;

        private FixedObstacle(
                int width,
                int height,
                List<Point> positions
        ) {
            super(width, height);
            this.positions = List.copyOf(positions);
        }

        @Override
        public List<Point> getObstacles() {
            return List.copyOf(positions);
        }
    }

    // Supplies a controlled coin coordinate.
    // Avoids random placement in traversal tests.
    private static final class FixedCoin extends Coin {
        private final Point position;

        private FixedCoin(
                int width,
                int height,
                Snake snake,
                Obstacle boardObstacle,
                Point position
        ) {
            super(width, height, snake, boardObstacle);
            this.position = new Point(position);
        }

        @Override
        public Point getPos() {
            return new Point(position);
        }
    }
}
