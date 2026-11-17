package com.comp2042.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Checks coin placement and power-up effects.
// Also verifies factory behavior.
// Uses fixed cells for deterministic assertions.
@DisplayName("Coin and coin factory models")
class CoinAndFactoryTest {
    private static final int WIDTH = 20;
    private static final int HEIGHT = 20;

    private Game game;
    private Snake snake;
    private Obstacle obstacle;

    // Creates normal gameplay settings.
    // Starts with an empty obstacle board.
    @BeforeEach
    void setUp() {
        game = new Game(WIDTH, HEIGHT, false, 'N');
        obstacle = new Obstacle(WIDTH, HEIGHT);
        snake = new Snake(WIDTH, HEIGHT, false, obstacle);
    }

    // Occupies selected cells with obstacles.
    // Expects a valid free coin cell.
    @Test
    @DisplayName("A normal coin avoids the snake and obstacles")
    void normalCoinAvoidsSnakeAndObstacles() {
        FixedObstacle occupiedObstacle = new FixedObstacle(
                WIDTH,
                HEIGHT,
                List.of(new Point(0, 0), new Point(19, 19))
        );
        Snake placedSnake = new Snake(WIDTH, HEIGHT, false, occupiedObstacle);

        Coin placedCoin = new Coin(WIDTH, HEIGHT, placedSnake, occupiedObstacle);
        Point position = placedCoin.getPos();

        assertTrue(position.x >= 0 && position.x < WIDTH);
        assertTrue(position.y >= 0 && position.y < HEIGHT);
        assertFalse(position.equals(placedSnake.getHead()));
        assertFalse(placedSnake.getBody().contains(position));
        assertFalse(occupiedObstacle.getObstacles().contains(position));
    }

    // Leaves only the top-left cell free.
    // Expects the coin to choose it exactly.
    @Test
    @DisplayName("A coin lands on the only free cell")
    void coinUsesOnlyFreeCellExactly() {
        Point onlyFreeCell = new Point(0, 0);
        FixedObstacle fullObstacle = new FixedObstacle(
                3,
                3,
                allCellsExcept(3, 3, onlyFreeCell)
        );
        Snake restrictedSnake = new Snake(3, 3, false, fullObstacle);

        Coin placedCoin = new Coin(3, 3, restrictedSnake, fullObstacle);

        assertEquals(onlyFreeCell, placedCoin.getPos());
    }

    // Occupies every board cell.
    // Expects the safe invalid coordinate.
    @Test
    @DisplayName("A coin returns an invalid position on a full board")
    void fullBoardCoinReturnsInvalidPosition() {
        FixedObstacle fullObstacle = new FixedObstacle(
                3,
                3,
                allCells(3, 3)
        );
        Snake restrictedSnake = new Snake(3, 3, false, fullObstacle);

        Coin placedCoin = new Coin(3, 3, restrictedSnake, fullObstacle);

        assertEquals(new Point(-1, -1), placedCoin.getPos());
    }

    // Applies the gold coin effect.
    // Expects its high point value.
    @Test
    @DisplayName("Gold coin awards the high-value score")
    void goldCoinAwardsHighValueScore() {
        GoldCoin goldCoin = new GoldCoin(WIDTH, HEIGHT, snake, obstacle);

        goldCoin.applyEffect(game, snake, obstacle);

        assertEquals(15, goldCoin.getPoints());
        assertEquals(15, game.getScorePoints());
        assertEquals(CoinType.GOLD, goldCoin.getType());
    }

    // Applies the rainbow coin effect.
    // Expects points and a speed increase.
    @Test
    @DisplayName("Rainbow coin awards points and increases speed")
    void rainbowCoinAwardsPointsAndIncreasesSpeed() {
        RainbowCoin rainbowCoin = new RainbowCoin(WIDTH, HEIGHT, snake, obstacle);
        double initialSpeed = game.getSpeed();

        rainbowCoin.applyEffect(game, snake, obstacle);

        assertEquals(rainbowCoin.getPoints(), game.getScorePoints());
        assertEquals(
                initialSpeed + rainbowCoin.getSpeedBonus(),
                game.getCurrentSpeed()
        );
    }

    // Applies the blue coin effect.
    // Expects temporary wall wrapping.
    @Test
    @DisplayName("Blue coin enables border passing")
    void blueCoinEnablesBorderPassing() {
        Obstacle smallObstacle = new Obstacle(4, 4);
        Snake smallSnake = new Snake(4, 4, false, smallObstacle);
        BlueCoin blueCoin = new BlueCoin(4, 4, smallSnake, smallObstacle);

        blueCoin.applyEffect(game, smallSnake, smallObstacle);
        smallSnake.move('U');
        smallSnake.move('U');
        assertEquals(0, smallSnake.getHead().y);

        smallSnake.move('U');

        assertEquals(3, smallSnake.getHead().y);
        assertFalse(smallSnake.getDead());
        assertEquals(blueCoin.getPoints(), game.getScorePoints());
    }

    // Applies the silver coin effect.
    // Expects added obstacles and a matching score deduction.
    @Test
    @DisplayName("Silver coin adds obstacles and lowers the target")
    void silverCoinAddsObstaclesAndLowersTarget() {
        SilverCoin silverCoin = new SilverCoin(WIDTH, HEIGHT, snake, obstacle);
        int initialMaxScore = maxScoreOf(game);

        silverCoin.applyEffect(game, snake, obstacle);

        int addedObstacles = obstacle.getObstacles().size();
        assertTrue(addedObstacles > 0);
        assertEquals(silverCoin.getPoints(), game.getScorePoints());
        assertEquals(initialMaxScore - addedObstacles, maxScoreOf(game));
    }

    // Disables power-up creation.
    // Expects base red coins only.
    @Test
    @DisplayName("Coin factory creates base coins when power-ups are disabled")
    void factoryCreatesBaseCoinsWhenPowerUpsAreDisabled() {
        CoinFactory factory = new CoinFactory();

        for (int index = 0; index < 25; index++) {
            Coin createdCoin = factory.createCoin(game, snake, obstacle);
            assertEquals(CoinType.RED, createdCoin.getType());
            assertEquals(Coin.class, createdCoin.getClass());
        }
    }

    // Enables power-up creation.
    // Expects more than one valid coin type.
    @Test
    @DisplayName("Coin factory creates power-up coins when enabled")
    void factoryCreatesPowerUpCoinsWhenEnabled() {
        Game powerUpGame = new Game(WIDTH, HEIGHT, true, 'N');
        CoinFactory factory = new CoinFactory();
        Set<CoinType> observedTypes = EnumSet.noneOf(CoinType.class);

        for (int index = 0; index < 240; index++) {
            Coin createdCoin = factory.createCoin(
                    powerUpGame,
                    snake,
                    obstacle
            );
            observedTypes.add(createdCoin.getType());
        }

        assertTrue(observedTypes.size() > 1);
        assertTrue(observedTypes.contains(CoinType.RED));
    }

    // Builds a cell list excluding one point.
    // Leaves exactly one free board cell.
    private static List<Point> allCellsExcept(
            int width,
            int height,
            Point excluded
    ) {
        List<Point> cells = allCells(width, height);
        cells.remove(excluded);
        return cells;
    }

    // Builds every board cell.
    // Supports full-board tests.
    private static List<Point> allCells(int width, int height) {
        List<Point> cells = new ArrayList<>();
        for (int column = 0; column < width; column++) {
            for (int row = 0; row < height; row++) {
                cells.add(new Point(column, row));
            }
        }
        return cells;
    }

    // Reads the private maximum score.
    // Verifies target adjustments exactly.
    private static int maxScoreOf(Game targetGame) {
        try {
            Field maxScoreField = Game.class.getDeclaredField("maxScore");
            maxScoreField.setAccessible(true);
            return maxScoreField.getInt(targetGame);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Unable to read the maximum score.", exception);
        }
    }

    // Supplies a controlled obstacle list.
    // Keeps placement tests deterministic.
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
}
