package com.studenttracker;

import com.formdev.flatlaf.FlatLightLaf;
import com.studenttracker.config.Services;
import com.studenttracker.ui.MainFrame;
import com.studenttracker.ui.support.Notifier;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::start);
    }

    private static void start() {
        installLookAndFeel();
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> Notifier.error(null, error));
        new MainFrame(Services.createDefault()).setVisible(true);
    }

    private static void installLookAndFeel() {
        try {
            if (FlatLightLaf.setup()) {
                UIManager.put("Component.arc", 8);
                UIManager.put("Button.arc", 8);
                UIManager.put("TextComponent.arc", 6);
                return;
            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            // The default Swing look and feel is an acceptable fallback.
        }
    }
}
