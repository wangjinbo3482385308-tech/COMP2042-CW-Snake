package com.comp2042.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Verifies preset level layouts, obstacle reset semantics, win target adjustments,
// and SilverCoin spawning compatibility on obstacle-bearing maps.
@DisplayName("LevelMap model and gameplay integration")
class LevelMapTest {
    private static final int BOARD_WIDTH = 20;
    private static final int BOARD_HEIGHT = 20;

    @Test
    @DisplayName("All preset maps expose expected display names")
    void presetMapsExposeExpectedDisplayNames() {
        assertEquals("Classic", LevelMap.CLASSIC.getDisplayName());
        assertEquals("Pillars", LevelMap.PILLARS.getDisplayName());
        assertEquals("Arena", LevelMap.ARENA.getDisplayName());
    }

    @Test
    @DisplayName("All preset maps stay in bounds, contain unique points, and clear the snake spawn safe zone")
    void presetMapsStayInBoundsUniqueAndClearOfSpawnZone() {
        int[][] boardSizes = {
                {BOARD_WIDTH, BOARD_HEIGHT},
                {12, 12},
                {10, 10},
                {30, 30}
        };

        for (int[] size : boardSizes) {
            int width = size[0];
            int height = size[1];
            int centerX = width / 2;
            int centerY = height / 2;

            for (LevelMap levelMap : LevelMap.values()) {
                List<Point> points = levelMap.buildObstacles(width, height);
                Set<Point> uniquePoints = new HashSet<>(points);

                assertEquals(points.size(), uniquePoints.size());
                if (levelMap == LevelMap.CLASSIC) {
                    assertTrue(points.isEmpty());
                } else {
                    assertFalse(points.isEmpty());
                }

                for (Point point : points) {
                    assertTrue(point.x >= 0 && point.x < width);
                    assertTrue(point.y >= 0 && point.y < height);
                }

                // Initial head, initial body, and at least 4 cells ahead (LEFT) must be clear.
                for (int x = centerX - 4; x <= centerX + 1; x++) {
                    assertFalse(
                            uniquePoints.contains(new Point(x, centerY)),
                            levelMap + " conflicted with safe spawn cell (" + x + "," + centerY + ")"
                    );
                }
            }
        }

        assertTrue(LevelMap.PILLARS.buildObstacles(0, 20).isEmpty());
        assertTrue(LevelMap.ARENA.buildObstacles(20, 0).isEmpty());
    }

    @Test
    @DisplayName("Snake spawned on Pillars and Arena survives at least four initial leftward moves")
    void snakeSurvivesInitialForwardLaneOnAllLevels() {
        for (LevelMap levelMap : LevelMap.values()) {
            Obstacle obstacle = new Obstacle(BOARD_WIDTH, BOARD_HEIGHT, levelMap);
            Snake snake = new Snake(BOARD_WIDTH, BOARD_HEIGHT, false, obstacle);

            for (int step = 0; step < 4; step++) {
                assertTrue(snake.move('L'));
                assertFalse(snake.getDead());
            }
        }
    }

    @Test
    @DisplayName("Obstacle restores Pillars and Arena presets accurately and clears dynamic obstacles on reset")
    void obstacleRestoresPresetsAndClearsDynamicObstaclesOnReset() {
        for (LevelMap levelMap : List.of(LevelMap.PILLARS, LevelMap.ARENA)) {
            List<Point> expectedPreset = levelMap.buildObstacles(BOARD_WIDTH, BOARD_HEIGHT);
            Obstacle obstacle = new Obstacle(BOARD_WIDTH, BOARD_HEIGHT);
            obstacle.applyLevelMap(levelMap);

            assertEquals(expectedPreset, obstacle.getObstacles());
            assertEquals(expectedPreset.size(), obstacle.getBaseObstacleCount());
            assertFalse(obstacle.hasDynamicObstacles());
            assertEquals(0, obstacle.getDynamicObstacleCount());

            Snake snake = new Snake(BOARD_WIDTH, BOARD_HEIGHT, false, obstacle);
            Coin coin = new Coin(BOARD_WIDTH, BOARD_HEIGHT, snake, obstacle);
            assertFalse(obstacle.getObstacles().contains(coin.getPos()));

            // Add dynamic obstacles and also remove one preset obstacle to simulate shield damage.
            obstacle.newObstacles(snake, coin, 5);
            assertTrue(obstacle.hasDynamicObstacles());
            assertTrue(obstacle.getDynamicObstacleCount() > 0);
            assertTrue(obstacle.removeObstacleAt(expectedPreset.get(0)));

            obstacle.reset();

            assertEquals(expectedPreset, obstacle.getObstacles());
            assertFalse(obstacle.hasDynamicObstacles());
            assertEquals(0, obstacle.getDynamicObstacleCount());
        }

        Obstacle classicObstacle = new Obstacle(BOARD_WIDTH, BOARD_HEIGHT);
        classicObstacle.applyLevelMap(null);
        assertTrue(classicObstacle.getObstacles().isEmpty());
        assertEquals(0, classicObstacle.getBaseObstacleCount());
    }

    @Test
    @DisplayName("Game deducts preset obstacle counts from initial maxScore and preserves them on resetScore")
    void gameDeductsPresetObstaclesFromMaxScoreAndRestoresOnReset() {
        Game nullLevelGame = new Game(BOARD_WIDTH, BOARD_HEIGHT, SpeedTier.NORMAL, true, null);
        assertSame(LevelMap.CLASSIC, nullLevelGame.getLevelMap());
        assertEquals(BOARD_WIDTH * BOARD_HEIGHT - 2, maxScoreOf(nullLevelGame));

        for (LevelMap levelMap : List.of(LevelMap.PILLARS, LevelMap.ARENA)) {
            int presetCount = levelMap.buildObstacles(BOARD_WIDTH, BOARD_HEIGHT).size();
            int expectedMaxScore = BOARD_WIDTH * BOARD_HEIGHT - 2 - presetCount;
            Game game = new Game(BOARD_WIDTH, BOARD_HEIGHT, SpeedTier.NORMAL, true, levelMap);

            assertSame(levelMap, game.getLevelMap());
            assertEquals(expectedMaxScore, maxScoreOf(game));

            game.incScore();
            game.decMaxScore(6);
            game.setState(GameState.RUNNING);
            assertEquals(expectedMaxScore - 6, maxScoreOf(game));

            game.resetScore();

            assertEquals(expectedMaxScore, maxScoreOf(game));
            assertEquals(GameState.READY, game.getState());
        }
    }

    @Test
    @DisplayName("CoinFactory can spawn SilverCoin on preset maps until dynamic obstacles are added")
    void coinFactorySpawnsSilverCoinOnPresetMapsUntilDynamicObstaclesExist() {
        CoinFactory factory = new CoinFactory();

        for (LevelMap levelMap : List.of(LevelMap.PILLARS, LevelMap.ARENA)) {
            Game game = new Game(BOARD_WIDTH, BOARD_HEIGHT, SpeedTier.NORMAL, true, levelMap);
            Obstacle obstacle = new Obstacle(BOARD_WIDTH, BOARD_HEIGHT, levelMap);
            Snake snake = new Snake(BOARD_WIDTH, BOARD_HEIGHT, false, obstacle);

            assertFalse(obstacle.getObstacles().isEmpty());
            assertFalse(obstacle.hasDynamicObstacles());

            boolean sawSilverBeforeDynamic = false;
            for (int attempt = 0; attempt < 300; attempt++) {
                Coin coin = factory.createCoin(game, snake, obstacle);
                assertFalse(obstacle.getObstacles().contains(coin.getPos()));
                if (coin.getType() == CoinType.SILVER) {
                    sawSilverBeforeDynamic = true;
                    break;
                }
            }
            assertTrue(sawSilverBeforeDynamic, "Expected SilverCoin to spawn on " + levelMap);

            // Once dynamic obstacles exist, SilverCoin should no longer spawn until reset.
            Coin referenceCoin = new Coin(BOARD_WIDTH, BOARD_HEIGHT, snake, obstacle);
            obstacle.newObstacles(snake, referenceCoin, 3);
            assertTrue(obstacle.hasDynamicObstacles());

            for (int attempt = 0; attempt < 150; attempt++) {
                Coin coin = factory.createCoin(game, snake, obstacle);
                assertFalse(
                        coin.getType() == CoinType.SILVER,
                        "SilverCoin should not spawn while dynamic obstacles already exist"
                );
            }
        }
    }

    private static int maxScoreOf(Game targetGame) {
        try {
            Field field = Game.class.getDeclaredField("maxScore");
            field.setAccessible(true);
            return field.getInt(targetGame);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Unable to read Game.maxScore", exception);
        }
    }
}
