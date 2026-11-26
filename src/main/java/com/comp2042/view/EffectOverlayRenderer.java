package com.comp2042.view;

import com.comp2042.model.PlayerInventory;
import com.comp2042.model.PortalPair;
import com.comp2042.model.Snake;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.awt.Point;
import java.util.Locale;

/**
 * Renders vector gameplay overlays on the board canvas, including the magnet influence area,
 * magnet countdown banner, shield halo around the snake head, and paired portal rings.
 */
public class EffectOverlayRenderer {

    /**
     * Creates a stateless vector overlay renderer.
     */
    public EffectOverlayRenderer() {
    }

    /**
     * Draws the translucent 5x5 magnet attraction zone clipped to board bounds.
     *
     * @param gc target canvas graphics context
     * @param snake active snake providing the head cell
     * @param boardWidth board width in grid cells
     * @param boardHeight board height in grid cells
     * @param cellSize rendered size of one board cell in canvas pixels
     * @param scale active canvas scale factor
     */
    public void drawMagnetRange(
            GraphicsContext gc,
            Snake snake,
            int boardWidth,
            int boardHeight,
            double cellSize,
            double scale
    ) {
        Point head = snake.getHead();
        int minX = Math.max(0, head.x - PlayerInventory.MAGNET_RADIUS);
        int maxX = Math.min(boardWidth - 1, head.x + PlayerInventory.MAGNET_RADIUS);
        int minY = Math.max(0, head.y - PlayerInventory.MAGNET_RADIUS);
        int maxY = Math.min(boardHeight - 1, head.y + PlayerInventory.MAGNET_RADIUS);

        double x = minX * cellSize;
        double y = minY * cellSize;
        double width = (maxX - minX + 1) * cellSize;
        double height = (maxY - minY + 1) * cellSize;
        double lineWidth = Math.max(1.0, 1.5 * scale);

        gc.setFill(Color.rgb(255, 215, 0, 0.20));
        gc.fillRect(x, y, width, height);
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(lineWidth);
        gc.strokeRect(
                x + lineWidth / 2,
                y + lineWidth / 2,
                Math.max(0, width - lineWidth),
                Math.max(0, height - lineWidth)
        );
    }

    /**
     * Draws the active portal pair as concentric blue (portal A) and orange (portal B) rings,
     * highlighting both rings when the snake is traversing the portal.
     *
     * @param gc target canvas graphics context
     * @param portalPair active portal pair providing endpoint coordinates and traversal state
     * @param boardWidth board width in grid cells
     * @param boardHeight board height in grid cells
     * @param cellSize rendered size of one board cell in canvas pixels
     * @param scale active canvas scale factor
     */
    public void drawPortals(
            GraphicsContext gc,
            PortalPair portalPair,
            int boardWidth,
            int boardHeight,
            double cellSize,
            double scale
    ) {
        Point portalA = portalPair.getPortalA();
        Point portalB = portalPair.getPortalB();
        boolean traversing = portalPair.isTraversing();

        if (isInsideBoard(portalA, boardWidth, boardHeight)) {
            drawPortalRing(
                    gc,
                    portalA,
                    Color.DEEPSKYBLUE,
                    Color.LIGHTCYAN,
                    Color.rgb(0, 191, 255, 0.25),
                    traversing,
                    scale,
                    cellSize
            );
        }

        if (isInsideBoard(portalB, boardWidth, boardHeight)) {
            drawPortalRing(
                    gc,
                    portalB,
                    Color.DARKORANGE,
                    Color.GOLD,
                    Color.rgb(255, 140, 0, 0.25),
                    traversing,
                    scale,
                    cellSize
            );
        }
    }

    // Draws concentric rings inside one portal cell with optional warp glow.
    private void drawPortalRing(
            GraphicsContext gc,
            Point cell,
            Color outerColor,
            Color innerColor,
            Color glowColor,
            boolean traversing,
            double scale,
            double cellSize
    ) {
        double x = cell.x * cellSize;
        double y = cell.y * cellSize;

        if (traversing) {
            gc.setFill(glowColor);
            gc.fillOval(x, y, cellSize, cellSize);
        }

        double outerStroke = Math.max(1.5, (traversing ? 2.8 : 2.0) * scale);
        double outerInset = Math.max(1.0, cellSize * 0.10);
        double outerDiameter = Math.max(1.0, cellSize - outerInset * 2);
        gc.setStroke(outerColor);
        gc.setLineWidth(outerStroke);
        gc.strokeOval(
                x + outerInset,
                y + outerInset,
                outerDiameter,
                outerDiameter
        );

        double innerStroke = Math.max(1.2, (traversing ? 2.2 : 1.5) * scale);
        double innerInset = Math.max(2.5, cellSize * 0.27);
        double innerDiameter = Math.max(1.0, cellSize - innerInset * 2);
        gc.setStroke(innerColor);
        gc.setLineWidth(innerStroke);
        gc.strokeOval(
                x + innerInset,
                y + innerInset,
                innerDiameter,
                innerDiameter
        );
    }

    /**
     * Draws a cyan shield halo ring around the snake's head cell.
     *
     * @param gc target canvas graphics context
     * @param snake active snake providing the head cell
     * @param cellSize rendered size of one board cell in canvas pixels
     * @param scale active canvas scale factor
     */
    public void drawShieldHalo(
            GraphicsContext gc,
            Snake snake,
            double cellSize,
            double scale
    ) {
        Point head = snake.getHead();
        double inset = Math.max(1.5, cellSize * 0.08);
        double diameter = Math.max(1.0, cellSize - inset * 2);

        gc.setStroke(Color.CYAN);
        gc.setLineWidth(Math.max(1.5, 2.0 * scale));
        gc.strokeOval(
                head.x * cellSize + inset,
                head.y * cellSize + inset,
                diameter,
                diameter
        );
    }

    /**
     * Draws the live magnet countdown banner in the upper-right corner of the canvas.
     *
     * @param gc target canvas graphics context
     * @param inventory player inventory supplying remaining magnet duration
     * @param canvasWidth rendered canvas width in pixels
     * @param cellSize rendered size of one board cell in canvas pixels
     * @param scale active canvas scale factor
     */
    public void drawMagnetTimer(
            GraphicsContext gc,
            PlayerInventory inventory,
            double canvasWidth,
            double cellSize,
            double scale
    ) {
        if (!inventory.isMagnetActive()) {
            return;
        }

        double bannerWidth = Math.min(canvasWidth - cellSize * 2, 9 * cellSize);
        double bannerHeight = Math.max(cellSize, 18 * scale);
        double x = canvasWidth - bannerWidth - cellSize * 0.35;
        double y = cellSize * 0.35;
        String text = String.format(
                Locale.ROOT,
                "MAGNET %.1fs",
                inventory.getMagnetTimeRemaining()
        );

        gc.setFill(Color.rgb(255, 215, 0, 0.94));
        gc.fillRoundRect(
                x,
                y,
                bannerWidth,
                bannerHeight,
                cellSize * 0.35,
                cellSize * 0.35
        );
        gc.setStroke(Color.rgb(154, 107, 0));
        gc.setLineWidth(Math.max(1.0, scale));
        gc.strokeRoundRect(
                x,
                y,
                bannerWidth,
                bannerHeight,
                cellSize * 0.35,
                cellSize * 0.35
        );
        gc.setFill(Color.rgb(30, 24, 0));
        gc.setFont(Font.font(
                "Arial",
                FontWeight.BOLD,
                Math.max(8.0, 12.0 * scale)
        ));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(text, x + cellSize * 0.35, y + bannerHeight * 0.70);
    }

    private boolean isInsideBoard(Point point, int boardWidth, int boardHeight) {
        return point != null
                && point.x >= 0
                && point.x < boardWidth
                && point.y >= 0
                && point.y < boardHeight;
    }
}
