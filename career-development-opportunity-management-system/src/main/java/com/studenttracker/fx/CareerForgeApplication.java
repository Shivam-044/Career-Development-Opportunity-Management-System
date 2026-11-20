package com.studenttracker.fx;

import com.studenttracker.config.Services;
import com.studenttracker.fx.navigation.NavigationManager;
import com.studenttracker.fx.support.AppInfo;
import com.studenttracker.fx.support.FxContext;
import com.studenttracker.fx.support.FxControllerFactory;
import com.studenttracker.fx.support.FxDialogService;
import com.studenttracker.fx.support.FxTaskRunner;
import com.studenttracker.fx.support.NotificationCenter;
import com.studenttracker.fx.support.ThemeManager;
import com.studenttracker.fx.support.ToastManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/** JavaFX entry point: builds the shared context, loads the main shell and shows the window. */
public class CareerForgeApplication extends Application {

    private FxTaskRunner tasks;

    @Override
    public void start(Stage stage) {
        try {
            Services services = Services.createDefault();
            ThemeManager themes = new ThemeManager();
            FxDialogService dialogs = new FxDialogService(themes);
            ToastManager toasts = new ToastManager();
            tasks = new FxTaskRunner(dialogs::error);
            NotificationCenter notifications = new NotificationCenter(services.dashboard(), tasks, toasts);

            FxContext context = new FxContext(
                    services, tasks, dialogs, toasts, themes, notifications, getHostServices());
            FxControllerFactory factory = new FxControllerFactory(context);
            context.setNavigation(new NavigationManager(factory));

            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource(AppInfo.RESOURCE_ROOT + "view/main-shell.fxml"), "Missing main-shell.fxml"));
            loader.setControllerFactory(factory);
            Parent root = loader.load();

            Scene scene = new Scene(root, 1320, 840);
            themes.register(scene);

            stage.setTitle("CareerForge");
            stage.setMinWidth(1120);
            stage.setMinHeight(700);
            stage.setScene(scene);
            try (InputStream logo = getClass().getResourceAsStream(AppInfo.RESOURCE_ROOT + "assets/logo.png")) {
                if (logo != null) {
                    stage.getIcons().add(new Image(logo));
                }
            }
            dialogs.setOwner(stage);
            stage.show();
        } catch (IOException | RuntimeException ex) {
            showStartupFailure(ex);
        }
    }

    @Override
    public void stop() {
        if (tasks != null) {
            tasks.shutdown();
        }
    }

    private void showStartupFailure(Exception failure) {
        failure.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("CareerForge");
        alert.setHeaderText("The application couldn't start");
        alert.setContentText(String.valueOf(failure.getMessage()));
        alert.showAndWait();
        Platform.exit();
    }
}
