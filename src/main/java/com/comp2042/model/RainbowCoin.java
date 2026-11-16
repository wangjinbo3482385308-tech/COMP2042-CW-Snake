package com.comp2042.model;

// Defines the temporary speed-boost coin.
public class RainbowCoin extends Coin {
    private static final double SPEED_BONUS = 5;

    public RainbowCoin(int x, int y, Snake snake, Obstacle obstacle) {
        super(x, y, snake, obstacle, CoinType.RAINBOW);
    }

    // Applies the rainbow movement bonus.
    @Override
    public void applyEffect(Game game, Snake snake, Obstacle obstacle) {
        super.applyEffect(game, snake, obstacle);
        game.setCurrentSpeed(game.getSpeed() + SPEED_BONUS);
    }

    public double getSpeedBonus() {
        return SPEED_BONUS;
    }
}
