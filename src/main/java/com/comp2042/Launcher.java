package com.comp2042;

/**
 * Provides a separate packaged application entry point.
 * Fat JARs can fail when the main class extends Application.
 * The runtime then reports missing JavaFX components.
 * This plain class delegates startup to Main.
 */
public final class Launcher {

    private Launcher() {
    }

    /**
     * Launches the application by forwarding arguments to {@link Main#main(String[])}.
     *
     * @param args command-line arguments passed to the JavaFX runtime
     */
    public static void main(String[] args) {
        Main.main(args);
    }
}
