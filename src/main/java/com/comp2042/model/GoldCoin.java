package com.comp2042.model;

// Defines the high-value coin variant.
public class GoldCoin extends Coin {

    public GoldCoin(int x, int y, Snake snake, Obstacle obstacle) {
        super(x, y, snake, obstacle, CoinType.GOLD);
    }

    // Awards the gold coin value.
    @Override
    public int getPoints() {
        return 15;
    }
}
