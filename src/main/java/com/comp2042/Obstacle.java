package com.comp2042;

import javafx.scene.paint.Color;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Obstacle {
    private Color color = Color.BLACK;
    private ArrayList<Point> obstacles = new ArrayList<>();
    private int x;
    private int y;

    public Obstacle(int x, int y) {
       this.x = x;
       this.y = y;
    }

    public void newObstacles(Snake snek, Coin coin, int amount) {
        Random rnd = new Random();
        Point p;
        // Enforce the hard limit before every spawn.
        // Keep x coordinates within valid grid bounds.
        // Avoid collisions with existing obstacles.
        // Retry any invalid candidate.
        for (int i=0; i<amount; i++) {
            if (obstacles.size() >= 50) {
                return;
            }
            do {
                p = new Point(rnd.nextInt(x), rnd.nextInt(y));
            } while (coin.getPos().equals(p) || snek.getBody().contains(p) || snek.getHead().equals(p) || obstacles.contains(p));
            obstacles.add(p);
        }
    }

    public Color getColor() {
        return color;
    }

    public ArrayList<Point> getObstacles() {
        return obstacles;
    }

    public void reset() {
        obstacles.clear();
    }
}
