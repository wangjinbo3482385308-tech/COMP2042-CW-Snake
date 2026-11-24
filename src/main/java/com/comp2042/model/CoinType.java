package com.comp2042.model;

/**
 * Identifies logical coin variants without exposing image resources to the model layer.
 */
public enum CoinType {
    /** Standard red coin awarding base points. */
    RED,
    /** High-value gold coin awarding bonus points. */
    GOLD,
    /** Silver coin that spawns additional board obstacles. */
    SILVER,
    /** Blue coin granting temporary border wrapping. */
    BLUE,
    /** Rainbow coin granting a temporary movement speed boost. */
    RAINBOW
}
