package com.studenttracker.ui.panels;

import com.studenttracker.config.Services;
import com.studenttracker.enums.ApplicationStatus;
import com.studenttracker.model.DashboardSummary;
import com.studenttracker.service.DashboardService;
import com.studenttracker.service.ExportService;
import com.studenttracker.ui.support.DataTable;
import com.studenttracker.ui.support.ListTableModel.Column;
import com.studenttracker.ui.support.Notifier;
import com.studenttracker.ui.support.Theme;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class ReportsPanel extends FeaturePanel {

    private static final long serialVersionUID = 1L;

    private interface Export {
        int run(Path destination) throws IOException;
    }

    private final transient DashboardService dashboard;
    private final transient ExportService exports;
    private final transient Map<ApplicationStatus, Integer> counts = new EnumMap<>(ApplicationStatus.class);
    private final JLabel total = new JLabel(" ");
    private final DataTable<ApplicationStatus> statusTable = new DataTable<>(List.of(
            new Column<ApplicationStatus>("Status", String.class, ApplicationStatus::toString, 160),
            new Column<ApplicationStatus>("Applications", Integer.class,
                    status -> counts.getOrDefault(status, 0), 100)),
            "");

    public ReportsPanel(Services services) {
        super("Reports", "Application totals, and CSV exports you can open in Excel or any spreadsheet.");
        this.dashboard = services.dashboard();
        this.exports = services.exports();

        JButton exportApplications = Theme.primaryButton("Export applications to CSV");
        exportApplications.addActionListener(event ->
                export("applications.csv", "Export applications", exports::exportApplications));
        JButton exportInterviews = Theme.secondaryButton("Export interviews to CSV");
        exportInterviews.addActionListener(event ->
                export("interviews.csv", "Export interviews", exports::exportInterviews));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(exportApplications);
        buttons.add(exportInterviews);

        total.setForeground(Theme.MUTED);
        JPanel summary = new JPanel(new BorderLayout(0, 8));
        summary.setOpaque(false);
        summary.add(Theme.sectionHeading("Applications by status"), BorderLayout.NORTH);
        summary.add(statusTable, BorderLayout.CENTER);
        summary.add(total, BorderLayout.SOUTH);
        summary.setPreferredSize(new Dimension(320, 260));

        JPanel summaryHolder = new JPanel(new BorderLayout());
        summaryHolder.setOpaque(false);
        summaryHolder.add(summary, BorderLayout.NORTH);

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(summaryHolder, BorderLayout.WEST);

        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setOpaque(false);
        body.add(buttons, BorderLayout.NORTH);
        body.add(left, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        statusTable.setRows(Arrays.asList(ApplicationStatus.values()));
    }

    @Override
    public void onShow() {
        load(dashboard::summary, this::showSummary);
    }

    private void showSummary(DashboardSummary summary) {
        counts.clear();
        counts.putAll(summary.applicationsByStatus());
        statusTable.setRows(Arrays.asList(ApplicationStatus.values()));
        total.setText(summary.totalApplications() + " applications in total");
    }

    private void export(String suggestedName, String dialogTitle, Export action) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(dialogTitle);
        chooser.setSelectedFile(new File(suggestedName));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File chosen = chooser.getSelectedFile();
        if (!chosen.getName().toLowerCase().endsWith(".csv")) {
            chosen = new File(chosen.getParentFile(), chosen.getName() + ".csv");
        }
        if (chosen.exists() && !Notifier.confirm(this, "Replace existing file?",
                chosen.getName() + " already exists in that folder. Replace it with the new export?",
                "Replace file", "Cancel")) {
            return;
        }

        Path destination = chosen.toPath();
        load(() -> {
            try {
                return action.run(destination);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }, rows -> Notifier.info(this, "Export finished",
                rows + (rows == 1 ? " row was" : " rows were") + " saved to " + destination + "."));
    }
}
