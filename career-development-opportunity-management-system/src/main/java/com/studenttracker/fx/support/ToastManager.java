package com.studenttracker.fx.support;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Short-lived confirmations in the corner of the window. The main shell hands over the container to fill. */
public final class ToastManager {

    private enum Kind {
        SUCCESS("check-circle", "success", 3.6),
        INFO("info", "info", 4.0),
        WARNING("warning", "warning", 5.0),
        ERROR("error", "danger", 6.5);

        private final String icon;
        private final String variant;
        private final double seconds;

        Kind(String icon, String variant, double seconds) {
            this.icon = icon;
            this.variant = variant;
            this.seconds = seconds;
        }
    }

    private static final int MAX_VISIBLE = 4;

    private VBox container;

    public void attach(VBox toastContainer) {
        this.container = toastContainer;
    }

    public void success(String message) {
        show(Kind.SUCCESS, message);
    }

    public void info(String message) {
        show(Kind.INFO, message);
    }

    public void warning(String message) {
        show(Kind.WARNING, message);
    }

    public void error(String message) {
        show(Kind.ERROR, message);
    }

    private void show(Kind kind, String message) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> show(kind, message));
            return;
        }
        if (container == null) {
            return;
        }

        StackPane iconBubble = new StackPane(Icons.of(kind.icon));
        iconBubble.getStyleClass().addAll("toast-icon", "toast-" + kind.variant);

        Label text = new Label(message);
        text.setWrapText(true);
        text.setMaxWidth(300);
        text.getStyleClass().add("toast-text");

        Button close = new Button();
        close.setGraphic(Icons.of("close"));
        close.getStyleClass().addAll("btn-icon", "btn-icon-small");
        close.setFocusTraversable(false);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox toast = new HBox(12, iconBubble, text, spacer, close);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.getStyleClass().addAll("toast", "toast-" + kind.variant);
        toast.setMaxWidth(400);

        while (container.getChildren().size() >= MAX_VISIBLE) {
            container.getChildren().remove(0);
        }
        container.getChildren().add(toast);
        FxAnimations.slideFadeIn(toast, 14, Duration.ZERO);

        PauseTransition wait = new PauseTransition(Duration.seconds(kind.seconds));
        wait.setOnFinished(event -> dismiss(toast));
        wait.play();
        close.setOnAction(event -> {
            wait.stop();
            dismiss(toast);
        });
    }

    private void dismiss(Node toast) {
        if (!container.getChildren().contains(toast)) {
            return;
        }
        if (FxAnimations.isReducedMotion()) {
            container.getChildren().remove(toast);
            return;
        }
        FadeTransition fade = new FadeTransition(Duration.millis(180), toast);
        fade.setToValue(0);
        fade.setOnFinished(event -> container.getChildren().remove(toast));
        fade.play();
    }
}
