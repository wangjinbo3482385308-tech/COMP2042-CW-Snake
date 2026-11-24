package com.comp2042;

import com.comp2042.view.ViewManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Starts the JavaFX application and delegates window setup to the view layer.
 */
public class Main extends Application {

    private static ViewManager viewManager;

    /**
     * Creates the JavaFX application instance.
     */
    public Main() {
    }

    /**
     * Initializes the shared view manager and displays the main menu.
     *
     * @param primaryStage primary application window supplied by JavaFX
     */
    @Override
    public void start(Stage primaryStage) {
        viewManager = new ViewManager(primaryStage);
        viewManager.initializeMainMenu();
    }

    /**
     * Launches the JavaFX runtime.
     *
     * @param args command-line arguments passed to {@link Application#launch(String...)}
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Returns the shared view manager created during startup.
     *
     * @return active {@link ViewManager} instance, or {@code null} before startup
     */
    public static ViewManager getViewManager() {
        return viewManager;
    }
}
