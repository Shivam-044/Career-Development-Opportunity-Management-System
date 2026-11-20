package com.studenttracker.fx.support;

import java.util.function.Consumer;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

public final class Toggles {

    private Toggles() {
    }

    /**
     * Makes a toggle group behave like a segmented control: one segment is always selected, and the
     * callback receives the selected toggle's user data whenever the choice changes.
     */
    public static void segmented(ToggleGroup group, Consumer<String> onChange) {
        group.selectedToggleProperty().addListener((observable, previous, current) -> {
            if (current == null) {
                if (previous != null) {
                    previous.setSelected(true);
                }
                return;
            }
            onChange.accept(String.valueOf(current.getUserData()));
        });
    }

    public static String selectedKey(ToggleGroup group) {
        Toggle selected = group.getSelectedToggle();
        return selected == null ? "" : String.valueOf(selected.getUserData());
    }
}
