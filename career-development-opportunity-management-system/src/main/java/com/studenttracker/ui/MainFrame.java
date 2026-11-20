package com.studenttracker.ui;

import com.studenttracker.config.Services;
import com.studenttracker.ui.panels.ApplicationsPanel;
import com.studenttracker.ui.panels.CompaniesPanel;
import com.studenttracker.ui.panels.DashboardPanel;
import com.studenttracker.ui.panels.FeaturePanel;
import com.studenttracker.ui.panels.InterviewsPanel;
import com.studenttracker.ui.panels.OpportunitiesPanel;
import com.studenttracker.ui.panels.ProfilePanel;
import com.studenttracker.ui.panels.ReportsPanel;
import com.studenttracker.ui.panels.SkillsPanel;
import com.studenttracker.ui.support.Theme;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** The application window: a navigation sidebar and a card area holding one panel per feature. */
public final class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final int SIDEBAR_WIDTH = 220;

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final transient Map<String, FeaturePanel> screens = new LinkedHashMap<>();

    public MainFrame(Services services) {
        super("CareerForge");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        screens.put("Dashboard", new DashboardPanel(services));
        screens.put("Profile", new ProfilePanel(services));
        screens.put("Skills", new SkillsPanel(services));
        screens.put("Companies", new CompaniesPanel(services));
        screens.put("Opportunities", new OpportunitiesPanel(services));
        screens.put("Applications", new ApplicationsPanel(services));
        screens.put("Interviews", new InterviewsPanel(services));
        screens.put("Reports", new ReportsPanel(services));
        screens.forEach((name, panel) -> content.add(panel, name));

        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        setSize(1240, 780);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);
        navigateTo("Dashboard");
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 14, 16, 14));
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));

        JLabel brand = new JLabel("CareerForge");
        brand.setFont(Theme.font(Font.BOLD, 18f));
        brand.setForeground(Color.WHITE);
        brand.setBorder(BorderFactory.createEmptyBorder(0, 8, 2, 0));
        brand.setAlignmentX(LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("Preparation and applications");
        tagline.setFont(Theme.font(Font.PLAIN, 12f));
        tagline.setForeground(Theme.SIDEBAR_MUTED);
        tagline.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        tagline.setAlignmentX(LEFT_ALIGNMENT);

        sidebar.add(brand);
        sidebar.add(tagline);
        sidebar.add(Box.createVerticalStrut(24));

        ButtonGroup group = new ButtonGroup();
        for (String name : screens.keySet()) {
            NavButton button = new NavButton(name);
            button.addActionListener(event -> navigateTo(name));
            group.add(button);
            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(4));
            if (name.equals("Dashboard")) {
                button.setSelected(true);
            }
        }
        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private void navigateTo(String name) {
        cards.show(content, name);
        screens.get(name).onShow();
    }
}
