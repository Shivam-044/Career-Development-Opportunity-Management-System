package com.studenttracker.fx.support;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.prefs.Preferences;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;

/**
 * Owns the light/dark theme. Every scene (main window and dialogs) is registered here, so switching
 * the theme restyles everything immediately without a restart. The choice is stored in Java Preferences.
 */
public final class ThemeManager {

    public enum Theme {
        LIGHT, DARK;

        String stylesheet() {
            return this == DARK ? "theme-dark.css" : "theme-light.css";
        }

        String rootClass() {
            return this == DARK ? "theme-dark" : "theme-light";
        }
    }

    private static final String KEY = "theme";
    private static final String CSS_ROOT = AppInfo.RESOURCE_ROOT + "css/";

    private final Preferences preferences = Preferences.userNodeForPackage(ThemeManager.class);
    private final ObjectProperty<Theme> theme = new SimpleObjectProperty<>();
    private final List<WeakReference<Scene>> scenes = new ArrayList<>();

    public ThemeManager() {
        theme.set(readStored());
    }

    public ObjectProperty<Theme> themeProperty() {
        return theme;
    }

    public Theme current() {
        return theme.get();
    }

    public void set(Theme next) {
        if (next == theme.get()) {
            return;
        }
        theme.set(next);
        preferences.put(KEY, next.name());
        scenes.removeIf(reference -> reference.get() == null);
        for (WeakReference<Scene> reference : scenes) {
            Scene scene = reference.get();
            if (scene != null) {
                apply(scene);
            }
        }
    }

    public void toggle() {
        set(current() == Theme.DARK ? Theme.LIGHT : Theme.DARK);
    }

    /** Applies the current theme to the scene and keeps it in sync from now on. */
    public void register(Scene scene) {
        apply(scene);
        scenes.add(new WeakReference<>(scene));
        FxAnimations.installHoverLift(scene);
    }

    private void apply(Scene scene) {
        Theme active = theme.get();
        scene.getStylesheets().setAll(
                resource(active.stylesheet()),
                resource("components.css"),
                resource("animations.css"));
        scene.getRoot().getStyleClass().removeAll(Theme.LIGHT.rootClass(), Theme.DARK.rootClass());
        scene.getRoot().getStyleClass().add(active.rootClass());
    }

    private Theme readStored() {
        try {
            return Theme.valueOf(preferences.get(KEY, Theme.DARK.name()));
        } catch (IllegalArgumentException ex) {
            return Theme.DARK;
        }
    }

    private static String resource(String name) {
        URL url = ThemeManager.class.getResource(CSS_ROOT + name);
        return Objects.requireNonNull(url, "Missing stylesheet " + name).toExternalForm();
    }
}
