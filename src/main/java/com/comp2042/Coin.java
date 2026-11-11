package com.comp2042;

import javafx.scene.image.Image;

import java.awt.*;
import java.util.Random;

public class Coin {
    private Point pos;
    private Image image = new Image("resources/views/res/redcoin.png");

    public Coin(int x, int y, Snake snek, Obstacle obstacle) {
        int tx;
        int ty;
        Random rnd = new Random();
        // Cap random placement attempts for a full board.
        // Stop after a bounded number of collisions.
        // Mark the coin unavailable if no valid cell is found.
        int maxAttempts = x * y * 2;
        int attempts = 0;
        do {
            tx = rnd.nextInt(x);
            ty = rnd.nextInt(y);
            pos = new Point(tx, ty);
            attempts++;
        } while ((snek.getHead().equals(pos) || snek.getBody().contains(pos) || obstacle.getObstacles().contains(pos))
                && attempts < maxAttempts);

        if (snek.getHead().equals(pos) || snek.getBody().contains(pos) || obstacle.getObstacles().contains(pos)) {
            pos = new Point(-1, -1);
        }
    }

    public Point getPos() {
        return new Point(pos);
    }

    public int getPoints() {
        int points = 5;
        return points;
    }

    public Image getImage() {
        return image;
    }
}
