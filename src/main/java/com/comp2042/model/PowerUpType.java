package com.comp2042.model;

/**
 * Defines purchasable shop power-ups along with their HUD labels and point costs.
 */
public enum PowerUpType {
    /** Absorbs one fatal collision with a wall, obstacle, or body segment. */
    SHIELD("Shield", 15),
    /** Automatically collects coins within a 5x5 area around the snake head for a limited time. */
    MAGNET("Magnet", 10),
    /** Revives the snake once per run at the board center upon death. */
    REVIVE_STONE("Revive Stone", 100);

    private final String displayName;
    private final int cost;

    PowerUpType(String displayName, int cost) {
        this.displayName = displayName;
        this.cost = cost;
    }

    /**
     * Returns the human-readable name shown in the HUD.
     *
     * @return display name of the power-up
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the shop point cost required to purchase this power-up.
     *
     * @return cost in shop points
     */
    public int getCost() {
        return cost;
    }
}
