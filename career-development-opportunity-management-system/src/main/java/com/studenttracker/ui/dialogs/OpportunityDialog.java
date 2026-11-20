package com.studenttracker.ui.dialogs;

import com.studenttracker.enums.OpportunityStatus;
import com.studenttracker.enums.OpportunityType;
import com.studenttracker.enums.WorkMode;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.model.Company;
import com.studenttracker.model.Opportunity;
import com.studenttracker.ui.support.Fields;
import com.studenttracker.ui.support.OptionalDateField;
import com.studenttracker.ui.support.TextAreaField;
import java.awt.Component;
import java.util.Arrays;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public final class OpportunityDialog extends FormDialog<Opportunity> {

    private static final long serialVersionUID = 1L;

    private final JComboBox<Company> company;
    private final JTextField roleTitle = Fields.text(26);
    private final JComboBox<OpportunityType> type = new JComboBox<>(OpportunityType.values());
    private final JTextField location = Fields.text(26);
    private final JComboBox<WorkMode> workMode = Fields.optionalCombo(WorkMode.values(), "Not specified");
    private final OptionalDateField deadline = new OptionalDateField();
    private final JTextField minimumCgpa = Fields.text(8);
    private final JTextField sourceUrl = Fields.text(26);
    private final JTextField requiredSkills = Fields.text(26);
    private final JComboBox<OpportunityStatus> status = new JComboBox<>(OpportunityStatus.values());
    private final TextAreaField description = new TextAreaField(4, 26);
    private final transient Opportunity editing;

    /**
     * @param companies companies the opportunity can belong to
     * @param editing   the opportunity being edited, or {@code null} to add a new one
     */
    public OpportunityDialog(Component owner, List<Company> companies, Opportunity editing) {
        super(owner, editing == null ? "Add opportunity" : "Edit opportunity",
                editing == null ? "Add opportunity" : "Save changes");
        this.editing = editing;
        company = new JComboBox<>(companies.toArray(new Company[0]));

        sourceUrl.putClientProperty("JTextField.placeholderText", "https://");
        requiredSkills.putClientProperty("JTextField.placeholderText", "Java, SQL, Git");
        requiredSkills.setToolTipText("Separate skills with commas");
        minimumCgpa.setToolTipText("Optional, from 0.00 to 10.00");

        if (editing != null) {
            selectCompany(editing.getCompanyId());
            roleTitle.setText(editing.getRoleTitle());
            type.setSelectedItem(editing.getType());
            location.setText(editing.getLocation());
            workMode.setSelectedItem(editing.getWorkMode());
            deadline.setDate(editing.getDeadline());
            minimumCgpa.setText(editing.getMinimumCgpa() == null ? "" : editing.getMinimumCgpa().toPlainString());
            sourceUrl.setText(editing.getSourceUrl());
            requiredSkills.setText(String.join(", ", editing.getRequiredSkills()));
            status.setSelectedItem(editing.getStatus());
            description.setText(editing.getDescription());
        } else {
            company.setSelectedIndex(-1);
        }

        form().addRow("Company", company, true);
        form().addRow("Role title", roleTitle, true);
        form().addRow("Type", type, true);
        form().addRow("Location", location);
        form().addRow("Work mode", workMode);
        form().addRow("Deadline", deadline);
        form().addRow("Minimum CGPA", minimumCgpa);
        form().addRow("Source URL", sourceUrl);
        form().addRow("Required skills", requiredSkills);
        form().addRow("Listing status", status, true);
        form().addRow("Description", description);
        finishLayout(owner);
    }

    private void selectCompany(long companyId) {
        for (int i = 0; i < company.getItemCount(); i++) {
            if (company.getItemAt(i).getCompanyId() == companyId) {
                company.setSelectedIndex(i);
                return;
            }
        }
    }

    @Override
    protected Opportunity collect() {
        Company selected = (Company) company.getSelectedItem();
        if (selected == null) {
            throw new ValidationException("Company is required.");
        }
        Opportunity opportunity = new Opportunity(
                selected.getCompanyId(), roleTitle.getText(), (OpportunityType) type.getSelectedItem());
        opportunity.setCompanyName(selected.getCompanyName());
        opportunity.setLocation(location.getText());
        opportunity.setWorkMode((WorkMode) workMode.getSelectedItem());
        opportunity.setDeadline(deadline.getDate());
        opportunity.setMinimumCgpa(Fields.parseScore(minimumCgpa.getText(), "Minimum CGPA"));
        opportunity.setSourceUrl(sourceUrl.getText());
        opportunity.setRequiredSkills(Arrays.asList(requiredSkills.getText().split(",")));
        opportunity.setStatus((OpportunityStatus) status.getSelectedItem());
        opportunity.setDescription(description.getText());
        if (editing != null) {
            opportunity.setOpportunityId(editing.getOpportunityId());
        }
        return opportunity;
    }
}
