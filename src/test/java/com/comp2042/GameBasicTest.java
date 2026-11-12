package com.comp2042;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GameBasicTest {

    @Test
    void normalDifficultyInitialisesExpectedBoardAndMovementSettings() {
        Game game = new Game(20, 20, false, 'N');

        assertEquals(20, game.getWidth());
        assertEquals(20, game.getHeight());
        assertEquals(0, scoreOf(game));
        assertEquals(8.5, game.getSpeed());
        assertFalse(game.isNoBorder());
    }

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

    @Test
    void decMaxScoreSubtractsTheExactRequestedAmount() {
        Game game = new Game(20, 20, false, 'N');
        int initialMaxScore = maxScoreOf(game);

        game.decMaxScore(5);

        assertEquals(initialMaxScore - 5, maxScoreOf(game));
    }

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

    @Test
    void unknownDifficultyUsesNormalSpeedFallback() {
        Game game = new Game(20, 20, false, '?');

        assertEquals(8.5, game.getSpeed());
        assertFalse(game.isNoBorder());
    }

    private static int scoreOf(Game game) {
        try {
            Field scoreField = Game.class.getDeclaredField("score");
            scoreField.setAccessible(true);
            return scoreField.getInt(game);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Unable to read the game score.", exception);
        }
    }

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
