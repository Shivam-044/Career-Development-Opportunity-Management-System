package com.studenttracker.ui.panels;

import com.studenttracker.config.Services;
import com.studenttracker.controller.ApplicationController;
import com.studenttracker.enums.ApplicationStatus;
import com.studenttracker.model.JobApplication;
import com.studenttracker.service.ApplicationService;
import com.studenttracker.service.InterviewService;
import com.studenttracker.ui.dialogs.InterviewDialog;
import com.studenttracker.ui.support.DataTable;
import com.studenttracker.ui.support.Fields;
import com.studenttracker.ui.support.ListTableModel.Column;
import com.studenttracker.ui.support.SearchField;
import com.studenttracker.ui.support.Theme;
import com.studenttracker.ui.support.Toolbar;
import java.awt.BorderLayout;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

public final class ApplicationsPanel extends FeaturePanel {

    private static final long serialVersionUID = 1L;
    private static final String EMPTY =
            "No applications yet. Mark an opportunity as applied and it will show up here.";

    private final transient ApplicationService service;
    private final transient InterviewService interviews;
    private final transient ApplicationController controller;

    private final SearchField search = new SearchField("Search company or role", this::onShow);
    private final JComboBox<ApplicationStatus> statusFilter =
            Fields.optionalCombo(ApplicationStatus.values(), "All statuses");
    private final DataTable<JobApplication> table = new DataTable<>(List.of(
            new Column<JobApplication>("Company", String.class, JobApplication::getCompanyName, 170),
            new Column<JobApplication>("Role", String.class, JobApplication::getRoleTitle, 210),
            new Column<JobApplication>("Applied on", LocalDate.class, JobApplication::getAppliedOn, 100),
            new Column<JobApplication>("Status", String.class, application -> application.getStatus().toString(), 100),
            new Column<JobApplication>("Notes", String.class,
                    application -> application.getNotes() == null ? "" : application.getNotes(), 340)),
            EMPTY);

    private final JButton updateStatus = Theme.primaryButton("Update status");
    private final JButton correctStatus = Theme.secondaryButton("Correct status");
    private final JButton notes = Theme.secondaryButton("Notes");
    private final JButton addInterview = Theme.secondaryButton("Add interview");

    public ApplicationsPanel(Services services) {
        super("Applications", "Follow each application from applied to offer.");
        this.service = services.applications();
        this.interviews = services.interviews();
        this.controller = new ApplicationController(service, this);

        statusFilter.addActionListener(event -> onShow());
        updateStatus.addActionListener(event -> table.selected()
                .ifPresent(application -> controller.changeStatus(application, this::onShow)));
        correctStatus.addActionListener(event -> table.selected()
                .ifPresent(application -> controller.correctStatus(application, this::onShow)));
        notes.addActionListener(event -> table.selected()
                .ifPresent(application -> controller.editNotes(application, this::onShow)));
        addInterview.addActionListener(event -> table.selected().ifPresent(this::scheduleInterview));
        table.onSelectionChanged(this::updateButtons);
        updateButtons();

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(new Toolbar().left(search, statusFilter)
                .right(notes, addInterview, correctStatus, updateStatus), BorderLayout.NORTH);
        body.add(table, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    @Override
    public void onShow() {
        String keyword = search.query();
        ApplicationStatus status = (ApplicationStatus) statusFilter.getSelectedItem();
        load(() -> service.search(keyword, status), applications -> {
            table.setRows(applications);
            table.setEmptyMessage(keyword.isEmpty() && status == null ? EMPTY : "No applications match.");
        });
    }

    private void updateButtons() {
        boolean selected = table.selected().isPresent();
        updateStatus.setEnabled(selected);
        correctStatus.setEnabled(selected);
        notes.setEnabled(selected);
        addInterview.setEnabled(selected);
    }

    private void scheduleInterview(JobApplication application) {
        InterviewDialog.forNew(this, List.of(application), application).showDialog()
                .ifPresent(interview -> perform(() -> interviews.add(interview), this::onShow));
    }
}
