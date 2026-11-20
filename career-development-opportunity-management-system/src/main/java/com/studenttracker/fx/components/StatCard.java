package com.studenttracker.fx.components;

import com.studenttracker.fx.support.FxAnimations;
import com.studenttracker.fx.support.Icons;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * A dashboard tile: an icon, a large number and a caption. Declared in FXML with {@code title}, {@code hint},
 * {@code icon} (the icon name) and {@code variant} (accent, info, success or warning).
 */
public class StatCard extends VBox {

    private final Label titleLabel = new Label();
    private final Label valueLabel = new Label("0");
    private final Label hintLabel = new Label();
    private final StackPane iconBubble = new StackPane();
    private String icon = "";
    private String variant = "accent";
    private int shown;

    public StatCard() {
        getStyleClass().addAll("glass-card", "stat-card", "hover-lift");
        setSpacing(10);

        iconBubble.getStyleClass().addAll("stat-icon-bubble", "stat-accent");
        titleLabel.getStyleClass().add("stat-title");
        valueLabel.getStyleClass().add("stat-value");
        hintLabel.getStyleClass().add("stat-hint");
        hintLabel.setWrapText(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox top = new HBox(titleLabel, spacer, iconBubble);
        top.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(top, valueLabel, hintLabel);
    }

    public final void setTitle(String title) {
        titleLabel.setText(title);
    }

    public final String getTitle() {
        return titleLabel.getText();
    }

    public final void setHint(String hint) {
        hintLabel.setText(hint);
        hintLabel.setVisible(hint != null && !hint.isBlank());
        hintLabel.setManaged(hintLabel.isVisible());
    }

    public final String getHint() {
        return hintLabel.getText();
    }

    public final void setIcon(String iconName) {
        this.icon = iconName;
        iconBubble.getChildren().setAll(Icons.of(iconName));
    }

    public final String getIcon() {
        return icon;
    }

    public final String getVariant() {
        return variant;
    }

    public final void setVariant(String variant) {
        this.variant = variant;
        iconBubble.getStyleClass().removeIf(name -> name.startsWith("stat-") && !name.equals("stat-icon-bubble"));
        iconBubble.getStyleClass().add("stat-" + variant);
    }

    /** Counts up from the previously shown number to the new one. */
    public void setValue(int value) {
        FxAnimations.animateCount(valueLabel, shown, value);
        shown = value;
    }
}
