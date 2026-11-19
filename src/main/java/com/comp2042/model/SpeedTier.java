package com.comp2042.model;

// Defines the available speed tiers.
// Each tier owns its movement and scoring values.
public enum SpeedTier {
    SLOW("Slow", 6.0, 1.0, false),
    NORMAL("Normal", 8.5, 1.5, true),
    FAST("Fast", 12.0, 2.0, true);

    private final String displayName;
    private final double baseSpeed;
    private final double scoreMultiplier;
    private final boolean noBorder;

    // Stores the immutable tier settings.
    SpeedTier(
            String displayName,
            double baseSpeed,
            double scoreMultiplier,
            boolean noBorder
    ) {
        this.displayName = displayName;
        this.baseSpeed = baseSpeed;
        this.scoreMultiplier = scoreMultiplier;
        this.noBorder = noBorder;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getBaseSpeed() {
        return baseSpeed;
    }

    public double getScoreMultiplier() {
        return scoreMultiplier;
    }

    public boolean isNoBorder() {
        return noBorder;
    }

    // Resolves a legacy difficulty code.
    // Unknown codes safely use the normal tier.
    public static SpeedTier fromDifficultyCode(char code) {
        char normalizedCode = Character.toUpperCase(code);
        if (normalizedCode == 'E') {
            return SLOW;
        }
        if (normalizedCode == 'H') {
            return FAST;
        }
        return NORMAL;
    }
}
