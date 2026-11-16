package com.comp2042.model;

// Defines the temporary border-passing coin.
public class BlueCoin extends Coin {

    public BlueCoin(int x, int y, Snake snake, Obstacle obstacle) {
        super(x, y, snake, obstacle, CoinType.BLUE);
    }

    // Enables wrapping until the next coin or reset.
    @Override
    public void applyEffect(Game game, Snake snake, Obstacle obstacle) {
        super.applyEffect(game, snake, obstacle);
        snake.ignoreBorders(true);
    }
}
