package com.studenttracker.fx.support;

import com.studenttracker.config.Services;
import com.studenttracker.fx.navigation.NavigationManager;
import java.util.ArrayList;
import java.util.List;
import javafx.application.HostServices;

/**
 * The shared objects every page needs: the one service graph, the background runner, dialogs, toasts and the
 * theme. Controllers receive it through their constructor instead of reaching for static state.
 */
public final class FxContext {

    private final Services services;
    private final FxTaskRunner tasks;
    private final FxDialogService dialogs;
    private final ToastManager toasts;
    private final ThemeManager themes;
    private final NotificationCenter notifications;
    private final HostServices hostServices;
    private final List<Runnable> profileListeners = new ArrayList<>();
    private NavigationManager navigation;

    public FxContext(Services services, FxTaskRunner tasks, FxDialogService dialogs, ToastManager toasts,
                     ThemeManager themes, NotificationCenter notifications, HostServices hostServices) {
        this.services = services;
        this.tasks = tasks;
        this.dialogs = dialogs;
        this.toasts = toasts;
        this.themes = themes;
        this.notifications = notifications;
        this.hostServices = hostServices;
    }

    public Services services() {
        return services;
    }

    public FxTaskRunner tasks() {
        return tasks;
    }

    public FxDialogService dialogs() {
        return dialogs;
    }

    public ToastManager toasts() {
        return toasts;
    }

    public ThemeManager themes() {
        return themes;
    }

    public NotificationCenter notifications() {
        return notifications;
    }

    public NavigationManager navigation() {
        return navigation;
    }

    public void setNavigation(NavigationManager navigation) {
        this.navigation = navigation;
    }

    public void openUrl(String url) {
        if (url != null && !url.isBlank()) {
            hostServices.showDocument(url);
        }
    }

    /** Lets the header refresh the avatar when the profile is saved on the Profile page. */
    public void onProfileChanged(Runnable listener) {
        profileListeners.add(listener);
    }

    public void fireProfileChanged() {
        profileListeners.forEach(Runnable::run);
    }
}
