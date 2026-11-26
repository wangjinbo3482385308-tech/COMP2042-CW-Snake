package com.comp2042.model;

import java.util.Random;

/**
 * Represents a silver coin that spawns new board obstacles when collected.
 */
public class SilverCoin extends Coin {

    /**
     * Spawns a silver coin on an unoccupied board cell.
     *
     * @param x board width in grid cells
     * @param y board height in grid cells
     * @param snake active snake occupying board cells
     * @param obstacle obstacle manager occupying board cells
     */
    public SilverCoin(int x, int y, Snake snake, Obstacle obstacle) {
        this(x, y, snake, obstacle, null);
    }

    /**
     * Spawns a silver coin on an unoccupied board cell while avoiding active portals.
     *
     * @param x board width in grid cells
     * @param y board height in grid cells
     * @param snake active snake occupying board cells
     * @param obstacle obstacle manager occupying board cells
     * @param portalPair active portal pair to avoid, or {@code null} when unused
     */
    public SilverCoin(
            int x,
            int y,
            Snake snake,
            Obstacle obstacle,
            PortalPair portalPair
    ) {
        super(x, y, snake, obstacle, CoinType.SILVER, portalPair);
    }

    /**
     * Awards base points, spawns new obstacles, and lowers the win target by the added count.
     *
     * @param game active game model whose win target is adjusted
     * @param snake active snake occupying board cells
     * @param obstacle obstacle manager receiving the new obstacles
     */
    @Override
    public void applyEffect(Game game, Snake snake, Obstacle obstacle) {
        super.applyEffect(game, snake, obstacle);

        int maxRange = Math.max(1, game.getWidth() * game.getHeight() / 20);
        int amount = new Random().nextInt(maxRange) + 3;
        int previousCount = obstacle.getObstacles().size();

        obstacle.newObstacles(snake, this, amount, game.getPortalPair());
        int addedCount = obstacle.getObstacles().size() - previousCount;
        game.decMaxScore(addedCount);
    }
}
