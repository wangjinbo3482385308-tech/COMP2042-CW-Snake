package com.comp2042.view;

import com.comp2042.model.Game;
import com.comp2042.model.PlayerInventory;
import com.comp2042.model.PortalPair;
import com.comp2042.model.PowerUpType;
import com.comp2042.model.Snake;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.net.URL;
import java.util.Locale;

/**
 * Manages the top HUD status bar, centered game-state overlay labels,
 * stylesheet binding, and responsive font scaling for windowed and fullscreen modes.
 */
public class GameHudManager {
    /** Outer padding applied around the game pane in pixels. */
    public static final double PANE_PADDING = 20.0;
    /** Base height reserved for the two-line top HUD label at 1x scale. */
    public static final double HUD_LABEL_HEIGHT = 48.0;
    /** Vertical spacing between the top HUD label and the board canvas at 1x scale. */
    public static final double HUD_SPACING = 8.0;
    /** Total extra height reserved above the board canvas for the non-overlapping HUD bar. */
    public static final double HUD_RESERVED_HEIGHT = HUD_LABEL_HEIGHT + HUD_SPACING;

    private final Game game;
    private final Label gameOverLabel;
    private final Label gameWonLabel;
    private final Label pausedLabel;
    private final Label scoreLabel;

    private VBox layoutRoot;
    private boolean gameOverVisible;
    private boolean gameWonVisible;
    private boolean pausedVisible;

    /**
     * Creates and styles the HUD and overlay labels for the supplied game model.
     *
     * @param game active game model providing scoring, speed tier, inventory, and portal state
     */
    public GameHudManager(Game game) {
        this.game = game;
        gameOverLabel = createOverlayLabel("GAME OVER", "gameOverLabel");
        gameWonLabel = createOverlayLabel("YOU WON!", "gameWonLabel");
        pausedLabel = createOverlayLabel("PAUSED", "pausedLabel");
        scoreLabel = createOverlayLabel(formatScoreText(0), "scoreLabel");
        scoreLabel.setVisible(true);
        scoreLabel.setWrapText(true);
        scoreLabel.setAlignment(Pos.CENTER_LEFT);
        scoreLabel.setTextAlignment(TextAlignment.LEFT);
        applyStylesheet();
        updateFontSizes(1.0, game.getWidth() * game.getMultiplier());
    }

    private Label createOverlayLabel(String text, String id) {
        Label label = new Label(text);
        label.setId(id);
        label.setVisible(false);
        return label;
    }

    private void applyStylesheet() {
        URL cssLocation = GameHudManager.class.getResource(
                "/resources/views/styling.css"
        );
        if (cssLocation == null) {
            return;
        }

        String stylesheet = cssLocation.toExternalForm();
        gameOverLabel.getStylesheets().add(stylesheet);
        gameWonLabel.getStylesheets().add(stylesheet);
        pausedLabel.getStylesheets().add(stylesheet);
        scoreLabel.getStylesheets().add(stylesheet);
    }

    /**
     * Arranges the top HUD bar above the board canvas and centers all state overlays
     * over the playable board without occluding playable cells.
     *
     * @param gamePane root pane hosting the game scene
     * @param gridCanvas playable game board canvas
     */
    public void layoutGamePane(StackPane gamePane, Canvas gridCanvas) {
        StackPane boardContainer = new StackPane(
                gridCanvas,
                gameOverLabel,
                gameWonLabel,
                pausedLabel
        );
        StackPane.setAlignment(gridCanvas, Pos.CENTER);
        StackPane.setAlignment(gameOverLabel, Pos.CENTER);
        StackPane.setAlignment(gameWonLabel, Pos.CENTER);
        StackPane.setAlignment(pausedLabel, Pos.CENTER);

        layoutRoot = new VBox(HUD_SPACING, scoreLabel, boardContainer);
        layoutRoot.setAlignment(Pos.CENTER);
        layoutRoot.setFillWidth(false);

        gamePane.getChildren().setAll(layoutRoot);
        gamePane.setPadding(new Insets(PANE_PADDING));
        StackPane.setAlignment(layoutRoot, Pos.CENTER);
        updateFontSizes(1.0, gridCanvas.getWidth());
    }

    /**
     * Scales HUD and state-overlay font sizes and header dimensions to match the active canvas scale.
     *
     * @param scale active canvas scale factor ({@code 1.0} in windowed mode)
     * @param boardWidth rendered width of the board canvas in pixels
     */
    public void updateFontSizes(double scale, double boardWidth) {
        String overlayStyle = "-fx-font-size: " + 25 * scale + "px;";
        gameOverLabel.setStyle(overlayStyle);
        gameWonLabel.setStyle(overlayStyle);
        pausedLabel.setStyle(overlayStyle);
        scoreLabel.setStyle(
                "-fx-font-size: " + 12 * scale + "px;"
                        + " -fx-text-fill: #111111;"
                        + " -fx-padding: 2 6;"
        );
        scoreLabel.setPrefWidth(boardWidth);
        scoreLabel.setMaxWidth(boardWidth);
        scoreLabel.setMinHeight(HUD_LABEL_HEIGHT * scale);
        scoreLabel.setPrefHeight(HUD_LABEL_HEIGHT * scale);
        if (layoutRoot != null) {
            layoutRoot.setSpacing(HUD_SPACING * scale);
        }
    }

    /**
     * Synchronizes visibility of the game-over overlay with the snake's alive state.
     *
     * @param snake active snake
     */
    public void updateGameOver(Snake snake) {
        boolean dead = snake.getDead();
        if (dead && !gameOverVisible) {
            gameOverVisible = true;
            gameOverLabel.setVisible(true);
        } else if (!dead && gameOverVisible) {
            gameOverVisible = false;
            gameOverLabel.setVisible(false);
        }
    }

    /**
     * Synchronizes visibility of the victory overlay with the game's win state.
     */
    public void updateGameWon() {
        boolean won = game.gameWon();
        if (won && !gameWonVisible) {
            gameWonVisible = true;
            gameWonLabel.setVisible(true);
        } else if (!won && gameWonVisible) {
            gameWonVisible = false;
            gameWonLabel.setVisible(false);
        }
    }

    /**
     * Synchronizes visibility of the centered paused overlay with the session's pause state.
     *
     * @param paused {@code true} to display the paused overlay; {@code false} to hide it
     */
    public void updatePaused(boolean paused) {
        if (paused && !pausedVisible) {
            pausedVisible = true;
            pausedLabel.setVisible(true);
        } else if (!paused && pausedVisible) {
            pausedVisible = false;
            pausedLabel.setVisible(false);
        }
    }

    /**
     * Refreshes the two-line HUD text with the current score, speed tier, shop stock, and portal status.
     */
    public void updateScoreLabel() {
        scoreLabel.setText(formatScoreText(game.getScorePoints()));
    }

    // Formats the compact two-line HUD displaying score, shop state, and active powers.
    private String formatScoreText(int scorePoints) {
        PlayerInventory inventory = game.getInventory();
        String magnetStatus = inventory.isMagnetActive()
                ? String.format(
                        Locale.ROOT,
                        "%.1fs",
                        inventory.getMagnetTimeRemaining()
                )
                : PowerUpType.MAGNET.getCost() + "pt";

        String reviveOption = "";
        if (!inventory.hasUsedReviveStone()) {
            String reviveStatus = inventory.hasReviveStone()
                    ? "ON"
                    : PowerUpType.REVIVE_STONE.getCost() + "pt";
            reviveOption = "  [3]Rev: " + reviveStatus;
        }

        PortalPair portalPair = game.getPortalPair();
        String portalOption = "";
        if (portalPair.isActive()) {
            portalOption = portalPair.isTraversing()
                    ? "  [P:WARP]"
                    : "  [P:ON]";
        }

        return String.format(
                Locale.ROOT,
                "Score: %d | %s (%.1fx) | Pts: %d\n"
                        + "[1]Shd: %d/%d  "
                        + "[2]Mag: %s%s%s",
                scorePoints,
                game.getSpeedTier().getDisplayName(),
                game.getSpeedTier().getScoreMultiplier(),
                inventory.getShopPoints(),
                inventory.getShieldCharges(),
                PlayerInventory.MAX_SHIELD_PURCHASES,
                magnetStatus,
                reviveOption,
                portalOption
        );
    }

    /**
     * Returns the centered game-over overlay label.
     *
     * @return game-over {@link Label}
     */
    public Label getGameOverLabel() {
        return gameOverLabel;
    }

    /**
     * Returns the centered victory overlay label.
     *
     * @return victory {@link Label}
     */
    public Label getGameWonLabel() {
        return gameWonLabel;
    }

    /**
     * Returns the centered pause overlay label.
     *
     * @return pause {@link Label}
     */
    public Label getPausedLabel() {
        return pausedLabel;
    }

    /**
     * Returns the top HUD status label.
     *
     * @return HUD {@link Label}
     */
    public Label getScoreLabel() {
        return scoreLabel;
    }
}
