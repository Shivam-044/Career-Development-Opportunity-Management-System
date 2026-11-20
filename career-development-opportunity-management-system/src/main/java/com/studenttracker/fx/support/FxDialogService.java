package com.studenttracker.fx.support;

import com.studenttracker.exception.ValidationException;
import com.studenttracker.util.ErrorMessages;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Modal dialogs in the app's glass style. Form dialogs load their fields from an FXML file and get a title bar,
 * inline validation message and buttons from this class; message dialogs cover info, confirm and error cases.
 */
public final class FxDialogService {

    private enum Kind {
        INFO("info", "info"),
        WARNING("warning", "warning"),
        ERROR("error", "danger"),
        QUESTION("help", "accent"),
        DANGER("warning", "danger");

        private final String icon;
        private final String variant;

        Kind(String icon, String variant) {
            this.icon = icon;
            this.variant = variant;
        }
    }

    private static final String FORM_ROOT = AppInfo.RESOURCE_ROOT + "view/dialogs/";

    private final ThemeManager themes;
    private Window owner;

    public FxDialogService(ThemeManager themes) {
        this.themes = themes;
    }

    public void setOwner(Window owner) {
        this.owner = owner;
    }

    // ---------------------------------------------------------------- forms

    /**
     * Shows a form dialog and waits for it. Returns the collected value, or empty if the user cancelled.
     *
     * @param prepare called with the dialog's controller before it is shown, to fill in existing values
     */
    public <T, C extends DialogForm<T>> Optional<T> form(String title, String submitLabel, String fxmlName,
                                                         Consumer<C> prepare) {
        URL location = Objects.requireNonNull(FxDialogService.class.getResource(FORM_ROOT + fxmlName),
                "Missing dialog " + fxmlName);
        FXMLLoader loader = new FXMLLoader(location);
        Parent content;
        try {
            content = loader.load();
        } catch (IOException ex) {
            throw new IllegalStateException("Couldn't load the dialog " + fxmlName, ex);
        }
        C controller = loader.getController();
        prepare.accept(controller);

        Label error = new Label();
        error.setWrapText(true);
        error.setMaxWidth(440);
        error.getStyleClass().add("form-error");
        error.setVisible(false);
        error.setManaged(false);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFocusTraversable(false);
        scroll.getStyleClass().add("dialog-scroll");
        scroll.setMaxHeight(maxContentHeight());

        Button cancel = button("Cancel", "btn-secondary");
        cancel.setCancelButton(true);
        Button submit = button(submitLabel, "btn-primary");
        submit.setDefaultButton(true);
        HBox footer = new HBox(10, grow(), cancel, submit);
        footer.setAlignment(Pos.CENTER_RIGHT);

        AtomicReference<T> result = new AtomicReference<>();
        VBox card = card(title, new VBox(14, scroll, error, footer));
        Stage stage = stage(card);

        submit.setOnAction(event -> {
            try {
                result.set(controller.collect());
                stage.close();
            } catch (ValidationException ex) {
                error.setText(ex.getMessage());
                error.setVisible(true);
                error.setManaged(true);
                FxAnimations.shake(card);
            }
        });
        cancel.setOnAction(event -> stage.close());
        closeButtonOf(card).setOnAction(event -> stage.close());
        stage.setOnShown(event -> controller.focusFirstField());

        show(stage, card);
        return Optional.ofNullable(result.get());
    }

    // ------------------------------------------------------------- messages

    public void info(String title, String message) {
        message(Kind.INFO, title, message, new String[] {"OK"});
    }

    public void warning(String title, String message) {
        message(Kind.WARNING, title, message, new String[] {"OK"});
    }

    /** Shows the friendly version of an exception. Safe to call from any thread. */
    public void error(Throwable failure) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> error(failure));
            return;
        }
        ErrorMessages.Message text = ErrorMessages.describe(failure);
        Kind kind = text.severity() == ErrorMessages.Severity.WARNING ? Kind.WARNING : Kind.ERROR;
        message(kind, text.title(), text.text(), new String[] {"OK"});
    }

    public boolean confirm(String title, String message, String confirmLabel, String cancelLabel) {
        return message(Kind.QUESTION, title, message, new String[] {confirmLabel, cancelLabel}) == 0;
    }

    /** Like {@link #confirm} but styled as a destructive action (remove, delete, archive). */
    public boolean confirmDestructive(String title, String message, String confirmLabel, String cancelLabel) {
        return message(Kind.DANGER, title, message, new String[] {confirmLabel, cancelLabel}) == 0;
    }

    /** Shows one button per label; returns the index of the one pressed, or -1 if the dialog was dismissed. */
    public int choose(String title, String message, String... labels) {
        return message(Kind.QUESTION, title, message, labels);
    }

    private int message(Kind kind, String title, String text, String[] labels) {
        StackPane iconBubble = new StackPane(Icons.of(kind.icon));
        iconBubble.getStyleClass().addAll("message-icon", "toast-" + kind.variant);
        Label body = new Label(text);
        body.setWrapText(true);
        body.setMaxWidth(400);
        body.getStyleClass().add("dialog-message");
        HBox row = new HBox(16, iconBubble, body);
        row.setAlignment(Pos.TOP_LEFT);

        AtomicInteger chosen = new AtomicInteger(-1);
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.getChildren().add(grow());
        VBox card = card(title, new VBox(22, row, footer));
        Stage stage = stage(card);

        for (int i = 0; i < labels.length; i++) {
            int index = i;
            String style = i == 0 ? (kind == Kind.DANGER ? "btn-danger-solid" : "btn-primary") : "btn-secondary";
            Button button = button(labels[i], style);
            button.setOnAction(event -> {
                chosen.set(index);
                stage.close();
            });
            if (i == 0) {
                button.setDefaultButton(true);
            }
            if (i == labels.length - 1 && labels.length > 1) {
                button.setCancelButton(true);
            }
            footer.getChildren().add(button);
        }
        closeButtonOf(card).setOnAction(event -> stage.close());
        if (labels.length == 1) {
            ((Button) footer.getChildren().get(1)).setCancelButton(true);
        }

        show(stage, card);
        return chosen.get();
    }

    // -------------------------------------------------------------- helpers

    private VBox card(String title, Node body) {
        Label heading = new Label(title);
        heading.getStyleClass().add("dialog-title");
        Button close = new Button();
        close.setGraphic(Icons.of("close"));
        close.getStyleClass().addAll("btn-icon", "btn-icon-small");
        close.setFocusTraversable(false);
        close.setId("dialog-close");
        HBox header = new HBox(heading, grow(), close);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("dialog-header");

        VBox card = new VBox(18, header, body);
        card.getStyleClass().add("dialog-card");
        card.setMinWidth(440);
        card.setMaxWidth(560);
        return card;
    }

    private Button closeButtonOf(VBox card) {
        HBox header = (HBox) card.getChildren().get(0);
        return (Button) header.getChildren().get(2);
    }

    private Stage stage(VBox card) {
        Stage stage = new Stage(StageStyle.TRANSPARENT);
        if (owner != null) {
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
        } else {
            stage.initModality(Modality.APPLICATION_MODAL);
        }
        StackPane root = new StackPane(card);
        root.getStyleClass().add("dialog-root");
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        themes.register(scene);
        stage.setScene(scene);

        // Let the card be dragged by its header, since the window itself has no title bar.
        double[] grab = new double[2];
        Node header = card.getChildren().get(0);
        header.setOnMousePressed(event -> {
            grab[0] = event.getScreenX() - stage.getX();
            grab[1] = event.getScreenY() - stage.getY();
        });
        header.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - grab[0]);
            stage.setY(event.getScreenY() - grab[1]);
        });
        return stage;
    }

    private void show(Stage stage, VBox card) {
        Node ownerRoot = owner == null || owner.getScene() == null ? null : owner.getScene().getRoot();
        Consumer<javafx.stage.WindowEvent> centre = event -> {
            if (owner != null) {
                stage.setX(owner.getX() + (owner.getWidth() - stage.getWidth()) / 2);
                stage.setY(owner.getY() + (owner.getHeight() - stage.getHeight()) / 2);
            }
            FxAnimations.popIn(card);
        };
        var previousOnShown = stage.getOnShown();
        stage.setOnShown(event -> {
            centre.accept(event);
            if (previousOnShown != null) {
                previousOnShown.handle(event);
            }
        });
        if (ownerRoot != null) {
            ownerRoot.setEffect(new GaussianBlur(5));
        }
        try {
            stage.showAndWait();
        } finally {
            if (ownerRoot != null) {
                ownerRoot.setEffect(null);
            }
        }
    }

    private static Button button(String text, String variant) {
        Button button = new Button(text);
        button.getStyleClass().addAll("btn", variant, "hover-lift");
        return button;
    }

    private static Region grow() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private static double maxContentHeight() {
        double screen = Screen.getPrimary().getVisualBounds().getHeight();
        return Math.max(280, Math.min(640, screen - 280));
    }
}
