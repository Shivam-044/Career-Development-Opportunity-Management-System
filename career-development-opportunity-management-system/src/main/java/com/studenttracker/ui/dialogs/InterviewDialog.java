package com.studenttracker.ui.dialogs;

import com.studenttracker.enums.InterviewMode;
import com.studenttracker.enums.InterviewResult;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.model.Interview;
import com.studenttracker.model.JobApplication;
import com.studenttracker.ui.support.Fields;
import com.studenttracker.ui.support.TextAreaField;
import com.studenttracker.util.Dates;
import java.awt.Component;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.JTextField;

public final class InterviewDialog extends FormDialog<Interview> {

    private static final long serialVersionUID = 1L;

    private final JComboBox<JobApplication> application;
    private final JTextField roundName = Fields.text(24);
    private final JSpinner scheduledAt = Fields.dateTimeSpinner();
    private final JComboBox<InterviewMode> mode = new JComboBox<>(InterviewMode.values());
    private final JComboBox<InterviewResult> result = new JComboBox<>(InterviewResult.values());
    private final TextAreaField feedback = new TextAreaField(4, 24);
    private final transient Interview editing;

    private InterviewDialog(Component owner, List<JobApplication> applications, Interview editing,
                            JobApplication preselected) {
        super(owner, editing == null ? "Add interview" : "Edit interview",
                editing == null ? "Add interview" : "Save changes");
        this.editing = editing;

        application = new JComboBox<>(applications.toArray(new JobApplication[0]));
        application.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                                   boolean selected, boolean focused) {
                String text = value instanceof JobApplication app ? app.describe() + "  (" + app.getStatus() + ")" : "";
                return super.getListCellRendererComponent(list, text, index, selected, focused);
            }
        });

        if (editing != null) {
            roundName.setText(editing.getRoundName());
            scheduledAt.setValue(Dates.toDate(editing.getScheduledAt()));
            mode.setSelectedItem(editing.getMode());
            result.setSelectedItem(editing.getResult());
            feedback.setText(editing.getFeedback());
            form().addRow("Application", new JLabel(editing.getRoleTitle() + " at " + editing.getCompanyName()));
        } else {
            application.setSelectedItem(preselected);
            if (preselected != null) {
                application.setEnabled(false);
            }
            form().addRow("Application", application, true);
        }
        form().addRow("Round", roundName, true);
        form().addRow("Date and time", scheduledAt, true);
        form().addRow("Mode", mode, true);
        form().addRow("Result", result, true);
        form().addRow("Feedback", feedback);
        finishLayout(owner);
    }

    /** Dialog for a new interview; pass {@code preselected} to fix the application, or null to let the user pick. */
    public static InterviewDialog forNew(Component owner, List<JobApplication> applications,
                                         JobApplication preselected) {
        return new InterviewDialog(owner, applications, null, preselected);
    }

    public static InterviewDialog forEdit(Component owner, Interview interview) {
        return new InterviewDialog(owner, List.of(), interview, null);
    }

    @Override
    protected Interview collect() {
        long applicationId;
        if (editing != null) {
            applicationId = editing.getApplicationId();
        } else {
            JobApplication selected = (JobApplication) application.getSelectedItem();
            if (selected == null) {
                throw new ValidationException("Application is required.");
            }
            applicationId = selected.getApplicationId();
        }
        Interview interview = new Interview(
                applicationId,
                roundName.getText(),
                Dates.toLocalDateTime((Date) scheduledAt.getValue()),
                (InterviewMode) mode.getSelectedItem());
        interview.setResult((InterviewResult) result.getSelectedItem());
        interview.setFeedback(feedback.getText());
        if (editing != null) {
            interview.setInterviewId(editing.getInterviewId());
            interview.setDisplayNames(editing.getCompanyName(), editing.getRoleTitle());
        }
        return interview;
    }
}
