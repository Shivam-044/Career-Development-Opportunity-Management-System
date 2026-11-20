package com.studenttracker.fx.controller.dialog;

import com.studenttracker.fx.support.DialogForm;
import com.studenttracker.model.Company;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public final class CompanyDialogController implements DialogForm<Company> {

    @FXML
    private TextField nameField;
    @FXML
    private TextField industryField;
    @FXML
    private TextField websiteField;
    @FXML
    private TextField locationField;
    @FXML
    private TextArea notesArea;

    private Company editing;

    /** @param editing the company being edited, or {@code null} to add a new one */
    public void configure(Company editing) {
        this.editing = editing;
        if (editing != null) {
            nameField.setText(editing.getCompanyName());
            industryField.setText(editing.getIndustry());
            websiteField.setText(editing.getWebsite());
            locationField.setText(editing.getLocation());
            notesArea.setText(editing.getNotes());
        }
    }

    @Override
    public Company collect() {
        Company company = new Company(nameField.getText());
        company.setIndustry(industryField.getText());
        company.setWebsite(websiteField.getText());
        company.setLocation(locationField.getText());
        company.setNotes(notesArea.getText());
        if (editing != null) {
            company.setCompanyId(editing.getCompanyId());
            company.setArchived(editing.isArchived());
        }
        return company;
    }

    @Override
    public void focusFirstField() {
        nameField.requestFocus();
    }
}
