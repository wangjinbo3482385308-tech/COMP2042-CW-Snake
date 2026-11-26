package com.comp2042.model;

import java.awt.Point;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Defines selectable preset level maps and builds their static obstacle layouts
 * while keeping the snake's initial spawn zone and forward path unobstructed.
 */
public enum LevelMap {
    /** Classic open board with no preset obstacles. */
    CLASSIC("Classic"),
    /** Symmetric four-pillar layout with one block in each quadrant. */
    PILLARS("Pillars"),
    /** Symmetric corridor arena with corner walls, inner baffles, and wide passages. */
    ARENA("Arena");

    private static final int SAFE_AHEAD_CELLS = 4;

    private final String displayName;

    // Stores the immutable menu and HUD label for this preset map.
    LevelMap(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the display label shown in the main menu and HUD.
     *
     * @return level map display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Builds the static preset obstacle coordinates for a board of the given dimensions.
     * All returned coordinates are unique, strictly inside {@code [0, width) x [0, height)},
     * and clear of the snake's initial body, head, and forward spawn lane.
     *
     * @param width board width in grid cells
     * @param height board height in grid cells
     * @return unmodifiable list of preset obstacle grid coordinates
     */
    public List<Point> buildObstacles(int width, int height) {
        if (width <= 0 || height <= 0 || this == CLASSIC) {
            return Collections.emptyList();
        }

        Set<Point> obstacles = new LinkedHashSet<>();
        if (this == PILLARS) {
            buildPillars(obstacles, width, height);
        } else if (this == ARENA) {
            buildArena(obstacles, width, height);
        }
        return List.copyOf(obstacles);
    }

    // Places four symmetric pillars in the board quadrants.
    private static void buildPillars(
            Set<Point> obstacles,
            int width,
            int height
    ) {
        if (width < 6 || height < 6) {
            return;
        }

        int pillarSize = (width >= 12 && height >= 12) ? 2 : 1;
        int leftX = width >= 12 ? Math.max(2, width / 4 - 1) : 1;
        int rightX = width - pillarSize - leftX;
        int topY = height >= 12 ? Math.max(2, height / 4 - 1) : 1;
        int bottomY = height - pillarSize - topY;

        if (leftX + pillarSize > rightX || topY + pillarSize > bottomY) {
            return;
        }

        addBlock(obstacles, leftX, topY, pillarSize, width, height);
        addBlock(obstacles, rightX, topY, pillarSize, width, height);
        addBlock(obstacles, leftX, bottomY, pillarSize, width, height);
        addBlock(obstacles, rightX, bottomY, pillarSize, width, height);
    }

    // Places four symmetric corner corridors and optional inner horizontal baffles.
    private static void buildArena(
            Set<Point> obstacles,
            int width,
            int height
    ) {
        if (width < 8 || height < 8) {
            return;
        }

        int marginX = Math.max(2, width / 6);
        int marginY = Math.max(2, height / 6);
        int maxArmX = Math.max(1, (width - 2 * marginX - 2) / 2);
        int maxArmY = Math.max(1, (height - 2 * marginY - 2) / 2);
        int armX = Math.min(Math.max(2, width / 6), maxArmX);
        int armY = Math.min(Math.max(2, height / 6), maxArmY);

        int leftX = marginX;
        int rightX = width - 1 - marginX;
        int topY = marginY;
        int bottomY = height - 1 - marginY;

        for (int offset = 0; offset < armX; offset++) {
            addIfValid(obstacles, leftX + offset, topY, width, height);
            addIfValid(obstacles, rightX - offset, topY, width, height);
            addIfValid(obstacles, leftX + offset, bottomY, width, height);
            addIfValid(obstacles, rightX - offset, bottomY, width, height);
        }

        for (int offset = 1; offset < armY; offset++) {
            addIfValid(obstacles, leftX, topY + offset, width, height);
            addIfValid(obstacles, rightX, topY + offset, width, height);
            addIfValid(obstacles, leftX, bottomY - offset, width, height);
            addIfValid(obstacles, rightX, bottomY - offset, width, height);
        }

        int centerY = height / 2;
        int innerTopY = topY + armY;
        int innerBottomY = height - 1 - innerTopY;
        if (innerTopY < centerY - 1 && innerBottomY > centerY + 1) {
            int barLength = Math.max(2, width / 5);
            int barStart = (width - barLength) / 2;
            for (int offset = 0; offset < barLength; offset++) {
                addIfValid(obstacles, barStart + offset, innerTopY, width, height);
                addIfValid(obstacles, barStart + offset, innerBottomY, width, height);
            }
        }
    }

    // Adds a square obstacle block anchored at (startX, startY).
    private static void addBlock(
            Set<Point> obstacles,
            int startX,
            int startY,
            int size,
            int width,
            int height
    ) {
        for (int dx = 0; dx < size; dx++) {
            for (int dy = 0; dy < size; dy++) {
                addIfValid(obstacles, startX + dx, startY + dy, width, height);
            }
        }
    }

    // Adds a coordinate only if it is within bounds and outside the safe spawn lane.
    private static void addIfValid(
            Set<Point> obstacles,
            int x,
            int y,
            int width,
            int height
    ) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }
        if (isInSafeSpawnZone(x, y, width, height)) {
            return;
        }
        obstacles.add(new Point(x, y));
    }

    // Protects the initial head (centerX, centerY), initial body (centerX + 1, centerY),
    // and at least four cells directly ahead along the initial LEFT direction.
    private static boolean isInSafeSpawnZone(
            int x,
            int y,
            int width,
            int height
    ) {
        int centerX = width / 2;
        int centerY = height / 2;
        return y == centerY
                && x >= centerX - SAFE_AHEAD_CELLS
                && x <= centerX + 1;
    }
}
