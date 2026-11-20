package com.studenttracker.fx;

import javafx.application.Application;

/**
 * Entry point for packaged builds. A JavaFX application class cannot be the main class of a plain
 * classpath jar, so this thin launcher starts it instead.
 */
public final class FxLauncher {

    private FxLauncher() {
    }

    public static void main(String[] args) {
        Application.launch(CareerForgeApplication.class, args);
    }
}
