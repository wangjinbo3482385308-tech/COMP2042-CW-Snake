package com.comp2042.model;

import java.util.Random;

// Defines the obstacle-spawning coin variant.
public class SilverCoin extends Coin {

    public SilverCoin(int x, int y, Snake snake, Obstacle obstacle) {
        super(x, y, snake, obstacle, CoinType.SILVER);
    }

    // Adds obstacles and lowers the remaining win target.
    @Override
    public void applyEffect(Game game, Snake snake, Obstacle obstacle) {
        super.applyEffect(game, snake, obstacle);

        int maxRange = Math.max(1, game.getWidth() * game.getHeight() / 20);
        int amount = new Random().nextInt(maxRange) + 3;
        int previousCount = obstacle.getObstacles().size();

        obstacle.newObstacles(snake, this, amount);
        int addedCount = obstacle.getObstacles().size() - previousCount;
        game.decMaxScore(addedCount);
    }
}
