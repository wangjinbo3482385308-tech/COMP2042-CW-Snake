package com.comp2042.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

// Owns snake movement and collision rules.
// Contains no image or color state.
public class Snake {
    private final Point head;
    private final List<Point> body;
    private final int gridWidth;
    private final int gridHeight;
    private final Obstacle obstacle;

    private boolean dead;
    private boolean ignoreBorders;
    private final boolean noBorder;
    private int snakeDir;

    // Creates a centered snake and its initial tail.
    public Snake(int x, int y, boolean noBorder, Obstacle obstacle) {
        this.obstacle = obstacle;
        this.noBorder = noBorder;
        head = new Point(x / 2, y / 2);
        body = new ArrayList<>();
        body.add(new Point(x / 2 + 1, y / 2));
        gridWidth = x;
        gridHeight = y;
        snakeDir = 270;
    }

    // Moves the head in one grid direction.
    // Applies border, obstacle, and self collisions.
    public void move(char direction) {
        body.add(new Point(head));

        switch (direction) {
            case 'U' -> moveHead(0, -1, 0);
            case 'D' -> moveHead(0, 1, 180);
            case 'L' -> moveHead(-1, 0, 270);
            case 'R' -> moveHead(1, 0, 90);
            default -> {
            }
        }

        if (hitSelf() && !body.get(0).equals(head)) {
            dead = true;
        }
    }

    // Applies one movement vector and its border behavior.
    private void moveHead(int deltaX, int deltaY, int direction) {
        snakeDir = direction;
        head.translate(deltaX, deltaY);

        if (isOutsideBoard() && (!ignoreBorders && !noBorder)
                || obstacle.getObstacles().contains(head)) {
            head.translate(-deltaX, -deltaY);
            dead = true;
            return;
        }

        if (isOutsideBoard() && (ignoreBorders || noBorder)) {
            wrapHead(deltaX, deltaY);
            if (obstacle.getObstacles().contains(head)) {
                dead = true;
            }
        }
    }

    // Places a wrapped head on the opposite board edge.
    private void wrapHead(int deltaX, int deltaY) {
        if (deltaY < 0) {
            head.setLocation(head.getX(), gridHeight - 1);
        } else if (deltaY > 0) {
            head.setLocation(head.getX(), 0);
        } else if (deltaX < 0) {
            head.setLocation(gridWidth - 1, head.getY());
        } else if (deltaX > 0) {
            head.setLocation(0, head.getY());
        }
    }

    // Reports grid coordinates outside the playable board.
    private boolean isOutsideBoard() {
        return head.getX() < 0
                || head.getY() < 0
                || head.getX() >= gridWidth
                || head.getY() >= gridHeight;
    }

    // Returns a body segment's straight orientation.
    public int getBodyDir(int bodyCount) {
        if (bodyCount == 0) {
            return 0;
        }
        boolean horizontal = body.get(bodyCount).getY()
                == body.get(bodyCount - 1).getY();
        return horizontal ? 270 : 0;
    }

    // Removes the tail unless the head entered the coin cell.
    public boolean ateCoin(Coin coin) {
        boolean ate = head.equals(coin.getPos());
        if (!ate) {
            body.remove(0);
        }
        return ate;
    }

    // Reports whether the head overlaps the body.
    public boolean hitSelf() {
        return body.contains(head);
    }

    public List<Point> getBody() {
        return new ArrayList<>(body);
    }

    public Point getHead() {
        return new Point(head);
    }

    public boolean getDead() {
        return dead;
    }

    // Enables or disables temporary border wrapping.
    public void ignoreBorders(boolean ignore) {
        ignoreBorders = ignore;
    }

    public int getSnakeDir() {
        return snakeDir;
    }
}
