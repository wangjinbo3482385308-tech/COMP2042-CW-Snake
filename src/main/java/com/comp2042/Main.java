package com.comp2042;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    private static ViewManager viewManager;

    @Override
    public void start(Stage primaryStage) {
        // Pass the application stage into the view manager.
        // Reuse the managed window throughout the application.
        viewManager = new ViewManager(primaryStage);
        viewManager.initalizeMainMenu();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static ViewManager getViewManager() {
        return viewManager;
    }
}
