package com.comp2042;

import com.comp2042.model.Game;
import com.comp2042.model.GameState;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

// Verifies core model behavior after the architecture refactor.
class GameBasicTest {

    // Checks normal board and movement defaults.
    @Test
    void normalDifficultyInitialisesExpectedBoardAndMovementSettings() {
        Game game = new Game(20, 20, false, 'N');

        assertEquals(20, game.getWidth());
        assertEquals(20, game.getHeight());
        assertEquals(0, scoreOf(game));
        assertEquals(8.5, game.getSpeed());
        assertFalse(game.isNoBorder());
    }

    // Checks score counters and reset behavior.
    @Test
    void scoreMethodsUpdateTheExpectedValues() {
        Game game = new Game(20, 20, false, 'N');

        game.incScore();
        assertEquals(1, scoreOf(game));

        game.addPoints(10);
        assertEquals(10, game.getScorePoints());

        game.resetScore();
        assertEquals(0, scoreOf(game));
        assertEquals(0, game.getScorePoints());
    }

    // Checks exact target reductions.
    @Test
    void decMaxScoreSubtractsTheExactRequestedAmount() {
        Game game = new Game(20, 20, false, 'N');
        int initialMaxScore = maxScoreOf(game);

        game.decMaxScore(5);

        assertEquals(initialMaxScore - 5, maxScoreOf(game));
    }

    // Checks that reset restores the original target.
    @Test
    void resetScoreRestoresTheInitialMaxScore() {
        Game game = new Game(20, 20, false, 'N');
        int initialMaxScore = maxScoreOf(game);
        game.incScore();
        game.addPoints(10);
        game.decMaxScore(5);

        game.resetScore();

        assertEquals(0, scoreOf(game));
        assertEquals(0, game.getScorePoints());
        assertEquals(initialMaxScore, maxScoreOf(game));
    }

    // Checks the fallback for unknown difficulty codes.
    @Test
    void unknownDifficultyUsesNormalSpeedFallback() {
        Game game = new Game(20, 20, false, '?');

        assertEquals(8.5, game.getSpeed());
        assertFalse(game.isNoBorder());
    }

    // Checks initial state and temporary speed restoration.
    @Test
    void stateAndCurrentSpeedStartAndResetConsistently() {
        Game game = new Game(20, 20, false, 'N');

        assertEquals(GameState.READY, game.getState());
        assertEquals(game.getSpeed(), game.getCurrentSpeed());

        game.setState(GameState.RUNNING);
        game.setCurrentSpeed(18);
        game.resetCurrentSpeed();

        assertEquals(game.getSpeed(), game.getCurrentSpeed());
    }

    // Reads the private score for compatibility checks.
    private static int scoreOf(Game game) {
        try {
            Field scoreField = Game.class.getDeclaredField("score");
            scoreField.setAccessible(true);
            return scoreField.getInt(game);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Unable to read the game score.", exception);
        }
    }

    // Reads the private target for compatibility checks.
    private static int maxScoreOf(Game game) {
        try {
            Field maxScoreField = Game.class.getDeclaredField("maxScore");
            maxScoreField.setAccessible(true);
            return maxScoreField.getInt(game);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Unable to read the maximum score.", exception);
        }
    }
}
