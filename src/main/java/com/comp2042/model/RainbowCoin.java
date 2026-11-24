package com.comp2042.model;

/**
 * Represents a rainbow coin that grants a temporary movement speed boost.
 */
public class RainbowCoin extends Coin {
    private static final double SPEED_BONUS = 5;

    /**
     * Spawns a rainbow coin on an unoccupied board cell.
     *
     * @param x board width in grid cells
     * @param y board height in grid cells
     * @param snake active snake occupying board cells
     * @param obstacle obstacle manager occupying board cells
     */
    public RainbowCoin(int x, int y, Snake snake, Obstacle obstacle) {
        super(x, y, snake, obstacle, CoinType.RAINBOW);
    }

    /**
     * Awards base points and adds the rainbow speed bonus to the session's base speed.
     *
     * @param game active game model whose current speed is boosted
     * @param snake active snake that collected the coin
     * @param obstacle obstacle manager for the board
     */
    @Override
    public void applyEffect(Game game, Snake snake, Obstacle obstacle) {
        super.applyEffect(game, snake, obstacle);
        game.setCurrentSpeed(game.getSpeed() + SPEED_BONUS);
    }

    /**
     * Returns the additional ticks per second granted by this coin.
     *
     * @return movement speed bonus
     */
    public double getSpeedBonus() {
        return SPEED_BONUS;
    }
}
