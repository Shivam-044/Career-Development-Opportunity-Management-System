package com.studenttracker.ui.panels;

import com.studenttracker.ui.support.BackgroundTask;
import com.studenttracker.ui.support.Notifier;
import com.studenttracker.ui.support.Theme;
import java.awt.BorderLayout;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/** Common frame for every screen: a titled page whose data is reloaded each time it is shown. */
public abstract class FeaturePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    protected FeaturePanel(String title, String subtitle) {
        super(new BorderLayout(0, 16));
        setBackground(Theme.PAGE);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(Theme.title(title));
        header.add(Box.createVerticalStrut(4));
        header.add(Theme.subtitle(subtitle));
        add(header, BorderLayout.NORTH);
    }

    /** Called whenever the user navigates to this screen. */
    public abstract void onShow();

    protected final <T> void load(Supplier<T> work, Consumer<T> onSuccess) {
        BackgroundTask.load(work, onSuccess, error -> Notifier.error(this, error));
    }

    protected final void perform(Runnable work, Runnable onSuccess) {
        BackgroundTask.perform(work, onSuccess, error -> Notifier.error(this, error));
    }
}
