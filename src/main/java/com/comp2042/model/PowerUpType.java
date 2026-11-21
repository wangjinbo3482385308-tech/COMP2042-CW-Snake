package com.comp2042.model;

// Defines the purchasable power-ups.
// Stores their display text and shop cost.
public enum PowerUpType {
    SHIELD("Shield", 15),
    MAGNET("Magnet", 10),
    REVIVE_STONE("Revive Stone", 100);

    private final String displayName;
    private final int cost;

    PowerUpType(String displayName, int cost) {
        this.displayName = displayName;
        this.cost = cost;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCost() {
        return cost;
    }
}
