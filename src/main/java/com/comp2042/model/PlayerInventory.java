package com.comp2042.model;

import java.awt.Point;

/**
 * Tracks shop currency, shield stock, magnet countdown, and revive stone state for a run.
 * Remains independent of JavaFX rendering.
 */
public class PlayerInventory {
    /** Maximum number of shields that can be purchased in a single run. */
    public static final int MAX_SHIELD_PURCHASES = 3;
    /** Active duration of a purchased magnet power-up in seconds. */
    public static final double MAGNET_DURATION_SECONDS = 10.0;
    /** Inclusive Chebyshev grid radius of the magnet collection zone. */
    public static final int MAGNET_RADIUS = 2;

    private int shopPoints;
    private int shieldCharges;
    private int shieldPurchases;
    private double magnetTimeRemaining;
    private boolean hasReviveStone;
    // Enforces one revive per run.
    // This state persists after consumption.
    private boolean reviveStoneUsed;

    /**
     * Creates an empty inventory with zero shop points and no active power-ups.
     */
    public PlayerInventory() {
    }

    /**
     * Adds positive points to the player's spendable shop currency.
     *
     * @param points positive amount of shop points to add
     */
    public void addShopPoints(int points) {
        if (points > 0) {
            shopPoints += points;
        }
    }

    /**
     * Purchases one shield charge if affordable and below the per-run purchase limit.
     *
     * @return {@code true} if the shield was purchased
     */
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

    /**
     * Consumes one available shield charge to absorb a collision.
     *
     * @return {@code true} if a shield charge was consumed
     */
    public boolean consumeShield() {
        if (shieldCharges <= 0) {
            return false;
        }

        shieldCharges--;
        return true;
    }

    /**
     * Purchases and activates a magnet if affordable and no magnet is currently active.
     *
     * @return {@code true} if the magnet was purchased and activated
     */
    public boolean buyMagnet() {
        int cost = PowerUpType.MAGNET.getCost();
        if (shopPoints < cost || isMagnetActive()) {
            return false;
        }

        shopPoints -= cost;
        magnetTimeRemaining = MAGNET_DURATION_SECONDS;
        return true;
    }

    /**
     * Advances the active magnet timer by the elapsed gameplay duration, clamping at zero.
     *
     * @param deltaSeconds elapsed gameplay time in seconds
     */
    public void tickMagnet(double deltaSeconds) {
        if (!(deltaSeconds > 0.0)) {
            return;
        }

        magnetTimeRemaining = Math.max(
                0.0,
                magnetTimeRemaining - deltaSeconds
        );
    }

    /**
     * Reports whether the magnet power-up is currently active.
     *
     * @return {@code true} if remaining magnet time is positive
     */
    public boolean isMagnetActive() {
        return magnetTimeRemaining > 0.0;
    }

    /**
     * Checks whether a coin lies within the active magnet's inclusive 5x5 collection area.
     *
     * @param head current grid position of the snake head
     * @param coinPos grid position of the coin
     * @return {@code true} if the magnet is active and the coin is within range
     */
    public boolean isWithinMagnetRange(Point head, Point coinPos) {
        if (!isMagnetActive()
                || head == null
                || coinPos == null
                || coinPos.x < 0
                || coinPos.y < 0) {
            return false;
        }

        return Math.abs(head.x - coinPos.x) <= MAGNET_RADIUS
                && Math.abs(head.y - coinPos.y) <= MAGNET_RADIUS;
    }

    /**
     * Purchases the single-use revive stone if affordable, unowned, and not yet spent this run.
     *
     * @return {@code true} if the revive stone was purchased
     */
    public boolean buyReviveStone() {
        int cost = PowerUpType.REVIVE_STONE.getCost();
        if (shopPoints < cost || hasReviveStone || reviveStoneUsed) {
            return false;
        }

        shopPoints -= cost;
        hasReviveStone = true;
        return true;
    }

    /**
     * Consumes the held revive stone and permanently marks the run's revive as spent.
     *
     * @return {@code true} if a revive stone was held and consumed
     */
    public boolean consumeReviveStone() {
        if (!hasReviveStone) {
            return false;
        }

        hasReviveStone = false;
        reviveStoneUsed = true;
        return true;
    }

    /**
     * Resets all shop currency, power-up timers, and per-run purchase limits.
     */
    public void reset() {
        shopPoints = 0;
        shieldCharges = 0;
        shieldPurchases = 0;
        magnetTimeRemaining = 0.0;
        hasReviveStone = false;
        reviveStoneUsed = false;
    }

    /**
     * Returns the player's current spendable shop points.
     *
     * @return available shop points
     */
    public int getShopPoints() {
        return shopPoints;
    }

    /**
     * Returns the number of currently held shield charges.
     *
     * @return available shield charges
     */
    public int getShieldCharges() {
        return shieldCharges;
    }

    /**
     * Returns the total number of shields purchased in the current run.
     *
     * @return count of shields bought this run
     */
    public int getShieldPurchases() {
        return shieldPurchases;
    }

    /**
     * Returns the remaining active magnet duration in seconds.
     *
     * @return remaining magnet time in seconds
     */
    public double getMagnetTimeRemaining() {
        return magnetTimeRemaining;
    }

    /**
     * Reports whether the player currently holds an unused revive stone.
     *
     * @return {@code true} if a revive stone is held
     */
    public boolean hasReviveStone() {
        return hasReviveStone;
    }

    /**
     * Reports whether this run has already spent its single revive stone.
     *
     * @return {@code true} if a revive stone was consumed during this run
     */
    public boolean hasUsedReviveStone() {
        return reviveStoneUsed;
    }
}
