package com.studenttracker.fx.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.concurrent.Task;
import javafx.scene.Node;

/**
 * Runs service calls (and therefore JDBC) on a small pool of daemon threads and delivers the result on
 * the JavaFX Application Thread. Must be called from the JavaFX thread.
 */
public final class FxTaskRunner {

    private static final int POOL_SIZE = 3;

    private final ExecutorService executor = Executors.newFixedThreadPool(POOL_SIZE, runnable -> {
        Thread thread = new Thread(runnable, "tracker-background");
        thread.setDaemon(true);
        return thread;
    });
    private final ReadOnlyBooleanWrapper busy = new ReadOnlyBooleanWrapper(false);
    private final Consumer<Throwable> defaultErrorHandler;
    private int running;

    public FxTaskRunner(Consumer<Throwable> defaultErrorHandler) {
        this.defaultErrorHandler = defaultErrorHandler;
    }

    /** True while at least one task is in flight. */
    public ReadOnlyBooleanProperty busyProperty() {
        return busy.getReadOnlyProperty();
    }

    /** Runs the work in the background. Errors go to the default handler; the guards are disabled meanwhile. */
    public <T> void run(Supplier<T> work, Consumer<T> onSuccess, Node... guards) {
        runHandling(work, onSuccess, defaultErrorHandler, guards);
    }

    public <T> void runHandling(Supplier<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError, Node... guards) {
        List<Node> disabledByUs = disable(guards);
        Task<T> task = new Task<>() {
            @Override
            protected T call() {
                return work.get();
            }
        };
        task.setOnSucceeded(event -> {
            release(disabledByUs);
            onSuccess.accept(task.getValue());
        });
        task.setOnFailed(event -> {
            release(disabledByUs);
            onError.accept(task.getException());
        });
        task.setOnCancelled(event -> release(disabledByUs));

        running++;
        busy.set(true);
        try {
            executor.execute(task);
        } catch (RejectedExecutionException ex) {
            release(disabledByUs);
        }
    }

    public void runVoid(Runnable work, Runnable onSuccess, Node... guards) {
        run(() -> {
            work.run();
            return Boolean.TRUE;
        }, done -> onSuccess.run(), guards);
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    private List<Node> disable(Node... guards) {
        List<Node> changed = new ArrayList<>();
        for (Node guard : guards) {
            if (guard != null && !guard.isDisable()) {
                guard.setDisable(true);
                changed.add(guard);
            }
        }
        return changed;
    }

    private void release(List<Node> disabledByUs) {
        for (Node node : disabledByUs) {
            node.setDisable(false);
        }
        running = Math.max(0, running - 1);
        busy.set(running > 0);
    }
}
