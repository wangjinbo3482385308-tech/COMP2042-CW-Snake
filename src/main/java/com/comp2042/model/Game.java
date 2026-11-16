package com.comp2042.model;

// Owns gameplay scores and timing settings.
// Contains no JavaFX or rendering dependencies.
public class Game {
    private final int width;
    private final int height;
    private final double multiplier;
    private final double speed;
    private final boolean powerUp;
    private final boolean noBorder;

    private int score;
    private int maxScore;
    private int scorePoints;
    private GameState state;
    private double currentSpeed;

    // Builds a game from board size and difficulty.
    // Unknown difficulty values use normal settings.
    public Game(int x, int y, boolean powerUp, char difficulty) {
        if (difficulty == 'E') {
            speed = 8.5;
            noBorder = true;
        } else if (difficulty == 'H') {
            speed = 13;
            noBorder = false;
        } else {
            speed = 8.5;
            noBorder = false;
        }

        this.powerUp = powerUp;
        width = x;
        height = y;
        multiplier = height > 50 ? 10 : 20;
        maxScore = x * y - 2;
        currentSpeed = speed;
        state = GameState.READY;
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

    // Adds raw points without changing the win counter.
    public void addPoints(int points) {
        scorePoints += points;
    }

    // Restores the difficulty's base movement rate.
    public void resetCurrentSpeed() {
        currentSpeed = speed;
    }

    // Applies a temporary movement speed.
    public void setCurrentSpeed(double currentSpeed) {
        this.currentSpeed = currentSpeed;
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
