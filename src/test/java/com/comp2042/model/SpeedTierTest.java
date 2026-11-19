package com.comp2042.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Checks speed tiers and their scoring effects.
@DisplayName("Speed tier model")
class SpeedTierTest {

    @Test
    @DisplayName("Slow tier exposes its settings")
    void slowTierExposesExpectedSettings() {
        assertEquals("Slow", SpeedTier.SLOW.getDisplayName());
        assertEquals(6.0, SpeedTier.SLOW.getBaseSpeed());
        assertEquals(1.0, SpeedTier.SLOW.getScoreMultiplier());
        assertFalse(SpeedTier.SLOW.isNoBorder());
    }

    @Test
    @DisplayName("Normal tier exposes its settings")
    void normalTierExposesExpectedSettings() {
        assertEquals("Normal", SpeedTier.NORMAL.getDisplayName());
        assertEquals(8.5, SpeedTier.NORMAL.getBaseSpeed());
        assertEquals(1.5, SpeedTier.NORMAL.getScoreMultiplier());
        assertTrue(SpeedTier.NORMAL.isNoBorder());
    }

    @Test
    @DisplayName("Fast tier exposes its settings")
    void fastTierExposesExpectedSettings() {
        assertEquals("Fast", SpeedTier.FAST.getDisplayName());
        assertEquals(12.0, SpeedTier.FAST.getBaseSpeed());
        assertEquals(2.0, SpeedTier.FAST.getScoreMultiplier());
        assertTrue(SpeedTier.FAST.isNoBorder());
    }

    @Test
    @DisplayName("Game applies each tier score multiplier")
    void gameAppliesTierScoreMultipliers() {
        Game slowGame = new Game(20, 20, SpeedTier.SLOW, false);
        Game normalGame = new Game(20, 20, SpeedTier.NORMAL, false);
        Game fastGame = new Game(20, 20, SpeedTier.FAST, false);

        slowGame.addPoints(10);
        normalGame.addPoints(10);
        fastGame.addPoints(10);

        assertEquals(10, slowGame.getScorePoints());
        assertEquals(15, normalGame.getScorePoints());
        assertEquals(20, fastGame.getScorePoints());
    }

    @Test
    @DisplayName("Game rejects a null tier")
    void gameUsesNormalTierWhenNull() {
        Game game = new Game(20, 20, (SpeedTier) null, false);

        assertSame(SpeedTier.NORMAL, game.getSpeedTier());
        assertEquals(8.5, game.getSpeed());
        assertEquals(8.5, game.getCurrentSpeed());
        assertTrue(game.isNoBorder());
    }

    @Test
    @DisplayName("Parser handles valid and invalid codes")
    void parserHandlesDifficultyCodes() {
        assertSame(SpeedTier.SLOW, SpeedTier.fromDifficultyCode('E'));
        assertSame(SpeedTier.SLOW, SpeedTier.fromDifficultyCode('e'));
        assertSame(SpeedTier.FAST, SpeedTier.fromDifficultyCode('H'));
        assertSame(SpeedTier.FAST, SpeedTier.fromDifficultyCode('h'));
        assertSame(SpeedTier.NORMAL, SpeedTier.fromDifficultyCode('N'));
        assertSame(SpeedTier.NORMAL, SpeedTier.fromDifficultyCode('Z'));
    }

    @Test
    @DisplayName("Invalid legacy difficulty uses normal settings")
    void invalidLegacyDifficultyUsesNormalFallback() {
        Game game = new Game(20, 20, false, 'Z');

        assertSame(SpeedTier.NORMAL, game.getSpeedTier());
        assertEquals(8.5, game.getSpeed());
        assertEquals(8.5, game.getCurrentSpeed());
        assertFalse(game.isNoBorder());
    }

    @Test
    @DisplayName("Changing tiers updates movement settings")
    void changingTierUpdatesMovementSettings() {
        Game game = new Game(20, 20, SpeedTier.NORMAL, false);

        game.setSpeedTier(SpeedTier.FAST);

        assertSame(SpeedTier.FAST, game.getSpeedTier());
        assertEquals(12.0, game.getSpeed());
        assertEquals(12.0, game.getCurrentSpeed());
        assertTrue(game.isNoBorder());

        game.setSpeedTier(null);

        assertSame(SpeedTier.NORMAL, game.getSpeedTier());
        assertEquals(8.5, game.getSpeed());
        assertEquals(8.5, game.getCurrentSpeed());
        assertTrue(game.isNoBorder());
    }
}
