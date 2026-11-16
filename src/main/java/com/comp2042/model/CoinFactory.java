package com.comp2042.model;

import java.util.Random;

// Centralizes random coin creation.
// New variants can be selected here without controller changes.
public class CoinFactory {
    private final Random random;

    public CoinFactory() {
        random = new Random();
    }

    // Creates a base coin when power-ups are disabled.
    // Uses weighted branches when power-ups are enabled.
    public Coin createCoin(Game game, Snake snake, Obstacle obstacle) {
        if (!game.isPowerUp()) {
            return new Coin(
                    game.getWidth(),
                    game.getHeight(),
                    snake,
                    obstacle
            );
        }

        int roll = random.nextInt(12);
        if (roll == 1) {
            return new GoldCoin(
                    game.getWidth(),
                    game.getHeight(),
                    snake,
                    obstacle
            );
        }
        if (roll == 2) {
            return new RainbowCoin(
                    game.getWidth(),
                    game.getHeight(),
                    snake,
                    obstacle
            );
        }
        if (roll == 3) {
            return new BlueCoin(
                    game.getWidth(),
                    game.getHeight(),
                    snake,
                    obstacle
            );
        }
        if (roll == 4 && obstacle.getObstacles().isEmpty()) {
            return new SilverCoin(
                    game.getWidth(),
                    game.getHeight(),
                    snake,
                    obstacle
            );
        }
        return new Coin(
                game.getWidth(),
                game.getHeight(),
                snake,
                obstacle
        );
    }
}
