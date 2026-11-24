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
    private boolean selfCollision;
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
        move(direction, null);
    }

    // Moves with optional shield protection.
    // Keeps the legacy movement contract intact.
    public void move(char direction, PlayerInventory inventory) {
        Point previousHead = new Point(head);
        selfCollision = false;
        body.add(new Point(head));

        switch (direction) {
            case 'U' -> moveHead(0, -1, 0, inventory);
            case 'D' -> moveHead(0, 1, 180, inventory);
            case 'L' -> moveHead(-1, 0, 270, inventory);
            case 'R' -> moveHead(1, 0, 90, inventory);
            default -> {
            }
        }

        // Stop immediately after unresolved terrain collisions.
        // Restore the pending segment to keep the death frame stable.
        if (dead) {
            body.remove(body.size() - 1);
            return;
        }

        if (hitSelf() && !body.get(0).equals(head)) {
            if (inventory != null && inventory.consumeShield()) {
                head.setLocation(previousHead);
                body.remove(body.size() - 1);
            } else {
                selfCollision = true;
                dead = true;
            }
        }

        // Undo the pending body increment.
        // Keep the death frame consistent.
        if (dead && !body.isEmpty()) {
            body.remove(body.size() - 1);
        }
    }

    // Teleports the head to an exact destination.
    // Applies obstacle and self collision rules.
    // A shield absorbs a collision safely.
    public void teleportHeadTo(
            Point destination,
            Obstacle obstacle,
            PlayerInventory inventory
    ) {
        if (destination == null) {
            throw new IllegalArgumentException(
                    "Teleport destination must be non-null."
            );
        }

        Point previousHead = new Point(head);
        selfCollision = false;
        head.setLocation(new Point(destination));

        if (obstacle != null
                && obstacle.getObstacles().contains(head)) {
            if (inventory != null && inventory.consumeShield()) {
                obstacle.removeObstacleAt(head);
            } else {
                head.setLocation(previousHead);
                dead = true;
                return;
            }
        }

        if (hitSelf()) {
            if (inventory != null && inventory.consumeShield()) {
                head.setLocation(previousHead);
            } else {
                selfCollision = true;
                dead = true;
            }
        }
    }

    // Applies one movement vector and its border behavior.
    private void moveHead(
            int deltaX,
            int deltaY,
            int direction,
            PlayerInventory inventory
    ) {
        snakeDir = direction;
        head.translate(deltaX, deltaY);

        if (isOutsideBoard()) {
            if (ignoreBorders || noBorder) {
                wrapHead(deltaX, deltaY);
                resolveObstacleAtWrappedHead(inventory);
            } else if (inventory != null && inventory.consumeShield()) {
                wrapHead(deltaX, deltaY);
                obstacle.removeObstacleAt(head);
            } else {
                head.translate(-deltaX, -deltaY);
                dead = true;
            }
            return;
        }

        if (obstacle.getObstacles().contains(head)) {
            if (inventory != null && inventory.consumeShield()) {
                obstacle.removeObstacleAt(head);
                return;
            }

            head.translate(-deltaX, -deltaY);
            dead = true;
        }
    }

    // Resolves an obstacle at a wrapped destination.
    // A shield clears the landing cell.
    private void resolveObstacleAtWrappedHead(PlayerInventory inventory) {
        if (!obstacle.getObstacles().contains(head)) {
            return;
        }

        if (inventory != null && inventory.consumeShield()) {
            obstacle.removeObstacleAt(head);
        } else {
            dead = true;
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

    // Handles direct contact or magnet-assisted collection.
    // Keeps the pending segment for either collection path.
    // Removes the tail only when the coin is out of reach.
    public boolean collectCoin(Coin coin, PlayerInventory inventory) {
        Point coinPosition = coin.getPos();
        boolean directCollection = head.equals(coinPosition);
        boolean magnetCollection = inventory != null
                && inventory.isWithinMagnetRange(head, coinPosition);

        if (!directCollection && !magnetCollection) {
            body.remove(0);
            return false;
        }
        return true;
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

    // Revives at the board center.
    // Rebuilds half the body along the right side.
    public void reviveAtCenter(int width, int height, Obstacle obstacle) {
        int centerX = width / 2;
        int centerY = height / 2;
        dead = false;
        selfCollision = false;
        snakeDir = 270;
        head.setLocation(centerX, centerY);

        if (obstacle != null) {
            obstacle.clearAreaAround(head, 1);
        }

        int requestedSize = Math.max(1, body.size() / 2);
        int availableCells = Math.max(0, width - 1 - centerX);
        int newBodySize = Math.min(requestedSize, availableCells);
        body.clear();

        // Keep the established tail-to-head order.
        // Farthest cells are stored first.
        for (int offset = newBodySize; offset >= 1; offset--) {
            body.add(new Point(centerX + offset, centerY));
        }
    }

    // Enables or disables temporary border wrapping.
    public void ignoreBorders(boolean ignore) {
        ignoreBorders = ignore;
    }

    public int getSnakeDir() {
        return snakeDir;
    }
}
