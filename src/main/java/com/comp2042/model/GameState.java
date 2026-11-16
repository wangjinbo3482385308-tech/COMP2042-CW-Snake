package com.comp2042.model;

// Represents the lifecycle of one game session.
// Controllers decide when each state is entered.
public enum GameState {
    READY,
    RUNNING,
    PAUSED,
    GAME_OVER,
    WON
}
