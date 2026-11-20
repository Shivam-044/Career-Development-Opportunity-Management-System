package com.studenttracker.fx.controller;

import com.studenttracker.fx.navigation.NavigationManager;
import com.studenttracker.fx.navigation.Route;
import com.studenttracker.fx.navigation.Searchable;
import com.studenttracker.fx.support.AppInfo;
import com.studenttracker.fx.support.FxAnimations;
import com.studenttracker.fx.support.FxContext;
import com.studenttracker.fx.support.FxFormats;
import com.studenttracker.fx.support.Icons;
import com.studenttracker.fx.support.NotificationBuilder.Item;
import com.studenttracker.fx.support.NotificationBuilder.Kind;
import com.studenttracker.fx.support.ThemeManager;
import com.studenttracker.model.Student;
import java.util.Optional;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Controller of the application frame: sidebar, header, reminders, toasts and the content area. */
public final class MainShellController {

    private static final Duration REMINDER_REFRESH = Duration.minutes(5);

    private final FxContext ctx;

    @FXML
    private ToggleGroup navGroup;
    @FXML
    private StackPane contentHost;
    @FXML
    private VBox toastContainer;
    @FXML
    private Label pageTitle;
    @FXML
    private Label pageSubtitle;
    @FXML
    private HBox searchBox;
    @FXML
    private TextField searchField;
    @FXML
    private Region themeIcon;
    @FXML
    private Button bellButton;
    @FXML
    private Label notificationBadge;
    @FXML
    private VBox notificationPanel;
    @FXML
    private VBox notificationList;
    @FXML
    private Label notificationEmpty;
    @FXML
    private Button clearAllButton;
    @FXML
    private ProgressBar busyBar;
    @FXML
    private Label avatarInitials;
    @FXML
    private Label avatarName;
    @FXML
    private Label versionLabel;

    private final PauseTransition searchDebounce = new PauseTransition(Duration.millis(250));
    private final PauseTransition busyDelay = new PauseTransition(Duration.millis(150));

    public MainShellController(FxContext ctx) {
        this.ctx = ctx;
    }

    @FXML
    private void initialize() {
        versionLabel.setText("Version " + AppInfo.VERSION);
        ctx.toasts().attach(toastContainer);

        NavigationManager navigation = ctx.navigation();
        navigation.attach(contentHost);
        navigation.currentRouteProperty().addListener((observable, previous, route) -> routeChanged(route));

        navGroup.selectedToggleProperty().addListener((observable, previous, selected) -> {
            if (selected == null) {
                if (previous != null) {
                    previous.setSelected(true);
                }
                return;
            }
            navigation.navigate(Route.valueOf(String.valueOf(selected.getUserData())));
        });

        wireSearch();
        wireTheme();
        wireBusyIndicator();
        wireNotifications();
        wireAvatar();

        contentHost.sceneProperty().addListener((observable, previous, scene) -> {
            if (scene != null) {
                scene.addEventFilter(MouseEvent.MOUSE_PRESSED, this::closeReminderPanelOnOutsideClick);
                scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.ESCAPE && notificationPanel.isVisible()) {
                        hideNotifications();
                    }
                });
            }
        });

        navigation.navigate(Route.DASHBOARD);
        ctx.notifications().refresh();
        Timeline periodic = new Timeline(new KeyFrame(REMINDER_REFRESH, event -> ctx.notifications().refresh()));
        periodic.setCycleCount(Animation.INDEFINITE);
        periodic.play();
    }

    // ------------------------------------------------------------ navigation

    private void routeChanged(Route route) {
        pageTitle.setText(route.title());
        pageSubtitle.setText(route.subtitle());
        selectNavItem(route);
        hideNotifications();

        Optional<Object> controller = ctx.navigation().currentController();
        boolean searchable = controller.isPresent() && controller.get() instanceof Searchable;
        searchBox.setVisible(searchable);
        searchBox.setManaged(searchable);
        searchField.clear();
        if (searchable) {
            searchField.setPromptText(((Searchable) controller.get()).searchPrompt());
        }
    }

    private void selectNavItem(Route route) {
        for (Toggle toggle : navGroup.getToggles()) {
            if (route.name().equals(toggle.getUserData())) {
                navGroup.selectToggle(toggle);
                return;
            }
        }
    }

    // ---------------------------------------------------------------- search

    private void wireSearch() {
        searchDebounce.setOnFinished(event -> ctx.navigation().currentController()
                .filter(Searchable.class::isInstance)
                .map(Searchable.class::cast)
                .ifPresent(page -> page.applySearch(searchField.getText().trim())));
        searchField.textProperty().addListener((observable, previous, text) -> searchDebounce.playFromStart());
    }

    // ----------------------------------------------------------------- theme

    private void wireTheme() {
        updateThemeIcon(ctx.themes().current());
        ctx.themes().themeProperty().addListener((observable, previous, theme) -> updateThemeIcon(theme));
    }

    private void updateThemeIcon(ThemeManager.Theme theme) {
        // Show the icon of the theme you would switch to.
        themeIcon.getStyleClass().removeAll("icon-sun", "icon-moon");
        themeIcon.getStyleClass().add(theme == ThemeManager.Theme.DARK ? "icon-sun" : "icon-moon");
    }

    @FXML
    private void toggleTheme() {
        ctx.themes().toggle();
    }

    // ------------------------------------------------------- busy indicator

    private void wireBusyIndicator() {
        busyDelay.setOnFinished(event -> {
            if (ctx.tasks().busyProperty().get()) {
                fadeBusyBar(1);
            }
        });
        ctx.tasks().busyProperty().addListener((observable, wasBusy, busy) -> {
            if (busy) {
                busyDelay.playFromStart();
            } else {
                busyDelay.stop();
                fadeBusyBar(0);
            }
        });
    }

    private void fadeBusyBar(double target) {
        FadeTransition fade = new FadeTransition(Duration.millis(160), busyBar);
        fade.setToValue(target);
        fade.play();
    }

    // -------------------------------------------------------------- reminders

    private void wireNotifications() {
        ctx.notifications().items().addListener((ListChangeListener<Item>) change -> renderNotifications());
        renderNotifications();
    }

    private void renderNotifications() {
        notificationList.getChildren().clear();
        for (Item item : ctx.notifications().items()) {
            notificationList.getChildren().add(notificationRow(item));
        }
        int count = ctx.notifications().items().size();
        notificationBadge.setText(count > 9 ? "9+" : Integer.toString(count));
        notificationBadge.setVisible(count > 0);
        notificationBadge.setManaged(count > 0);
        notificationEmpty.setVisible(count == 0);
        notificationEmpty.setManaged(count == 0);
        clearAllButton.setDisable(count == 0);
    }

    private Node notificationRow(Item item) {
        boolean interview = item.kind() == Kind.INTERVIEW;
        StackPane bubble = new StackPane(Icons.of(interview ? "event" : "work"));
        bubble.getStyleClass().addAll("stat-icon-bubble", interview ? "stat-accent" : "stat-warning");

        Label title = new Label(item.title());
        title.getStyleClass().add("notification-title");
        title.setWrapText(true);
        Label detail = new Label(item.detail());
        detail.getStyleClass().add("muted");
        VBox text = new VBox(2, title, detail);
        HBox.setHgrow(text, Priority.ALWAYS);

        Button dismiss = new Button();
        dismiss.setGraphic(Icons.of("close"));
        dismiss.getStyleClass().addAll("btn-icon", "btn-icon-small");
        dismiss.setOnAction(event -> ctx.notifications().dismiss(item));

        HBox row = new HBox(12, bubble, text, dismiss);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("notification-row");
        row.setOnMouseClicked(event -> {
            if (!isInside(event.getTarget(), dismiss)) {
                hideNotifications();
                ctx.navigation().navigate(interview ? Route.INTERVIEWS : Route.OPPORTUNITIES);
            }
        });
        return row;
    }

    @FXML
    private void toggleNotifications() {
        if (notificationPanel.isVisible()) {
            hideNotifications();
            return;
        }
        notificationPanel.setManaged(true);
        notificationPanel.setVisible(true);
        FxAnimations.slideFadeIn(notificationPanel, -10, Duration.ZERO);
    }

    private void hideNotifications() {
        notificationPanel.setVisible(false);
        notificationPanel.setManaged(false);
    }

    @FXML
    private void dismissAll() {
        ctx.notifications().dismissAll();
    }

    private void closeReminderPanelOnOutsideClick(MouseEvent event) {
        if (notificationPanel.isVisible()
                && !isInside(event.getTarget(), notificationPanel)
                && !isInside(event.getTarget(), bellButton)) {
            hideNotifications();
        }
    }

    private static boolean isInside(Object target, Node container) {
        Node node = target instanceof Node found ? found : null;
        while (node != null) {
            if (node == container) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }

    // ----------------------------------------------------------------- avatar

    private void wireAvatar() {
        showAvatar(null);
        ctx.onProfileChanged(this::loadAvatar);
        loadAvatar();
    }

    private void loadAvatar() {
        ctx.tasks().runHandling(
                () -> ctx.services().students().findProfile().map(Student::getFullName).orElse(null),
                this::showAvatar,
                error -> { });
    }

    private void showAvatar(String name) {
        boolean known = name != null && !name.isBlank();
        avatarName.setText(known ? FxFormats.firstName(name) : "Set up profile");
        avatarInitials.setText(known ? FxFormats.initials(name) : "?");
    }
}
