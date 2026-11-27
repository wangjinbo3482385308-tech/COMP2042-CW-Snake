package com.comp2042.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages snake coordinates, movement, growth, teleportation, revival, and collision rules.
 * Contains no rendering or JavaFX state.
 */
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

    /**
     * Creates a snake centered on the board facing left with one initial tail segment.
     *
     * @param x board width in grid cells
     * @param y board height in grid cells
     * @param noBorder {@code true} to wrap across board edges permanently
     * @param obstacle obstacle manager checked during movement
     */
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

    /**
     * Moves the head by one grid cell without shield protection.
     * Applies border, obstacle, and self collision rules.
     *
     * @param direction movement code ({@code U}, {@code D}, {@code L}, or {@code R})
     * @return {@code true} when the snake advances by one cell
     */
    public boolean move(char direction) {
        return move(direction, null);
    }

    /**
     * Moves the head by one grid cell with optional shield protection.
     * Rolls back head, direction, and body when a shield blocks self-collision.
     *
     * @param direction movement code ({@code U}, {@code D}, {@code L}, or {@code R})
     * @param inventory active player inventory, or {@code null} when unused
     * @return {@code true} when the snake advances by one cell; {@code false} on death or shield rollback
     */
    public boolean move(char direction, PlayerInventory inventory) {
        Point previousHead = new Point(head);
        int previousDir = snakeDir;
        selfCollision = false;
        body.add(new Point(head));

        boolean shieldWallRollback;
        switch (direction) {
            case 'U' -> shieldWallRollback = moveHead(0, -1, 0, inventory);
            case 'D' -> shieldWallRollback = moveHead(0, 1, 180, inventory);
            case 'L' -> shieldWallRollback = moveHead(-1, 0, 270, inventory);
            case 'R' -> shieldWallRollback = moveHead(1, 0, 90, inventory);
            default -> {
                body.remove(body.size() - 1);
                return false;
            }
        }

        // Roll back in place after spending a single shield when a wall wrap would self-collide.
        if (shieldWallRollback) {
            head.setLocation(previousHead);
            snakeDir = previousDir;
            body.remove(body.size() - 1);
            dead = false;
            return false;
        }

        // Stop immediately after unresolved terrain collisions.
        // Restore the pending segment to keep the death frame stable.
        if (dead) {
            body.remove(body.size() - 1);
            return false;
        }

        if (hitSelf() && !body.get(0).equals(head)) {
            if (inventory != null && inventory.consumeShield()) {
                head.setLocation(previousHead);
                snakeDir = previousDir;
                body.remove(body.size() - 1);
                return false;
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

        return !dead;
    }

    /**
     * Teleports the snake head to an exact cell and resolves obstacle or self collisions.
     * Consumes a shield charge to clear an obstacle or roll back a self collision when available.
     *
     * @param destination target grid cell for the head
     * @param obstacle obstacle manager checked at the destination, or {@code null}
     * @param inventory active player inventory for shield protection, or {@code null}
     * @throws IllegalArgumentException if {@code destination} is {@code null}
     */
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
    // Returns true when a shield-protected wall hit must roll back in place
    // because the wrapped landing cell is occupied by a non-tail body segment.
    private boolean moveHead(
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
                Point wrappedTarget = computeWrappedHead(deltaX, deltaY);
                if (hitsNonTailBody(wrappedTarget)) {
                    return true;
                }
                head.setLocation(wrappedTarget);
                obstacle.removeObstacleAt(head);
            } else {
                head.translate(-deltaX, -deltaY);
                dead = true;
            }
            return false;
        }

        if (obstacle.getObstacles().contains(head)) {
            if (inventory != null && inventory.consumeShield()) {
                obstacle.removeObstacleAt(head);
                return false;
            }

            head.translate(-deltaX, -deltaY);
            dead = true;
        }
        return false;
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
        head.setLocation(computeWrappedHead(deltaX, deltaY));
    }

    // Computes the opposite-edge coordinate for an out-of-bounds movement vector.
    private Point computeWrappedHead(int deltaX, int deltaY) {
        if (deltaY < 0) {
            return new Point(head.x, gridHeight - 1);
        }
        if (deltaY > 0) {
            return new Point(head.x, 0);
        }
        if (deltaX < 0) {
            return new Point(gridWidth - 1, head.y);
        }
        if (deltaX > 0) {
            return new Point(0, head.y);
        }
        return new Point(head);
    }

    // Checks whether a landing cell overlaps any non-tail body segment.
    // Skips index 0 because the tail vacates unless the snake grows.
    private boolean hitsNonTailBody(Point target) {
        for (int index = 1; index < body.size(); index++) {
            if (body.get(index).equals(target)) {
                return true;
            }
        }
        return false;
    }

    // Reports grid coordinates outside the playable board.
    private boolean isOutsideBoard() {
        return head.getX() < 0
                || head.getY() < 0
                || head.getX() >= gridWidth
                || head.getY() >= gridHeight;
    }

    /**
     * Returns the straight sprite rotation angle for the specified body segment index.
     *
     * @param bodyCount index into the body segment list
     * @return {@code 270} for horizontal segments or {@code 0} for vertical segments
     */
    public int getBodyDir(int bodyCount) {
        if (bodyCount == 0) {
            return 0;
        }
        boolean horizontal = body.get(bodyCount).getY()
                == body.get(bodyCount - 1).getY();
        return horizontal ? 270 : 0;
    }

    /**
     * Checks direct head-to-coin contact and removes the tail segment if the coin was not eaten.
     *
     * @param coin active coin on the board
     * @return {@code true} if the head occupies the coin cell
     */
    public boolean ateCoin(Coin coin) {
        boolean ate = head.equals(coin.getPos());
        if (!ate) {
            body.remove(0);
        }
        return ate;
    }

    /**
     * Checks direct contact or active magnet range and removes the tail only when uncollected.
     *
     * @param coin active coin on the board
     * @param inventory player inventory providing magnet range, or {@code null}
     * @return {@code true} if the coin was collected and the snake grew
     */
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

    /**
     * Reports whether the snake head currently overlaps any body segment.
     *
     * @return {@code true} if the head collides with the body
     */
    public boolean hitSelf() {
        return body.contains(head);
    }

    /**
     * Returns a shallow copy of the snake's body coordinates in tail-to-head order.
     *
     * @return list of body segment points
     */
    public List<Point> getBody() {
        return new ArrayList<>(body);
    }

    /**
     * Returns a defensive copy of the snake head's grid position.
     *
     * @return current head coordinates
     */
    public Point getHead() {
        return new Point(head);
    }

    /**
     * Reports whether the snake has died.
     *
     * @return {@code true} if the snake is dead
     */
    public boolean getDead() {
        return dead;
    }

    /**
     * Revives the snake at the board center facing left, halves its body length,
     * and clears obstacles around the head and along every revived body segment.
     *
     * @param width board width in grid cells
     * @param height board height in grid cells
     * @param obstacle obstacle manager whose cells around the center and revived body are cleared, or {@code null}
     */
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

        // Clear any obstacle underneath the rebuilt body segments.
        if (obstacle != null) {
            for (Point segment : body) {
                obstacle.removeObstacleAt(segment);
            }
        }
    }

    /**
     * Enables or disables temporary border wrapping granted by a blue coin.
     *
     * @param ignore {@code true} to wrap across board borders temporarily
     */
    public void ignoreBorders(boolean ignore) {
        ignoreBorders = ignore;
    }

    /**
     * Returns the current head heading angle in degrees ({@code 0}, {@code 90}, {@code 180}, or {@code 270}).
     *
     * @return head rotation angle in degrees
     */
    public int getSnakeDir() {
        return snakeDir;
    }
}
