package com.comp2042.view;

import com.comp2042.model.PortalPair;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Verifies pure snake sprite geometry and turn tracking without starting JavaFX Toolkit.
@DisplayName("SnakeSpriteRenderer geometry")
class SnakeSpriteRendererTest {
    private static final int BOARD_WIDTH = 20;
    private static final int BOARD_HEIGHT = 20;

    @Test
    @DisplayName("Normal cardinal steps resolve to expected angles and reject non-adjacent cells")
    void adjacentDirectionCoversNormalMovesAndNonAdjacentCells() {
        Point center = new Point(10, 10);

        assertEquals(0, SnakeSpriteRenderer.computeAdjacentDirection(center, new Point(10, 9), BOARD_WIDTH, BOARD_HEIGHT));
        assertEquals(90, SnakeSpriteRenderer.computeAdjacentDirection(center, new Point(11, 10), BOARD_WIDTH, BOARD_HEIGHT));
        assertEquals(180, SnakeSpriteRenderer.computeAdjacentDirection(center, new Point(10, 11), BOARD_WIDTH, BOARD_HEIGHT));
        assertEquals(270, SnakeSpriteRenderer.computeAdjacentDirection(center, new Point(9, 10), BOARD_WIDTH, BOARD_HEIGHT));
        assertEquals(-1, SnakeSpriteRenderer.computeAdjacentDirection(center, new Point(12, 10), BOARD_WIDTH, BOARD_HEIGHT));
        assertEquals(-1, SnakeSpriteRenderer.computeAdjacentDirection(center, new Point(11, 11), BOARD_WIDTH, BOARD_HEIGHT));
        assertEquals(-1, SnakeSpriteRenderer.computeAdjacentDirection(null, center, BOARD_WIDTH, BOARD_HEIGHT));
    }

    @Test
    @DisplayName("Border wrap-around transitions resolve to the matching cardinal angles")
    void adjacentDirectionWrapsAcrossAllFourBoardEdges() {
        assertEquals(0, SnakeSpriteRenderer.computeAdjacentDirection(new Point(5, 0), new Point(5, 19), BOARD_WIDTH, BOARD_HEIGHT));
        assertEquals(180, SnakeSpriteRenderer.computeAdjacentDirection(new Point(5, 19), new Point(5, 0), BOARD_WIDTH, BOARD_HEIGHT));
        assertEquals(270, SnakeSpriteRenderer.computeAdjacentDirection(new Point(0, 8), new Point(19, 8), BOARD_WIDTH, BOARD_HEIGHT));
        assertEquals(90, SnakeSpriteRenderer.computeAdjacentDirection(new Point(19, 8), new Point(0, 8), BOARD_WIDTH, BOARD_HEIGHT));
    }

    @Test
    @DisplayName("Body rotation handles adjacent segments and falls forward or to heading across portal splits")
    void bodyRotationHandlesNormalAndPortalSplitSegments() {
        List<Point> horizontalBody = List.of(new Point(6, 5), new Point(5, 5));
        assertEquals(
                270,
                SnakeSpriteRenderer.computeBodyRotation(horizontalBody, new Point(4, 5), 1, 270, BOARD_WIDTH, BOARD_HEIGHT)
        );

        List<Point> verticalBody = List.of(new Point(5, 6), new Point(5, 5));
        assertEquals(
                0,
                SnakeSpriteRenderer.computeBodyRotation(verticalBody, new Point(5, 4), 1, 0, BOARD_WIDTH, BOARD_HEIGHT)
        );

        // Segment 1 jumped across a portal from segment 0 (2,2) -> (15,10), but is adjacent to head (15,9) vertically.
        List<Point> portalSplitBody = List.of(new Point(2, 2), new Point(15, 10));
        assertEquals(
                0,
                SnakeSpriteRenderer.computeBodyRotation(portalSplitBody, new Point(15, 9), 1, 270, BOARD_WIDTH, BOARD_HEIGHT)
        );

        // Segment 1 is non-adjacent to both previous and head, so it falls back to head direction (horizontal -> 270).
        assertEquals(
                270,
                SnakeSpriteRenderer.computeBodyRotation(portalSplitBody, new Point(18, 18), 1, 90, BOARD_WIDTH, BOARD_HEIGHT)
        );
    }

    @Test
    @DisplayName("Tail rotation follows adjacent and edge-wrapped next segments")
    void tailRotationFollowsAdjacentAndWrappedNextSegments() {
        PortalPair inactivePortal = new PortalPair();

        List<Point> normalBody = List.of(new Point(10, 10), new Point(10, 9));
        assertEquals(
                0,
                SnakeSpriteRenderer.computeTailRotation(normalBody, new Point(10, 8), 0, inactivePortal, BOARD_WIDTH, BOARD_HEIGHT)
        );

        List<Point> wrappedBody = List.of(new Point(0, 4), new Point(19, 4));
        assertEquals(
                270,
                SnakeSpriteRenderer.computeTailRotation(wrappedBody, new Point(18, 4), 270, inactivePortal, BOARD_WIDTH, BOARD_HEIGHT)
        );
    }

    @Test
    @DisplayName("Portal-split tail rotation points toward nearest active portal or falls back to heading")
    void tailRotationFallsBackToNearestPortalOrHeadingWhenSplit() {
        PortalPair portalPair = new PortalPair();
        portalPair.spawnAt(new Point(4, 5), new Point(16, 15));

        // Tail at (3,5) is directly left of portal A (4,5) while next segment jumped to (16,14).
        List<Point> splitNearPortalA = List.of(new Point(3, 5), new Point(16, 14));
        assertEquals(
                90,
                SnakeSpriteRenderer.computeTailRotation(splitNearPortalA, new Point(16, 13), 0, portalPair, BOARD_WIDTH, BOARD_HEIGHT)
        );

        // Tail at (16,18) is closer to portal B (16,15) along the vertical axis -> points up (0).
        List<Point> splitDistantFromPortalB = List.of(new Point(16, 18), new Point(4, 4));
        assertEquals(
                0,
                SnakeSpriteRenderer.computeTailRotation(splitDistantFromPortalB, new Point(4, 3), 90, portalPair, BOARD_WIDTH, BOARD_HEIGHT)
        );

        // Tail standing directly on portal A falls back to current heading (180).
        List<Point> tailOnPortal = List.of(new Point(4, 5), new Point(16, 16));
        assertEquals(
                180,
                SnakeSpriteRenderer.computeTailRotation(tailOnPortal, new Point(16, 17), 180, portalPair, BOARD_WIDTH, BOARD_HEIGHT)
        );
    }

    @Test
    @DisplayName("Turn markers record corners, reset cleanly, and guard board boundaries")
    void turnGridTracksCornerMarkersAndSafelyIgnoresOutOfBounds() {
        SnakeSpriteRenderer renderer = new SnakeSpriteRenderer(BOARD_WIDTH, BOARD_HEIGHT);
        Point corner = new Point(7, 8);

        renderer.turnDir(corner, KeyCode.LEFT, KeyCode.UP);
        assertEquals(2, renderer.getTurn(corner));

        renderer.turnDir(new Point(-1, 0), KeyCode.UP, KeyCode.RIGHT);
        renderer.turnDir((Point) null, KeyCode.UP, KeyCode.RIGHT);
        assertFalse(renderer.isInsideBoard(new Point(BOARD_WIDTH, 0)));
        assertTrue(renderer.isInsideBoard(corner));
        assertEquals(0, renderer.getTurn(new Point(-1, 0)));

        renderer.resetTurns();
        assertEquals(0, renderer.getTurn(corner));
    }
}
