package com.comp2042;

import com.comp2042.view.ViewManager;
import javafx.application.Application;
import javafx.stage.Stage;

// Starts the JavaFX application.
// Delegates window setup to the view layer.
public class Main extends Application {

    private static ViewManager viewManager;

    // Stores the shared view manager.
    // Opens the main menu after setup.
    @Override
    public void start(Stage primaryStage) {
        viewManager = new ViewManager(primaryStage);
        viewManager.initializeMainMenu();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static ViewManager getViewManager() {
        return viewManager;
    }
}
