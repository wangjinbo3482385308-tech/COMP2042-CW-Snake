package com.comp2042.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Checks obstacle placement and lifecycle behavior.
// Verifies bounds and occupied-cell avoidance.
// Keeps repeated generation within its limit.
@DisplayName("Obstacle model")
class ObstacleTest {
    private static final int WIDTH = 20;
    private static final int HEIGHT = 20;

    private Snake snake;
    private Coin coin;
    private Obstacle obstacle;

    // Creates a fresh board and occupants.
    // Shares one obstacle model per test.
    @BeforeEach
    void setUp() {
        obstacle = new Obstacle(WIDTH, HEIGHT);
        snake = new Snake(WIDTH, HEIGHT, false, obstacle);
        coin = new Coin(WIDTH, HEIGHT, snake, obstacle);
    }

    // Generates a group of obstacles.
    // Expects every coordinate inside the board.
    @Test
    @DisplayName("Generated obstacles stay inside the board")
    void generatedObstaclesStayInsideBoard() {
        obstacle.newObstacles(snake, coin, 30);

        assertFalse(obstacle.getObstacles().isEmpty());
        for (Point point : obstacle.getObstacles()) {
            assertTrue(point.x >= 0);
            assertTrue(point.x < WIDTH);
            assertTrue(point.y >= 0);
            assertTrue(point.y < HEIGHT);
        }
    }

    // Generates a group on an occupied board.
    // Expects unique free cells only.
    @Test
    @DisplayName("Generated obstacles avoid all occupied cells")
    void generatedObstaclesAvoidOccupiedCells() {
        obstacle.newObstacles(snake, coin, 30);
        List<Point> generated = obstacle.getObstacles();
        Set<Point> uniquePoints = new HashSet<>(generated);

        assertEquals(generated.size(), uniquePoints.size());
        assertFalse(generated.contains(snake.getHead()));
        assertFalse(generated.contains(coin.getPos()));
        for (Point bodyPoint : snake.getBody()) {
            assertFalse(generated.contains(bodyPoint));
        }
    }

    // Fills obstacles across multiple calls.
    // Expects the limit and reset contract.
    @Test
    @DisplayName("Repeated generation respects the cap and reset clears the board")
    void repeatedGenerationRespectsCapAndResetClears() {
        obstacle.newObstacles(snake, coin, 30);
        int firstCount = obstacle.getObstacles().size();
        assertTrue(firstCount > 0);

        obstacle.newObstacles(snake, coin, 100);

        assertTrue(obstacle.getObstacles().size() >= firstCount);
        assertTrue(obstacle.getObstacles().size() <= 50);

        obstacle.reset();

        assertTrue(obstacle.getObstacles().isEmpty());
    }
}
