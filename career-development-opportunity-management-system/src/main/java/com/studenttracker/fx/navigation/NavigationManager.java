package com.studenttracker.fx.navigation;

import com.studenttracker.fx.support.AppInfo;
import com.studenttracker.fx.support.FxAnimations;
import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javafx.animation.Animation;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

/** Loads each page once, keeps it, and swaps pages inside one content area with a short cross-fade. */
public final class NavigationManager {

    private record LoadedPage(Parent view, Object controller) {
    }

    private static final String VIEW_ROOT = AppInfo.RESOURCE_ROOT + "view/";

    private final Callback<Class<?>, Object> controllerFactory;
    private final Map<Route, LoadedPage> pages = new EnumMap<>(Route.class);
    private final ObjectProperty<Route> currentRoute = new SimpleObjectProperty<>();
    private Pane host;
    private Animation running;

    public NavigationManager(Callback<Class<?>, Object> controllerFactory) {
        this.controllerFactory = controllerFactory;
    }

    /** Sets the container the pages are shown in. */
    public void attach(Pane contentHost) {
        this.host = contentHost;
    }

    public ObjectProperty<Route> currentRouteProperty() {
        return currentRoute;
    }

    public Route current() {
        return currentRoute.get();
    }

    public Optional<Object> currentController() {
        Route route = currentRoute.get();
        return route == null ? Optional.empty() : Optional.ofNullable(pages.get(route)).map(LoadedPage::controller);
    }

    public void navigate(Route route) {
        Objects.requireNonNull(host, "Navigation has no content area yet");
        if (route == currentRoute.get() && !host.getChildren().isEmpty()) {
            return;
        }
        LoadedPage page = load(route);
        currentRoute.set(route);

        if (page.controller() instanceof PageController pageController) {
            pageController.onShow();
        }

        Node incoming = page.view();
        Node outgoing = host.getChildren().isEmpty() ? null : host.getChildren().get(0);
        if (running != null) {
            running.stop();
        }
        if (outgoing != null) {
            outgoing.setOpacity(1);
            outgoing.setTranslateY(0);
        }
        if (incoming == outgoing) {
            return;
        }
        running = FxAnimations.pageTransition(outgoing, incoming, () -> host.getChildren().setAll(incoming));
    }

    private LoadedPage load(Route route) {
        LoadedPage cached = pages.get(route);
        if (cached != null) {
            return cached;
        }
        URL location = Objects.requireNonNull(NavigationManager.class.getResource(VIEW_ROOT + route.fxml()),
                "Missing view " + route.fxml());
        FXMLLoader loader = new FXMLLoader(location);
        loader.setControllerFactory(controllerFactory);
        try {
            Parent view = loader.load();
            LoadedPage page = new LoadedPage(view, loader.getController());
            pages.put(route, page);
            return page;
        } catch (IOException ex) {
            throw new IllegalStateException("Couldn't load the " + route.title() + " page", ex);
        }
    }
}
