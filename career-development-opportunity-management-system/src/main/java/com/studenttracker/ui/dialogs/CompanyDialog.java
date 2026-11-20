package com.studenttracker.ui.dialogs;

import com.studenttracker.model.Company;
import com.studenttracker.ui.support.Fields;
import com.studenttracker.ui.support.TextAreaField;
import java.awt.Component;
import javax.swing.JTextField;

public final class CompanyDialog extends FormDialog<Company> {

    private static final long serialVersionUID = 1L;

    private final JTextField name = Fields.text(26);
    private final JTextField industry = Fields.text(26);
    private final JTextField website = Fields.text(26);
    private final JTextField location = Fields.text(26);
    private final TextAreaField notes = new TextAreaField(4, 26);
    private final transient Company editing;

    /** @param editing the company being edited, or {@code null} to add a new one */
    public CompanyDialog(Component owner, Company editing) {
        super(owner, editing == null ? "Add company" : "Edit company",
                editing == null ? "Add company" : "Save changes");
        this.editing = editing;

        if (editing != null) {
            name.setText(editing.getCompanyName());
            industry.setText(editing.getIndustry());
            website.setText(editing.getWebsite());
            location.setText(editing.getLocation());
            notes.setText(editing.getNotes());
        }
        website.putClientProperty("JTextField.placeholderText", "https://");

        form().addRow("Company name", name, true);
        form().addRow("Industry", industry);
        form().addRow("Website", website);
        form().addRow("Location", location);
        form().addRow("Notes", notes);
        finishLayout(owner);
    }

    @Override
    protected Company collect() {
        Company company = new Company(name.getText());
        company.setIndustry(industry.getText());
        company.setWebsite(website.getText());
        company.setLocation(location.getText());
        company.setNotes(notes.getText());
        if (editing != null) {
            company.setCompanyId(editing.getCompanyId());
            company.setArchived(editing.isArchived());
        }
        return company;
    }
}
