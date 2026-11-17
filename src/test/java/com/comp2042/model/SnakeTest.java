package com.comp2042.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Checks snake movement and collision behavior.
// Exercises borders and body state.
// Uses only the public model API.
@DisplayName("Snake model")
class SnakeTest {
    private static final int WIDTH = 20;
    private static final int HEIGHT = 20;

    private Obstacle obstacle;
    private Snake snake;

    // Creates an empty board for each test.
    // Starts with a closed-border snake.
    @BeforeEach
    void setUp() {
        obstacle = new Obstacle(WIDTH, HEIGHT);
        snake = new Snake(WIDTH, HEIGHT, false, obstacle);
    }

    // Starts from a centered head.
    // Expects one initial tail segment.
    @Test
    @DisplayName("Initial snake position and direction are correct")
    void initialSnakeUsesCenteredHeadAndSingleTail() {
        assertEquals(new Point(WIDTH / 2, HEIGHT / 2), snake.getHead());
        assertEquals(
                List.of(new Point(WIDTH / 2 + 1, HEIGHT / 2)),
                snake.getBody()
        );
        assertEquals(270, snake.getSnakeDir());
        assertFalse(snake.getDead());
    }

    // Uses a fresh snake for each direction.
    // Expects the matching head coordinate.
    @Test
    @DisplayName("The snake moves in all four directions")
    void movementSupportsAllFourDirections() {
        assertSingleMove('U', new Point(10, 9), 0);
        assertSingleMove('D', new Point(10, 11), 180);
        assertSingleMove('L', new Point(9, 10), 270);
        assertSingleMove('R', new Point(11, 10), 90);
    }

    // Compares an eaten coin with a missed coin.
    // Expects eating to retain the new segment.
    @Test
    @DisplayName("Eating a coin grows the snake by one segment")
    void eatingCoinRetainsTheNewBodySegment() {
        Snake growingSnake = newSnake(false, obstacle);
        Coin food = fixedCoin(new Point(11, 10), growingSnake, obstacle);

        growingSnake.move('R');
        assertTrue(growingSnake.ateCoin(food));
        assertEquals(2, growingSnake.getBody().size());

        Snake movingSnake = newSnake(false, obstacle);
        Coin missedFood = fixedCoin(new Point(-1, -1), movingSnake, obstacle);
        movingSnake.move('R');
        assertFalse(movingSnake.ateCoin(missedFood));
        assertEquals(1, movingSnake.getBody().size());
    }

    // Moves the head onto the left board edge.
    // Expects the next move to cause death.
    @Test
    @DisplayName("Closed borders reject entry beyond the board")
    void closedBorderRejectsHeadBeyondBoard() {
        moveRepeatedly(snake, 'L', 10);
        assertEquals(new Point(0, 10), snake.getHead());
        int bodySizeBeforeDeath = snake.getBody().size();

        snake.move('L');

        assertTrue(snake.getDead());
        assertEquals(new Point(0, 10), snake.getHead());
        assertEquals(bodySizeBeforeDeath, snake.getBody().size());
    }

    // Crosses each edge with the constructor option.
    // Expects coordinates on the opposite edge.
    @Test
    @DisplayName("No-border mode wraps all four edges")
    void noBorderModeWrapsAcrossAllFourEdges() {
        assertEdgeWrap('U', new Point(10, 19), true, false);
        assertEdgeWrap('D', new Point(10, 0), true, false);
        assertEdgeWrap('L', new Point(19, 10), true, false);
        assertEdgeWrap('R', new Point(0, 10), true, false);
    }

    // Enables wrapping after construction.
    // Expects the same four edge coordinates.
    @Test
    @DisplayName("Temporary border-passing mode wraps all four edges")
    void temporaryBorderModeWrapsAcrossAllFourEdges() {
        assertEdgeWrap('U', new Point(10, 19), false, true);
        assertEdgeWrap('D', new Point(10, 0), false, true);
        assertEdgeWrap('L', new Point(19, 10), false, true);
        assertEdgeWrap('R', new Point(0, 10), false, true);
    }

    // Places an obstacle on the wrapped destination.
    // Expects the wrapped head to be dead.
    @Test
    @DisplayName("Wrapping into an obstacle causes death")
    void wrapIntoObstacleCausesDeath() {
        FixedObstacle blockedObstacle = new FixedObstacle(
                WIDTH,
                HEIGHT,
                List.of(new Point(19, 10))
        );
        Snake wrappingSnake = newSnake(true, blockedObstacle);

        moveRepeatedly(wrappingSnake, 'L', 11);

        assertTrue(wrappingSnake.getDead());
        assertEquals(new Point(19, 10), wrappingSnake.getHead());
    }

    // Builds a four-segment snake.
    // Expects a reversal to die without growth.
    @Test
    @DisplayName("A short snake dies on self-collision")
    void shortSnakeDiesAndRollsBackBody() {
        Snake shortSnake = newSnake(false, obstacle);
        growRight(shortSnake, 3);
        int lengthBeforeDeath = shortSnake.getBody().size();
        assertEquals(4, lengthBeforeDeath);

        shortSnake.move('L');

        assertTrue(shortSnake.getDead());
        assertEquals(lengthBeforeDeath, shortSnake.getBody().size());
    }

    // Builds an eight-segment snake.
    // Expects a reversal to die without growth.
    @Test
    @DisplayName("A long snake dies on self-collision")
    void longSnakeDiesAndRollsBackBody() {
        Snake longSnake = newSnake(false, obstacle);
        growRight(longSnake, 7);
        int lengthBeforeDeath = longSnake.getBody().size();
        assertEquals(8, lengthBeforeDeath);

        longSnake.move('L');

        assertTrue(longSnake.getDead());
        assertEquals(lengthBeforeDeath, longSnake.getBody().size());
    }

    // Moves a fresh snake once.
    // Checks its head and direction.
    private void assertSingleMove(char direction, Point expectedHead, int expectedDirection) {
        Obstacle boardObstacle = new Obstacle(WIDTH, HEIGHT);
        Snake movingSnake = newSnake(false, boardObstacle);

        movingSnake.move(direction);

        assertEquals(expectedHead, movingSnake.getHead());
        assertEquals(expectedDirection, movingSnake.getSnakeDir());
        assertFalse(movingSnake.getDead());
    }

    // Moves to one edge and crosses it.
    // Checks the opposite-edge coordinate.
    private void assertEdgeWrap(
            char direction,
            Point expectedHead,
            boolean noBorder,
            boolean temporaryWrap
    ) {
        Obstacle boardObstacle = new Obstacle(WIDTH, HEIGHT);
        Snake wrappingSnake = newSnake(noBorder, boardObstacle);
        if (temporaryWrap) {
            wrappingSnake.ignoreBorders(true);
        }

        int moveCount = direction == 'D' || direction == 'R' ? 10 : 11;
        moveRepeatedly(wrappingSnake, direction, moveCount);

        assertEquals(expectedHead, wrappingSnake.getHead());
        assertFalse(wrappingSnake.getDead());
    }

    // Repeats one movement command.
    // Keeps the test setup concise.
    private static void moveRepeatedly(Snake target, char direction, int count) {
        for (int index = 0; index < count; index++) {
            target.move(direction);
        }
    }

    // Grows a snake with consecutive right moves.
    // Places food at every new head cell.
    private void growRight(Snake target, int foodCount) {
        for (int index = 0; index < foodCount; index++) {
            Point targetHead = new Point(
                    target.getHead().x + 1,
                    target.getHead().y
            );
            Coin food = fixedCoin(targetHead, target, obstacle);

            target.move('R');

            assertTrue(target.ateCoin(food));
        }
    }

    // Creates a snake with the requested border mode.
    // Reuses the selected obstacle board.
    private static Snake newSnake(boolean noBorder, Obstacle boardObstacle) {
        return new Snake(WIDTH, HEIGHT, noBorder, boardObstacle);
    }

    // Creates a coin at an exact test coordinate.
    // Avoids random placement in movement tests.
    private static Coin fixedCoin(Point position, Snake target, Obstacle boardObstacle) {
        return new FixedCoin(WIDTH, HEIGHT, target, boardObstacle, position);
    }

    // Supplies a controlled obstacle list.
    // Keeps collision tests deterministic.
    private static final class FixedObstacle extends Obstacle {
        private final List<Point> positions;

        private FixedObstacle(int width, int height, List<Point> positions) {
            super(width, height);
            this.positions = List.copyOf(positions);
        }

        @Override
        public List<Point> getObstacles() {
            return List.copyOf(positions);
        }
    }

    // Supplies a controlled coin coordinate.
    // Avoids changes to production APIs.
    private static final class FixedCoin extends Coin {
        private final Point position;

        private FixedCoin(
                int width,
                int height,
                Snake target,
                Obstacle boardObstacle,
                Point position
        ) {
            super(width, height, target, boardObstacle);
            this.position = new Point(position);
        }

        @Override
        public Point getPos() {
            return new Point(position);
        }
    }
}
