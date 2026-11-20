package com.studenttracker.ui.support;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.SwingWorker;

/**
 * Runs slow work (database calls) off the Event Dispatch Thread and hands the outcome
 * back on it, so the window stays responsive.
 */
public final class BackgroundTask {

    private BackgroundTask() {
    }

    public static <T> void load(Supplier<T> work, Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() {
                return work.get();
            }

            @Override
            protected void done() {
                try {
                    onSuccess.accept(get());
                } catch (ExecutionException ex) {
                    onFailure.accept(ex.getCause());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    public static void perform(Runnable work, Runnable onSuccess, Consumer<Throwable> onFailure) {
        load(() -> {
            work.run();
            return null;
        }, ignored -> onSuccess.run(), onFailure);
    }
}
