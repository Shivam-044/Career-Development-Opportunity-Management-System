package com.studenttracker.fx.support;

import com.studenttracker.fx.support.NotificationBuilder.Item;
import com.studenttracker.model.DashboardSummary;
import com.studenttracker.service.DashboardService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Keeps the list of reminders shown behind the bell icon. Reminders come from real interview and deadline
 * data, and dismissals are remembered between launches. They only appear while the application is running.
 */
public final class NotificationCenter {

    private static final String DISMISSED_KEY = "dismissedReminders";
    private static final String SEPARATOR = "\n";

    private final DashboardService dashboard;
    private final FxTaskRunner tasks;
    private final ToastManager toasts;
    private final Preferences preferences = Preferences.userNodeForPackage(NotificationCenter.class);
    private final ObservableList<Item> items = FXCollections.observableArrayList();
    private boolean announced;

    public NotificationCenter(DashboardService dashboard, FxTaskRunner tasks, ToastManager toasts) {
        this.dashboard = dashboard;
        this.tasks = tasks;
        this.toasts = toasts;
    }

    public ObservableList<Item> items() {
        return FXCollections.unmodifiableObservableList(items);
    }

    /** Reloads reminders in the background. Failures are ignored here; the pages report database problems. */
    public void refresh() {
        tasks.runHandling(dashboard::summary, this::apply, error -> { });
    }

    public void dismiss(Item item) {
        Set<String> dismissed = readDismissed();
        dismissed.add(item.key());
        writeDismissed(dismissed);
        items.remove(item);
    }

    public void dismissAll() {
        Set<String> dismissed = readDismissed();
        items.forEach(item -> dismissed.add(item.key()));
        writeDismissed(dismissed);
        items.clear();
    }

    private void apply(DashboardSummary summary) {
        List<Item> all = NotificationBuilder.build(summary, LocalDateTime.now());
        Set<String> dismissed = readDismissed();
        dismissed.retainAll(all.stream().map(Item::key).collect(Collectors.toSet()));
        writeDismissed(dismissed);

        List<Item> visible = all.stream().filter(item -> !dismissed.contains(item.key())).toList();
        items.setAll(visible);

        if (!announced) {
            announced = true;
            if (!visible.isEmpty()) {
                toasts.info(visible.size() == 1
                        ? "You have 1 reminder: " + visible.get(0).title() + "."
                        : "You have " + visible.size() + " reminders. Open the bell to see them.");
            }
        }
    }

    private Set<String> readDismissed() {
        String stored = preferences.get(DISMISSED_KEY, "");
        if (stored.isBlank()) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(stored.split(SEPARATOR)));
    }

    private void writeDismissed(Set<String> dismissed) {
        String joined = String.join(SEPARATOR, dismissed);
        preferences.put(DISMISSED_KEY, joined.length() > Preferences.MAX_VALUE_LENGTH ? "" : joined);
    }
}
