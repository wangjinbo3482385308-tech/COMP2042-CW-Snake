package com.comp2042.model;

import java.util.Random;

/**
 * Centralizes random coin creation so new variants can be added without modifying the controller.
 */
public class CoinFactory {
    private final Random random;

    /**
     * Creates a coin factory backed by a default random generator.
     */
    public CoinFactory() {
        random = new Random();
    }

    /**
     * Creates the next coin for the board.
     * Returns a standard coin when power-ups are disabled, or rolls weighted special variants otherwise.
     *
     * @param game active game model providing board size and power-up mode
     * @param snake active snake occupying board cells
     * @param obstacle obstacle manager occupying board cells
     * @return newly spawned {@link Coin} instance
     */
    public Coin createCoin(Game game, Snake snake, Obstacle obstacle) {
        PortalPair portalPair = game.getPortalPair();
        if (!game.isPowerUp()) {
            return new Coin(
                    game.getWidth(),
                    game.getHeight(),
                    snake,
                    obstacle,
                    portalPair
            );
        }

        int roll = random.nextInt(12);
        if (roll == 1) {
            return new GoldCoin(
                    game.getWidth(),
                    game.getHeight(),
                    snake,
                    obstacle,
                    portalPair
            );
        }
        if (roll == 2) {
            return new RainbowCoin(
                    game.getWidth(),
                    game.getHeight(),
                    snake,
                    obstacle,
                    portalPair
            );
        }
        if (roll == 3) {
            return new BlueCoin(
                    game.getWidth(),
                    game.getHeight(),
                    snake,
                    obstacle,
                    portalPair
            );
        }
        if (roll == 4 && !obstacle.hasDynamicObstacles()) {
            return new SilverCoin(
                    game.getWidth(),
                    game.getHeight(),
                    snake,
                    obstacle,
                    portalPair
            );
        }
        return new Coin(
                game.getWidth(),
                game.getHeight(),
                snake,
                obstacle,
                portalPair
        );
    }
}
