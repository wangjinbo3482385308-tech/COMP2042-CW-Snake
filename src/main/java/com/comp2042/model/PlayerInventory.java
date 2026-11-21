package com.comp2042.model;

import java.awt.Point;

// Tracks shop currency and owned power-ups.
// Keeps all state independent of rendering.
public class PlayerInventory {
    public static final int MAX_SHIELD_PURCHASES = 3;
    public static final double MAGNET_DURATION_SECONDS = 10.0;
    public static final int MAGNET_RADIUS = 2;

    private int shopPoints;
    private int shieldCharges;
    private int shieldPurchases;
    private double magnetTimeRemaining;
    private boolean hasReviveStone;

    // Adds positive shop currency only.
    // Leaves all other point totals unchanged.
    public void addShopPoints(int points) {
        if (points > 0) {
            shopPoints += points;
        }
    }

    // Buys one shield when affordable.
    // Enforces the per-run purchase limit.
    public boolean buyShield() {
        int cost = PowerUpType.SHIELD.getCost();
        if (shopPoints < cost
                || shieldPurchases >= MAX_SHIELD_PURCHASES) {
            return false;
        }

        shopPoints -= cost;
        shieldCharges++;
        shieldPurchases++;
        return true;
    }

    // Uses one available shield charge.
    public boolean consumeShield() {
        if (shieldCharges <= 0) {
            return false;
        }

        shieldCharges--;
        return true;
    }

    // Buys a magnet when none is active.
    // Restarts the full magnet duration.
    public boolean buyMagnet() {
        int cost = PowerUpType.MAGNET.getCost();
        if (shopPoints < cost || isMagnetActive()) {
            return false;
        }

        shopPoints -= cost;
        magnetTimeRemaining = MAGNET_DURATION_SECONDS;
        return true;
    }

    // Advances the magnet countdown.
    // Clamps expired time to zero.
    public void tickMagnet(double deltaSeconds) {
        if (!(deltaSeconds > 0.0)) {
            return;
        }

        magnetTimeRemaining = Math.max(
                0.0,
                magnetTimeRemaining - deltaSeconds
        );
    }

    public boolean isMagnetActive() {
        return magnetTimeRemaining > 0.0;
    }

    // Checks a coin against the active magnet area.
    // Uses an inclusive five-by-five grid.
    public boolean isWithinMagnetRange(Point head, Point coinPos) {
        if (!isMagnetActive() || head == null || coinPos == null) {
            return false;
        }

        return Math.abs(head.x - coinPos.x) <= MAGNET_RADIUS
                && Math.abs(head.y - coinPos.y) <= MAGNET_RADIUS;
    }

    // Buys the single-use revive stone.
    public boolean buyReviveStone() {
        int cost = PowerUpType.REVIVE_STONE.getCost();
        if (shopPoints < cost || hasReviveStone) {
            return false;
        }

        shopPoints -= cost;
        hasReviveStone = true;
        return true;
    }

    // Uses the revive stone when one is held.
    public boolean consumeReviveStone() {
        if (!hasReviveStone) {
            return false;
        }

        hasReviveStone = false;
        return true;
    }

    // Restores the inventory after a game reset.
    public void reset() {
        shopPoints = 0;
        shieldCharges = 0;
        shieldPurchases = 0;
        magnetTimeRemaining = 0.0;
        hasReviveStone = false;
    }

    public int getShopPoints() {
        return shopPoints;
    }

    public int getShieldCharges() {
        return shieldCharges;
    }

    public int getShieldPurchases() {
        return shieldPurchases;
    }

    public double getMagnetTimeRemaining() {
        return magnetTimeRemaining;
    }

    public boolean hasReviveStone() {
        return hasReviveStone;
    }
}
