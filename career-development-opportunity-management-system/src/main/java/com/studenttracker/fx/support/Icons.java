package com.studenttracker.fx.support;

import javafx.scene.layout.Region;

/** Vector icons are drawn from CSS shapes (see components.css), so they follow the theme colours. */
public final class Icons {

    private Icons() {
    }

    public static Region of(String name) {
        Region icon = new Region();
        icon.getStyleClass().addAll("icon", "icon-" + name);
        icon.setMouseTransparent(true);
        return icon;
    }
}
