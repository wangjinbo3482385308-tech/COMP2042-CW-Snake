package com.comp2042.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Checks game settings and scoring behavior.
// Migrates the former basic tests.
// Verifies exact win and reset boundaries.
@DisplayName("Game model")
class GameTest {
    private Game normalGame;

    // Creates a normal game for each test.
    // Provides a stable baseline.
    @BeforeEach
    void setUp() {
        normalGame = new Game(20, 20, false, 'N');
    }

    // Builds an easy-difficulty game.
    // Expects legacy speed and closed borders.
    @Test
    @DisplayName("Easy difficulty keeps default walls closed")
    void easyDifficultyUsesExpectedSettings() {
        Game easyGame = new Game(20, 20, false, 'E');

        assertEquals(8.5, easyGame.getSpeed());
        assertEquals(8.5, easyGame.getCurrentSpeed());
        assertFalse(easyGame.isNoBorder());
        assertEquals(GameState.READY, easyGame.getState());
    }

    // Builds a normal-difficulty game.
    // Expects default speed and closed borders.
    @Test
    @DisplayName("Normal difficulty uses default settings")
    void normalDifficultyUsesExpectedSettings() {
        assertEquals(8.5, normalGame.getSpeed());
        assertEquals(8.5, normalGame.getCurrentSpeed());
        assertFalse(normalGame.isNoBorder());
        assertEquals(20, normalGame.getWidth());
        assertEquals(20, normalGame.getHeight());
    }

    // Builds a hard-difficulty game.
    // Expects faster movement and closed borders.
    @Test
    @DisplayName("Hard difficulty increases speed")
    void hardDifficultyUsesExpectedSettings() {
        Game hardGame = new Game(20, 20, false, 'H');

        assertEquals(13, hardGame.getSpeed());
        assertEquals(13, hardGame.getCurrentSpeed());
        assertFalse(hardGame.isNoBorder());
    }

    // Uses an unsupported difficulty code.
    // Expects normal settings as a fallback.
    @Test
    @DisplayName("Unknown difficulty falls back to normal settings")
    void unknownDifficultyUsesNormalFallback() {
        Game fallbackGame = new Game(20, 20, false, 'Z');

        assertEquals(8.5, fallbackGame.getSpeed());
        assertFalse(fallbackGame.isNoBorder());
    }

    // Reduces the win target once.
    // Expects exactly the requested deduction.
    @Test
    @DisplayName("Maximum-score reduction is exact")
    void decMaxScoreUsesExactAmount() {
        int initialMaxScore = maxScoreOf(normalGame);

        normalGame.decMaxScore(7);

        assertEquals(initialMaxScore - 7, maxScoreOf(normalGame));
    }

    // Reaches the final win target.
    // Expects the threshold boundary only.
    @Test
    @DisplayName("Game won triggers exactly at the target")
    void gameWonTriggersAtExactThreshold() {
        Game smallGame = new Game(3, 3, false, 'N');

        for (int index = 0; index < 6; index++) {
            smallGame.incScore();
        }
        assertFalse(smallGame.gameWon());

        smallGame.incScore();

        assertTrue(smallGame.gameWon());
        assertEquals(GameState.WON, smallGame.getState());
    }

    // Updates every score-related value.
    // Expects reset to restore the initial target.
    @Test
    @DisplayName("Reset restores scores and the initial target")
    void resetScoreRestoresInitialValues() {
        int initialMaxScore = maxScoreOf(normalGame);
        normalGame.incScore();
        normalGame.incScore();
        normalGame.addPoints(12);
        normalGame.decMaxScore(20);
        normalGame.setState(GameState.RUNNING);

        normalGame.resetScore();

        assertEquals(0, scoreOf(normalGame));
        assertEquals(0, normalGame.getScorePoints());
        assertEquals(initialMaxScore, maxScoreOf(normalGame));
        assertEquals(GameState.READY, normalGame.getState());
    }

    // Applies a temporary movement speed.
    // Expects a reset to the difficulty speed.
    @Test
    @DisplayName("Current speed resets to the base speed")
    void resetCurrentSpeedRestoresBaseSpeed() {
        normalGame.setCurrentSpeed(18);
        assertEquals(18, normalGame.getCurrentSpeed());

        normalGame.resetCurrentSpeed();

        assertEquals(normalGame.getSpeed(), normalGame.getCurrentSpeed());
    }

    // Reads the private collected score.
    // Keeps assertions tied to public changes.
    private static int scoreOf(Game targetGame) {
        return privateInt(targetGame, "score");
    }

    // Reads the private maximum score.
    // Keeps assertions tied to public changes.
    private static int maxScoreOf(Game targetGame) {
        return privateInt(targetGame, "maxScore");
    }

    // Reads one private integer field.
    // Reports reflection failures clearly.
    private static int privateInt(Game targetGame, String fieldName) {
        try {
            Field field = Game.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(targetGame);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(
                    "Unable to read game field: " + fieldName,
                    exception
            );
        }
    }
}
