package com.comp2042.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Checks shop purchases and power-up integrations.
// Exercises model behavior without the UI.
@DisplayName("Player inventory model")
class PlayerInventoryTest {
    private static final int WIDTH = 20;
    private static final int HEIGHT = 20;

    private Game game;
    private PlayerInventory inventory;
    private Obstacle obstacle;
    private Snake snake;

    // Creates fresh model objects for each test.
    // Uses the legacy scoring branch by default.
    @BeforeEach
    void setUp() {
        game = new Game(WIDTH, HEIGHT, false, 'N');
        inventory = game.getInventory();
        obstacle = new Obstacle(WIDTH, HEIGHT);
        snake = new Snake(WIDTH, HEIGHT, false, obstacle);
    }

    // Applies a normal coin collection.
    // Keeps shop spending separate from game scores.
    @Test
    @DisplayName("Coin earnings fund the shop without score deductions")
    void coinEarningsFundShopWithoutChangingScores() {
        Coin coin = new Coin(WIDTH, HEIGHT, snake, obstacle);
        coin.applyEffect(game, snake, obstacle);
        game.incScore();

        assertEquals(coin.getPoints(), game.getScorePoints());
        assertEquals(coin.getPoints(), inventory.getShopPoints());

        int pointsBeforeSpending = game.getScorePoints();
        int scoreBeforeSpending = scoreOf(game);
        inventory.addShopPoints(200);

        assertTrue(inventory.buyShield());
        assertTrue(inventory.buyMagnet());
        assertTrue(inventory.buyReviveStone());

        assertEquals(pointsBeforeSpending, game.getScorePoints());
        assertEquals(scoreBeforeSpending, scoreOf(game));
        assertEquals(80, inventory.getShopPoints());
    }

    // Applies the earned tier multiplier.
    // Credits the same final gain to the shop.
    @Test
    @DisplayName("Tier bonuses add matching shop points")
    void tierBonusesAddMatchingShopPoints() {
        Game tieredGame = new Game(
                WIDTH,
                HEIGHT,
                SpeedTier.NORMAL,
                false
        );

        tieredGame.addPoints(10);

        assertEquals(15, tieredGame.getScorePoints());
        assertEquals(15, tieredGame.getInventory().getShopPoints());
    }

    // Buys shields up to the purchase cap.
    // Rejects every later shield purchase.
    @Test
    @DisplayName("Shield purchases stop at the per-run limit")
    void shieldPurchasesStopAtLimit() {
        inventory.addShopPoints(60);

        assertTrue(inventory.buyShield());
        assertTrue(inventory.buyShield());
        assertTrue(inventory.buyShield());
        assertFalse(inventory.buyShield());

        assertEquals(3, inventory.getShieldCharges());
        assertEquals(3, inventory.getShieldPurchases());
        assertEquals(15, inventory.getShopPoints());
    }

    // Protects a snake at a closed wall edge.
    // Places the head on the opposite edge.
    @Test
    @DisplayName("A shield prevents wall death and wraps the head")
    void shieldPreventsWallDeath() {
        inventory.addShopPoints(15);
        assertTrue(inventory.buyShield());

        moveRepeatedly(snake, 'L', 11, inventory);

        assertFalse(snake.getDead());
        assertEquals(new Point(WIDTH - 1, HEIGHT / 2), snake.getHead());
        assertEquals(0, inventory.getShieldCharges());
    }

    // Places an obstacle directly ahead.
    // Checks protected and unprotected movement.
    @Test
    @DisplayName("A shield destroys an obstacle that would cause death")
    void shieldDestroysObstacle() {
        Point obstacleCell = new Point(WIDTH / 2 + 1, HEIGHT / 2);
        setObstacles(obstacle, List.of(obstacleCell));
        inventory.addShopPoints(15);
        assertTrue(inventory.buyShield());

        snake.move('R', inventory);

        assertFalse(snake.getDead());
        assertEquals(obstacleCell, snake.getHead());
        assertFalse(obstacle.getObstacles().contains(obstacleCell));
        assertEquals(0, inventory.getShieldCharges());

        Obstacle unprotectedBoard = new Obstacle(WIDTH, HEIGHT);
        setObstacles(unprotectedBoard, List.of(obstacleCell));
        Snake unprotectedSnake = new Snake(
                WIDTH,
                HEIGHT,
                false,
                unprotectedBoard
        );

        unprotectedSnake.move('R', null);

        assertTrue(unprotectedSnake.getDead());
        assertEquals(new Point(WIDTH / 2, HEIGHT / 2), unprotectedSnake.getHead());
    }

    // Moves a shielded snake directly into its own body.
    // Verifies full state rollback and single shield consumption.
    @Test
    @DisplayName("A shield blocks self-collision and rolls back movement")
    void shieldBlocksSelfCollisionAndRollsBackState() {
        growSnake(snake, 3);
        inventory.addShopPoints(PowerUpType.SHIELD.getCost());
        assertTrue(inventory.buyShield());

        Point previousHead = snake.getHead();
        int previousDir = snake.getSnakeDir();
        int previousBodySize = snake.getBody().size();
        assertEquals(4, previousBodySize);

        boolean moved = snake.move('L', inventory);

        assertFalse(moved);
        assertFalse(snake.getDead());
        assertEquals(previousHead, snake.getHead());
        assertEquals(previousDir, snake.getSnakeDir());
        assertEquals(previousBodySize, snake.getBody().size());
        assertEquals(0, inventory.getShieldCharges());
    }

    // Checks the five-by-five magnet boundary.
    // Runs the countdown to complete expiry.
    @Test
    @DisplayName("Magnet range and duration follow the shop contract")
    void magnetRangeAndDurationFollowContract() {
        inventory.addShopPoints(20);
        Point head = new Point(10, 10);

        assertTrue(inventory.buyMagnet());
        assertFalse(inventory.buyMagnet());
        assertTrue(inventory.isWithinMagnetRange(head, new Point(12, 12)));
        assertTrue(inventory.isWithinMagnetRange(head, new Point(8, 8)));
        assertFalse(inventory.isWithinMagnetRange(head, new Point(13, 10)));
        assertFalse(inventory.isWithinMagnetRange(head, new Point(10, 13)));

        inventory.tickMagnet(9.5);
        assertTrue(inventory.isMagnetActive());
        inventory.tickMagnet(0.5);

        assertEquals(0.0, inventory.getMagnetTimeRemaining());
        assertFalse(inventory.isMagnetActive());
        assertFalse(inventory.isWithinMagnetRange(head, new Point(11, 10)));
    }

    // Buys the single revive stone.
    // Blocks reuse after its first consumption.
    @Test
    @DisplayName("Revive stone uses one purchase and one consumption")
    void reviveStoneUsesSinglePurchaseAndConsumption() {
        inventory.addShopPoints(250);

        assertTrue(inventory.buyReviveStone());
        assertFalse(inventory.buyReviveStone());
        assertTrue(inventory.hasReviveStone());
        assertFalse(inventory.hasUsedReviveStone());
        assertEquals(150, inventory.getShopPoints());

        assertTrue(inventory.consumeReviveStone());
        assertFalse(inventory.hasReviveStone());
        assertTrue(inventory.hasUsedReviveStone());
        assertFalse(inventory.consumeReviveStone());
        assertFalse(inventory.buyReviveStone());
        assertEquals(150, inventory.getShopPoints());
    }

    // Creates a long dead snake.
    // Surrounds the center with obstacles.
    @Test
    @DisplayName("Revival centers and shortens the snake safely")
    void revivalCentersAndShortensSnakeSafely() {
        growSnake(snake, 7);
        snake.move('L');
        assertTrue(snake.getDead());
        assertEquals(8, snake.getBody().size());

        List<Point> obstacles = List.of(
                new Point(10, 10),
                new Point(9, 9),
                new Point(11, 11),
                new Point(8, 10),
                new Point(12, 10),
                new Point(13, 10),
                new Point(14, 10)
        );
        setObstacles(obstacle, obstacles);

        snake.reviveAtCenter(WIDTH, HEIGHT, obstacle);

        Point center = new Point(WIDTH / 2, HEIGHT / 2);
        List<Point> expectedBody = List.of(
                new Point(14, 10),
                new Point(13, 10),
                new Point(12, 10),
                new Point(11, 10)
        );
        assertEquals(center, snake.getHead());
        assertFalse(snake.getDead());
        assertEquals(expectedBody, snake.getBody());
        assertFalse(snake.hitSelf());
        for (Point segment : expectedBody) {
            assertFalse(obstacle.getObstacles().contains(segment));
        }
        assertEquals(List.of(new Point(8, 10)), obstacle.getObstacles());
    }

    // Revives short snakes with one or two body segments.
    // Expects at least one segment and a clear three-by-three center.
    @Test
    @DisplayName("Revival keeps at least one body segment for short snakes")
    void revivalPreservesMinimumBodyAndClearsCenterAreaForShortSnakes() {
        assertShortSnakeRevival(0);
        assertShortSnakeRevival(1);
    }

    // Runs one short-snake revival check after the requested growth.
    // Verifies center respawn, minimum body size, and three-by-three clearing.
    private void assertShortSnakeRevival(int growthSteps) {
        Obstacle boardObstacle = new Obstacle(WIDTH, HEIGHT);
        Snake shortSnake = new Snake(WIDTH, HEIGHT, false, boardObstacle);
        for (int index = 0; index < growthSteps; index++) {
            Point nextHead = new Point(
                    shortSnake.getHead().x + 1,
                    shortSnake.getHead().y
            );
            Coin food = new FixedCoin(
                    WIDTH,
                    HEIGHT,
                    shortSnake,
                    boardObstacle,
                    nextHead
            );
            assertTrue(shortSnake.move('R'));
            assertTrue(shortSnake.ateCoin(food));
        }

        assertEquals(growthSteps + 1, shortSnake.getBody().size());

        Point fatalObstacle = new Point(
                shortSnake.getHead().x,
                shortSnake.getHead().y - 1
        );
        setObstacles(boardObstacle, List.of(fatalObstacle));
        assertFalse(shortSnake.move('U'));
        assertTrue(shortSnake.getDead());
        assertEquals(growthSteps + 1, shortSnake.getBody().size());

        Point center = new Point(WIDTH / 2, HEIGHT / 2);
        List<Point> outsideObstacles = List.of(
                new Point(center.x - 2, center.y),
                new Point(center.x + 2, center.y)
        );
        List<Point> allObstacles = new ArrayList<>(outsideObstacles);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                allObstacles.add(new Point(center.x + dx, center.y + dy));
            }
        }
        setObstacles(boardObstacle, allObstacles);

        shortSnake.reviveAtCenter(WIDTH, HEIGHT, boardObstacle);

        assertFalse(shortSnake.getDead());
        assertEquals(center, shortSnake.getHead());
        assertEquals(270, shortSnake.getSnakeDir());
        assertTrue(shortSnake.getBody().size() >= 1);
        assertEquals(List.of(new Point(center.x + 1, center.y)), shortSnake.getBody());
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                assertFalse(boardObstacle.getObstacles().contains(
                        new Point(center.x + dx, center.y + dy)
                ));
            }
        }
        assertEquals(outsideObstacles, boardObstacle.getObstacles());
    }

    // Resets every inventory value.
    // Expects a clean run state.
    @Test
    @DisplayName("Reset clears currency and owned power-ups")
    void resetClearsInventory() {
        inventory.addShopPoints(200);
        assertTrue(inventory.buyShield());
        assertTrue(inventory.buyMagnet());
        assertTrue(inventory.buyReviveStone());

        game.resetScore();

        assertEquals(0, inventory.getShopPoints());
        assertEquals(0, inventory.getShieldCharges());
        assertEquals(0, inventory.getShieldPurchases());
        assertEquals(0.0, inventory.getMagnetTimeRemaining());
        assertFalse(inventory.hasReviveStone());
        assertFalse(inventory.hasUsedReviveStone());
    }

    // Grows the snake along a straight line.
    // Uses fixed coin cells without obstacles.
    private void growSnake(Snake target, int foodCount) {
        for (int index = 0; index < foodCount; index++) {
            Point nextHead = new Point(
                    target.getHead().x + 1,
                    target.getHead().y
            );
            Coin food = new FixedCoin(
                    WIDTH,
                    HEIGHT,
                    target,
                    obstacle,
                    nextHead
            );
            target.move('R');
            assertTrue(target.ateCoin(food));
        }
    }

    // Repeats movement with one inventory.
    // Keeps test setup concise.
    private static void moveRepeatedly(
            Snake target,
            char direction,
            int count,
            PlayerInventory targetInventory
    ) {
        for (int index = 0; index < count; index++) {
            target.move(direction, targetInventory);
        }
    }

    // Replaces the obstacle board contents.
    // Keeps collision tests deterministic.
    @SuppressWarnings("unchecked")
    private static void setObstacles(
            Obstacle target,
            List<Point> positions
    ) {
        try {
            Field field = Obstacle.class.getDeclaredField("obstacles");
            field.setAccessible(true);
            List<Point> stored = (List<Point>) field.get(target);
            stored.clear();
            for (Point position : positions) {
                stored.add(new Point(position));
            }
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Unable to prepare obstacles.", exception);
        }
    }

    // Reads the private game score.
    // Verifies purchases never change it.
    private static int scoreOf(Game target) {
        try {
            Field field = Game.class.getDeclaredField("score");
            field.setAccessible(true);
            return field.getInt(target);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Unable to read game score.", exception);
        }
    }

    // Supplies a controlled coin position.
    // Avoids random growth paths.
    private static final class FixedCoin extends Coin {
        private final Point position;

        private FixedCoin(
                int width,
                int height,
                Snake targetSnake,
                Obstacle targetObstacle,
                Point position
        ) {
            super(width, height, targetSnake, targetObstacle);
            this.position = new Point(position);
        }

        @Override
        public Point getPos() {
            return new Point(position);
        }
    }
}
