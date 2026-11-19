package com.comp2042.model;

// Owns gameplay scores and timing settings.
// Contains no JavaFX or rendering dependencies.
public class Game {
    private final int width;
    private final int height;
    private final double multiplier;
    private final boolean powerUp;

    private double speed;
    private boolean noBorder;
    private int score;
    private int maxScore;
    private int scorePoints;
    private GameState state;
    private double currentSpeed;
    private SpeedTier speedTier = SpeedTier.NORMAL;
    private boolean scoreMultiplierEnabled;

    // Builds a game from an explicit speed tier.
    // A null tier safely uses normal settings.
    // The win target follows the board size.
    public Game(int width, int height, SpeedTier speedTier, boolean powerUp) {
        this.width = width;
        this.height = height;
        this.powerUp = powerUp;
        multiplier = height > 50 ? 10 : 20;
        maxScore = width * height - 2;
        state = GameState.READY;
        applyTier(speedTier);
    }

    // Keeps the current legacy constructor available.
    // It delegates tier resolution to the shared parser.
    // Legacy difficulty values keep their original behavior.
    public Game(int x, int y, boolean powerUp, char difficulty) {
        this(x, y, SpeedTier.fromDifficultyCode(difficulty), powerUp);
        applyLegacyDifficulty(difficulty);
    }

    // Supports the documented difficulty-first argument order.
    // Existing callers may keep using the boolean-first form.
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

    // Preserves settings used by the legacy API.
    // Letter matching ignores case.
    private void applyLegacyDifficulty(char difficulty) {
        char normalizedDifficulty = Character.toUpperCase(difficulty);
        if (normalizedDifficulty == 'E') {
            speed = 8.5;
            noBorder = true;
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

    // Reports whether the win target has been reached.
    public boolean gameWon() {
        return score >= maxScore;
    }

    // Advances the collected-coin counter.
    // Marks the session as won when the target is reached.
    public void incScore() {
        score++;
        if (gameWon()) {
            state = GameState.WON;
        }
    }

    // Reduces the win target without passing the current score.
    public void decMaxScore(int amount) {
        maxScore = Math.max(score, maxScore - amount);
    }

    // Restores a clean run while keeping board settings.
    public void resetScore() {
        score = 0;
        scorePoints = 0;
        maxScore = width * height - 2;
        state = GameState.READY;
    }

    // Adds points with the active tier multiplier.
    // The win counter remains unchanged.
    public void addPoints(int points) {
        int scaledPoints = (int) Math.round(
                points * speedTier.getScoreMultiplier()
        );
        scorePoints += scaledPoints;
    }

    // Preserves legacy coin scoring.
    // Explicit tiers still apply their multipliers.
    void addCoinPoints(int points) {
        if (scoreMultiplierEnabled) {
            addPoints(points);
        } else {
            scorePoints += points;
        }
    }

    // Restores the difficulty's base movement rate.
    public void resetCurrentSpeed() {
        currentSpeed = speed;
    }

    // Applies a temporary movement speed.
    public void setCurrentSpeed(double currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    // Switches the active speed tier.
    // A null tier safely uses normal settings.
    // Base and current rates update together.
    public void setSpeedTier(SpeedTier tier) {
        applyTier(tier);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public int getScorePoints() {
        return scorePoints;
    }

    public boolean isPowerUp() {
        return powerUp;
    }

    public double getSpeed() {
        return speed;
    }

    public double getCurrentSpeed() {
        return currentSpeed;
    }

    public SpeedTier getSpeedTier() {
        return speedTier;
    }

    public boolean isNoBorder() {
        return noBorder;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }
}
