package com.studenttracker.fx.controller;

import com.studenttracker.config.AppConfig;
import com.studenttracker.exception.DataAccessException;
import com.studenttracker.fx.support.AppInfo;
import com.studenttracker.fx.support.FxAnimations;
import com.studenttracker.fx.support.FxContext;
import com.studenttracker.fx.support.ThemeManager;
import com.studenttracker.fx.support.Toggles;
import com.studenttracker.util.ErrorMessages;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

public final class SettingsFxController extends BaseFxController {

    @FXML
    private ToggleGroup themeGroup;
    @FXML
    private ToggleButton lightToggle;
    @FXML
    private ToggleButton darkToggle;
    @FXML
    private CheckBox reduceMotion;
    @FXML
    private Label serverLabel;
    @FXML
    private Label accountLabel;
    @FXML
    private Button testButton;
    @FXML
    private Label testResult;
    @FXML
    private Label aboutLabel;

    public SettingsFxController(FxContext ctx) {
        super(ctx);
    }

    @FXML
    private void initialize() {
        selectTheme(ctx.themes().current());
        Toggles.segmented(themeGroup, key -> ctx.themes().set(ThemeManager.Theme.valueOf(key)));
        ctx.themes().themeProperty().addListener((observable, previous, theme) -> selectTheme(theme));

        reduceMotion.setSelected(FxAnimations.isReducedMotion());
        aboutLabel.setText(AppInfo.NAME + " " + AppInfo.VERSION + "\nJava " + System.getProperty("java.version")
                + "  |  JavaFX " + AppInfo.javafxVersion()
                + "\nA local desktop tracker for skills, opportunities, applications and interviews.");
        showConnectionDetails();
    }

    @Override
    public void onShow() {
        showConnectionDetails();
    }

    private void selectTheme(ThemeManager.Theme theme) {
        (theme == ThemeManager.Theme.DARK ? darkToggle : lightToggle).setSelected(true);
    }

    @FXML
    private void reduceMotionChanged() {
        FxAnimations.setReducedMotion(reduceMotion.isSelected());
    }

    private void showConnectionDetails() {
        try {
            AppConfig config = AppConfig.current();
            String url = config.dbUrl();
            int query = url.indexOf('?');
            serverLabel.setText(query >= 0 ? url.substring(0, query) : url);
            accountLabel.setText(config.dbUser() + " (password hidden)");
        } catch (DataAccessException ex) {
            serverLabel.setText("Not configured");
            accountLabel.setText(ex.getMessage());
        }
    }

    @FXML
    private void testConnection() {
        testResult.getStyleClass().removeAll("form-error", "form-hint");
        testResult.setText("Testing...");
        ctx.tasks().runHandling(services.dashboard()::summary,
                summary -> {
                    testResult.getStyleClass().add("form-hint");
                    testResult.setText("Connected. The database answered normally.");
                },
                error -> {
                    testResult.getStyleClass().add("form-error");
                    testResult.setText(ErrorMessages.describe(error).text());
                }, testButton);
    }
}
