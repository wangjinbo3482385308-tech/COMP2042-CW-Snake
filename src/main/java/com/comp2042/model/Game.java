package com.comp2042.model;

/**
 * Owns gameplay configuration, scoring, speed tiers, inventory, portals, and session state.
 * Remains completely independent of JavaFX and rendering concerns.
 */
public class Game {
    private final int width;
    private final int height;
    private final double multiplier;
    private final boolean powerUp;
    private final PlayerInventory inventory = new PlayerInventory();
    private final PortalPair portalPair = new PortalPair();

    private double speed;
    private boolean noBorder;
    private int score;
    private int maxScore;
    private int scorePoints;
    private GameState state;
    private double currentSpeed;
    private SpeedTier speedTier = SpeedTier.NORMAL;
    private boolean scoreMultiplierEnabled;

    /**
     * Builds a game configured with an explicit speed tier.
     * Defaults to {@link SpeedTier#NORMAL} when {@code speedTier} is {@code null}.
     *
     * @param width board width in grid cells
     * @param height board height in grid cells
     * @param speedTier selected speed tier, or {@code null} for normal
     * @param powerUp {@code true} to enable special power-up coins
     */
    public Game(int width, int height, SpeedTier speedTier, boolean powerUp) {
        this.width = width;
        this.height = height;
        this.powerUp = powerUp;
        multiplier = height > 50 ? 10 : 20;
        maxScore = width * height - 2;
        state = GameState.READY;
        applyTier(speedTier);
    }

    /**
     * Builds a game using a legacy difficulty code with boolean-first parameter order.
     *
     * @param x board width in grid cells
     * @param y board height in grid cells
     * @param powerUp {@code true} to enable special power-up coins
     * @param difficulty legacy difficulty code ({@code E}, {@code M}, or {@code H})
     */
    public Game(int x, int y, boolean powerUp, char difficulty) {
        this(x, y, SpeedTier.fromDifficultyCode(difficulty), powerUp);
        applyLegacyDifficulty(difficulty);
    }

    /**
     * Builds a game using a legacy difficulty code with difficulty-first parameter order.
     *
     * @param width board width in grid cells
     * @param height board height in grid cells
     * @param difficulty legacy difficulty code ({@code E}, {@code M}, or {@code H})
     * @param powerUp {@code true} to enable special power-up coins
     */
    public Game(int width, int height, char difficulty, boolean powerUp) {
        this(width, height, powerUp, difficulty);
    }

    // Applies movement and border settings for a tier.
    // The current rate starts from the base rate.
    private void applyTier(SpeedTier tier) {
        speedTier = tier == null ? SpeedTier.NORMAL : tier;
        speed = speedTier.getBaseSpeed();
        currentSpeed = speed;
        noBorder = speedTier.isNoBorder();
        scoreMultiplierEnabled = true;
    }

    // Preserves speed settings used by the legacy API.
    // Border passing remains a BlueCoin-only effect.
    private void applyLegacyDifficulty(char difficulty) {
        char normalizedDifficulty = Character.toUpperCase(difficulty);
        if (normalizedDifficulty == 'E') {
            speed = 8.5;
            noBorder = false;
        } else if (normalizedDifficulty == 'H') {
            speed = 13;
            noBorder = false;
        } else {
            speed = 8.5;
            noBorder = false;
        }
        currentSpeed = speed;
        scoreMultiplierEnabled = false;
    }

    /**
     * Reports whether the collected-coin win target has been reached.
     *
     * @return {@code true} if the current coin count meets or exceeds the win target
     */
    public boolean gameWon() {
        return score >= maxScore;
    }

    /**
     * Increments the collected-coin counter and transitions to {@link GameState#WON} when complete.
     */
    public void incScore() {
        score++;
        if (gameWon()) {
            state = GameState.WON;
        }
    }

    /**
     * Lowers the win target after obstacles shrink playable space, without falling below the current score.
     *
     * @param amount number of cells to subtract from the win target
     */
    public void decMaxScore(int amount) {
        maxScore = Math.max(score, maxScore - amount);
    }

    /**
     * Resets scores, state, portals, and inventory for a fresh run while preserving board settings.
     */
    public void resetScore() {
        score = 0;
        scorePoints = 0;
        maxScore = width * height - 2;
        state = GameState.READY;
        portalPair.reset();
        inventory.reset();
    }

    /**
     * Adds points scaled by the active speed tier multiplier to both score and shop balance.
     *
     * @param points base points before applying the tier multiplier
     */
    public void addPoints(int points) {
        int gainedPoints = (int) Math.round(
                points * speedTier.getScoreMultiplier()
        );
        scorePoints += gainedPoints;
        inventory.addShopPoints(gainedPoints);
    }

    /**
     * Adds a flat bonus reward to score and shop points without applying speed multipliers.
     *
     * @param bonus positive bonus points to award
     */
    public void addBonusPoints(int bonus) {
        if (bonus > 0) {
            scorePoints += bonus;
            inventory.addShopPoints(bonus);
        }
    }

    // Preserves legacy coin scoring.
    // Explicit tiers still apply their multipliers.
    void addCoinPoints(int points) {
        if (scoreMultiplierEnabled) {
            addPoints(points);
        } else {
            scorePoints += points;
            inventory.addShopPoints(points);
        }
    }

    /**
     * Restores the active movement speed to the session's base speed.
     */
    public void resetCurrentSpeed() {
        currentSpeed = speed;
    }

    /**
     * Sets a temporary movement speed for the active session.
     *
     * @param currentSpeed movement rate in ticks per second
     */
    public void setCurrentSpeed(double currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    /**
     * Switches the active speed tier and updates both base and current speeds.
     * Defaults to {@link SpeedTier#NORMAL} when {@code tier} is {@code null}.
     *
     * @param tier target speed tier, or {@code null} for normal
     */
    public void setSpeedTier(SpeedTier tier) {
        applyTier(tier);
    }

    /**
     * Returns the board width in grid cells.
     *
     * @return board width in cells
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the board height in grid cells.
     *
     * @return board height in cells
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the base pixel size of a single grid cell.
     *
     * @return cell pixel multiplier
     */
    public double getMultiplier() {
        return multiplier;
    }

    /**
     * Returns the total score points earned in the current run.
     *
     * @return current score points
     */
    public int getScorePoints() {
        return scorePoints;
    }

    /**
     * Reports whether special power-up coins are enabled.
     *
     * @return {@code true} if power-up coins can spawn
     */
    public boolean isPowerUp() {
        return powerUp;
    }

    /**
     * Returns the player's shop currency and power-up inventory.
     *
     * @return session {@link PlayerInventory}
     */
    public PlayerInventory getInventory() {
        return inventory;
    }

    /**
     * Returns the session's portal pair manager.
     *
     * @return session {@link PortalPair}
     */
    public PortalPair getPortalPair() {
        return portalPair;
    }

    /**
     * Returns the base movement speed for the current difficulty or tier.
     *
     * @return base speed in ticks per second
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Returns the active movement speed including any temporary boost.
     *
     * @return current speed in ticks per second
     */
    public double getCurrentSpeed() {
        return currentSpeed;
    }

    /**
     * Returns the currently selected speed tier.
     *
     * @return active {@link SpeedTier}
     */
    public SpeedTier getSpeedTier() {
        return speedTier;
    }

    /**
     * Reports whether border wrapping is permanently enabled for the board.
     *
     * @return {@code true} if walls wrap by default
     */
    public boolean isNoBorder() {
        return noBorder;
    }

    /**
     * Returns the current session lifecycle state.
     *
     * @return active {@link GameState}
     */
    public GameState getState() {
        return state;
    }

    /**
     * Updates the current session lifecycle state.
     *
     * @param state new {@link GameState}
     */
    public void setState(GameState state) {
        this.state = state;
    }
}
