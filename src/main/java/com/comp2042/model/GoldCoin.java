package com.comp2042.model;

/**
 * Represents a high-value gold coin variant.
 */
public class GoldCoin extends Coin {

    /**
     * Spawns a gold coin on an unoccupied board cell.
     *
     * @param x board width in grid cells
     * @param y board height in grid cells
     * @param snake active snake occupying board cells
     * @param obstacle obstacle manager occupying board cells
     */
    public GoldCoin(int x, int y, Snake snake, Obstacle obstacle) {
        super(x, y, snake, obstacle, CoinType.GOLD);
    }

    /**
     * Returns the gold coin's elevated score value.
     *
     * @return {@code 15} points
     */
    @Override
    public int getPoints() {
        return 15;
    }
}
