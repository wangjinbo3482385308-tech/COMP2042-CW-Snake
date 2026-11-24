package com.comp2042.model;

/**
 * Represents the lifecycle states of a single game session.
 */
public enum GameState {
    /** Initial state before the player starts moving. */
    READY,
    /** Active gameplay loop is running. */
    RUNNING,
    /** Gameplay loop is temporarily halted by the player. */
    PAUSED,
    /** Session ended because the snake died without a remaining revive. */
    GAME_OVER,
    /** Session ended because the player reached the win score target. */
    WON
}
