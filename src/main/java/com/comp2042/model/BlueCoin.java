package com.comp2042.model;

/**
 * Represents a blue coin that temporarily enables border wrapping when collected.
 */
public class BlueCoin extends Coin {

    /**
     * Spawns a blue coin on an unoccupied board cell.
     *
     * @param x board width in grid cells
     * @param y board height in grid cells
     * @param snake active snake occupying board cells
     * @param obstacle obstacle manager occupying board cells
     */
    public BlueCoin(int x, int y, Snake snake, Obstacle obstacle) {
        super(x, y, snake, obstacle, CoinType.BLUE);
    }

    /**
     * Awards base points and enables border wrapping until the next coin or reset.
     *
     * @param game active game model
     * @param snake active snake receiving border wrapping
     * @param obstacle obstacle manager for the board
     */
    @Override
    public void applyEffect(Game game, Snake snake, Obstacle obstacle) {
        super.applyEffect(game, snake, obstacle);
        snake.ignoreBorders(true);
    }
}
