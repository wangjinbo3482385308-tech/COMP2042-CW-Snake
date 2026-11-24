package com.comp2042.model;

/**
 * Defines selectable speed tiers along with their base speeds, score multipliers, and wall rules.
 */
public enum SpeedTier {
    /** Slower movement speed with a {@code 1.0x} score multiplier and closed walls. */
    SLOW("Slow", 6.0, 1.0, false),
    /** Standard movement speed with a {@code 1.5x} score multiplier and closed walls. */
    NORMAL("Normal", 8.5, 1.5, false),
    /** High movement speed with a {@code 2.0x} score multiplier and closed walls. */
    FAST("Fast", 12.0, 2.0, false);

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

    /**
     * Returns the display label shown in the menu and HUD.
     *
     * @return tier display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the base movement rate for this tier.
     *
     * @return base speed in ticks per second
     */
    public double getBaseSpeed() {
        return baseSpeed;
    }

    /**
     * Returns the multiplier applied to coin points in this tier.
     *
     * @return score multiplier
     */
    public double getScoreMultiplier() {
        return scoreMultiplier;
    }

    /**
     * Reports whether this tier enables permanent border wrapping.
     *
     * @return {@code true} if walls wrap by default
     */
    public boolean isNoBorder() {
        return noBorder;
    }

    /**
     * Maps a legacy difficulty character code to its corresponding speed tier.
     * Unknown codes default to {@link #NORMAL}.
     *
     * @param code legacy difficulty code ({@code E}, {@code M}, or {@code H})
     * @return resolved {@link SpeedTier}
     */
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
