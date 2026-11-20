package com.studenttracker.fx.navigation;

/** Implemented by page controllers so the shell can tell them when they become visible. */
public interface PageController {

    /** Called every time the page is shown; pages reload their data here. */
    default void onShow() {
    }
}
