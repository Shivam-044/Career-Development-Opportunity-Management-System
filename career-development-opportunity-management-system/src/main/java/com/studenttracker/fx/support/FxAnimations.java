package com.studenttracker.fx.support;

import java.util.List;
import java.util.prefs.Preferences;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

/**
 * Small, consistent motion helpers. Everything respects the "reduce motion" setting: when it is on,
 * values jump straight to their final state instead of animating.
 */
public final class FxAnimations {

    private static final Duration FAST = Duration.millis(140);
    private static final Duration NORMAL = Duration.millis(240);
    private static final String REDUCE_MOTION_KEY = "reduceMotion";
    private static final String HOVER_KEY = "hoverTransition";
    private static final Preferences PREFS = Preferences.userNodeForPackage(FxAnimations.class);

    private static volatile boolean reducedMotion = PREFS.getBoolean(REDUCE_MOTION_KEY, false);

    private FxAnimations() {
    }

    public static boolean isReducedMotion() {
        return reducedMotion;
    }

    public static void setReducedMotion(boolean reduced) {
        reducedMotion = reduced;
        PREFS.putBoolean(REDUCE_MOTION_KEY, reduced);
    }

    public static void fadeIn(Node node) {
        if (reducedMotion) {
            node.setOpacity(1);
            return;
        }
        node.setOpacity(0);
        FadeTransition fade = new FadeTransition(NORMAL, node);
        fade.setToValue(1);
        fade.play();
    }

    public static void slideFadeIn(Node node, double fromY, Duration delay) {
        if (reducedMotion) {
            node.setOpacity(1);
            node.setTranslateY(0);
            return;
        }
        node.setOpacity(0);
        node.setTranslateY(fromY);
        FadeTransition fade = new FadeTransition(NORMAL, node);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(NORMAL, node);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        ParallelTransition both = new ParallelTransition(fade, slide);
        both.setDelay(delay);
        both.play();
    }

    /** Fades a group of nodes in one after another, a few tens of milliseconds apart. */
    public static void staggerIn(List<? extends Node> nodes) {
        int index = 0;
        for (Node node : nodes) {
            slideFadeIn(node, 14, Duration.millis(index * 45L));
            index++;
        }
    }

    public static void animateCount(Label label, int from, int to) {
        if (reducedMotion || from == to) {
            label.setText(Integer.toString(to));
            return;
        }
        DoubleProperty value = new SimpleDoubleProperty(from);
        value.addListener((observable, before, now) ->
                label.setText(Integer.toString((int) Math.round(now.doubleValue()))));
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(value, from)),
                new KeyFrame(Duration.millis(650), new KeyValue(value, to, Interpolator.EASE_OUT)));
        timeline.setOnFinished(event -> label.setText(Integer.toString(to)));
        timeline.play();
    }

    public static void animateProgress(ProgressBar bar, double target) {
        if (reducedMotion) {
            bar.setProgress(target);
            return;
        }
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(600), new KeyValue(bar.progressProperty(), target, Interpolator.EASE_OUT)));
        timeline.play();
    }

    /**
     * Cross-fades between two pages. {@code swap} runs once the outgoing page has faded out and must
     * put the incoming page on screen. Returns the running animation so a caller can stop it.
     */
    public static Animation pageTransition(Node outgoing, Node incoming, Runnable swap) {
        if (reducedMotion || outgoing == null) {
            swap.run();
            incoming.setOpacity(1);
            incoming.setTranslateY(0);
            return null;
        }
        FadeTransition out = new FadeTransition(Duration.millis(90), outgoing);
        out.setToValue(0);
        out.setOnFinished(event -> {
            swap.run();
            outgoing.setOpacity(1);
            incoming.setOpacity(0);
            incoming.setTranslateY(10);
            FadeTransition in = new FadeTransition(Duration.millis(200), incoming);
            in.setToValue(1);
            TranslateTransition rise = new TranslateTransition(Duration.millis(200), incoming);
            rise.setToY(0);
            rise.setInterpolator(Interpolator.EASE_OUT);
            new ParallelTransition(in, rise).play();
        });
        out.play();
        return out;
    }

    public static void popIn(Node node) {
        if (reducedMotion) {
            return;
        }
        node.setOpacity(0);
        node.setScaleX(0.96);
        node.setScaleY(0.96);
        FadeTransition fade = new FadeTransition(Duration.millis(180), node);
        fade.setToValue(1);
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), node);
        scale.setToX(1);
        scale.setToY(1);
        scale.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(fade, scale).play();
    }

    /** A brief horizontal nudge to flag a rejected form. */
    public static void shake(Node node) {
        if (reducedMotion) {
            return;
        }
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(node.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(60), new KeyValue(node.translateXProperty(), -7)),
                new KeyFrame(Duration.millis(120), new KeyValue(node.translateXProperty(), 7)),
                new KeyFrame(Duration.millis(180), new KeyValue(node.translateXProperty(), -4)),
                new KeyFrame(Duration.millis(240), new KeyValue(node.translateXProperty(), 0)));
        timeline.play();
    }

    /** Gives every node with the style class {@code hover-lift} a gentle scale on hover. */
    public static void installHoverLift(Scene scene) {
        scene.addEventFilter(MouseEvent.MOUSE_ENTERED_TARGET, event -> {
            if (event.getTarget() instanceof Node node && node.getStyleClass().contains("hover-lift")) {
                scaleTo(node, 1.02);
            }
        });
        scene.addEventFilter(MouseEvent.MOUSE_EXITED_TARGET, event -> {
            if (event.getTarget() instanceof Node node && node.getStyleClass().contains("hover-lift")) {
                scaleTo(node, 1.0);
            }
        });
    }

    private static void scaleTo(Node node, double scale) {
        if (reducedMotion) {
            return;
        }
        Object previous = node.getProperties().get(HOVER_KEY);
        if (previous instanceof Animation running) {
            running.stop();
        }
        ScaleTransition transition = new ScaleTransition(FAST, node);
        transition.setToX(scale);
        transition.setToY(scale);
        node.getProperties().put(HOVER_KEY, transition);
        transition.play();
    }
}
