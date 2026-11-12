package com.comp2042;

import javafx.scene.paint.Color;

public class Game {
    private Color color = Color.web("feffe4");
    private int width;
    private int height;
    private int score;
    private int maxScore;
    private double multiplier;
    private int scorePoints;
    private double speed;
    private boolean powerUp;
    private boolean noBorder;

    public Game(int x, int y, boolean powerUp, char difficulty) {
        if (difficulty == 'E') {
            speed = 8.5;
            noBorder = true;
        } else if (difficulty == 'N') {
            speed = 8.5;
            noBorder = false;
        } else if (difficulty == 'H') {
            speed = 13;
            noBorder = false;
            // Use a safe default for unknown difficulty codes.
            // Keep normal speed and border collisions enabled.
        } else {
            speed = 8.5;
            noBorder = false;
        }
        // Apply the power-up setting exactly once.
        // Remove duplicate assignment after construction.
        this.powerUp = powerUp;
        width = x;
        height = y;
        maxScore = x * y - 2;
        if (height > 50) {
            multiplier = 10;
        } else {
            multiplier = 20;
        }
    }

    public boolean gameWon() {
        // Treat reaching the target as a win.
        // This also handles overshooting safely.
        return score >= maxScore;
    }

    public void incScore() {
        score++;
    }

    public void decMaxScore(int num) {
        // Reduce the target by the requested amount.
        // Never place the target below the current score.
        this.maxScore = Math.max(score, this.maxScore - num);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void resetScore() {
        // Reset both score values for a new game.
        // Restore the full-board win target.
        score = 0;
        scorePoints = 0;
        maxScore = width * height - 2;
    }

    public Color getColor() {
        return color;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void addPoints(int n) {
        scorePoints += n;
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

    public boolean isNoBorder() {
        return noBorder;
    }
}
