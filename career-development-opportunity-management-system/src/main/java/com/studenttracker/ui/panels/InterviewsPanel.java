package com.studenttracker.ui.panels;

import com.studenttracker.config.Services;
import com.studenttracker.model.Interview;
import com.studenttracker.service.ApplicationService;
import com.studenttracker.service.InterviewService;
import com.studenttracker.service.InterviewService.Scope;
import com.studenttracker.ui.dialogs.InterviewDialog;
import com.studenttracker.ui.support.DataTable;
import com.studenttracker.ui.support.ListTableModel.Column;
import com.studenttracker.ui.support.Notifier;
import com.studenttracker.ui.support.Theme;
import com.studenttracker.ui.support.Toolbar;
import java.awt.BorderLayout;
import java.awt.Component;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

public final class InterviewsPanel extends FeaturePanel {

    private static final long serialVersionUID = 1L;

    private final transient InterviewService service;
    private final transient ApplicationService applications;
    private final JComboBox<Scope> scope = new JComboBox<>(Scope.values());
    private final DataTable<Interview> table = new DataTable<>(List.of(
            new Column<Interview>("Company", String.class, Interview::getCompanyName, 150),
            new Column<Interview>("Role", String.class, Interview::getRoleTitle, 180),
            new Column<Interview>("Round", String.class, Interview::getRoundName, 150),
            new Column<Interview>("Date and time", LocalDateTime.class, Interview::getScheduledAt, 150),
            new Column<Interview>("Mode", String.class, interview -> interview.getMode().toString(), 70),
            new Column<Interview>("Result", String.class, interview -> interview.getResult().toString(), 80),
            new Column<Interview>("Feedback", String.class,
                    interview -> interview.getFeedback() == null ? "" : interview.getFeedback(), 280)),
            "");
    private final JButton edit = Theme.secondaryButton("Edit");
    private final JButton delete = Theme.secondaryButton("Delete");

    public InterviewsPanel(Services services) {
        super("Interviews", "Every interview round, with the outcome and what you learned from it.");
        this.service = services.interviews();
        this.applications = services.applications();

        scope.setSelectedItem(Scope.UPCOMING);
        scope.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                                   int index, boolean selected, boolean focused) {
                String label = value == Scope.UPCOMING ? "Upcoming" : value == Scope.PAST ? "Past and cancelled" : "All";
                return super.getListCellRendererComponent(list, label, index, selected, focused);
            }
        });
        scope.addActionListener(event -> onShow());

        JButton add = Theme.primaryButton("Add interview");
        add.addActionListener(event -> addInterview());
        edit.addActionListener(event -> editInterview());
        delete.addActionListener(event -> deleteInterview());
        table.onSelectionChanged(this::updateButtons);
        table.onActivate(this::editInterview);
        updateButtons();

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(new Toolbar().left(new JLabel("Show"), scope).right(delete, edit, add), BorderLayout.NORTH);
        body.add(table, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    @Override
    public void onShow() {
        Scope selected = (Scope) scope.getSelectedItem();
        load(() -> service.list(selected), interviews -> {
            table.setRows(interviews);
            table.setEmptyMessage(switch (selected) {
                case UPCOMING -> "No upcoming interviews. Add one from here or from the Applications screen.";
                case PAST -> "No past interviews yet.";
                case ALL -> "No interviews yet.";
            });
        });
    }

    private void updateButtons() {
        boolean selected = table.selected().isPresent();
        edit.setEnabled(selected);
        delete.setEnabled(selected);
    }

    private void addInterview() {
        load(applications::listActive, active -> {
            if (active.isEmpty()) {
                Notifier.info(this, "No applications in progress",
                        "Interviews belong to an application that is still in progress. "
                                + "Mark an opportunity as applied first.");
                return;
            }
            InterviewDialog.forNew(this, active, null).showDialog()
                    .ifPresent(interview -> perform(() -> service.add(interview), this::onShow));
        });
    }

    private void editInterview() {
        table.selected().ifPresent(selected ->
                InterviewDialog.forEdit(this, selected).showDialog()
                        .ifPresent(interview -> perform(() -> service.update(interview), this::onShow)));
    }

    private void deleteInterview() {
        table.selected().ifPresent(interview -> {
            boolean confirmed = Notifier.confirm(this, "Delete interview?",
                    "Delete the " + interview.getRoundName() + " round for " + interview.getRoleTitle()
                            + " at " + interview.getCompanyName() + "? This can't be undone. "
                            + "To keep the record, edit it and set the result to Cancelled instead.",
                    "Delete interview", "Keep interview");
            if (confirmed) {
                perform(() -> service.delete(interview), this::onShow);
            }
        });
    }
}
