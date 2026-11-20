package com.studenttracker.fx.controller.dialog;

import com.studenttracker.enums.OpportunityStatus;
import com.studenttracker.enums.OpportunityType;
import com.studenttracker.enums.WorkMode;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.fx.support.Combos;
import com.studenttracker.fx.support.DialogForm;
import com.studenttracker.fx.support.FxFormats;
import com.studenttracker.model.Company;
import com.studenttracker.model.Opportunity;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public final class OpportunityDialogController implements DialogForm<Opportunity> {

    @FXML
    private ComboBox<Company> companyCombo;
    @FXML
    private TextField roleField;
    @FXML
    private ComboBox<OpportunityType> typeCombo;
    @FXML
    private ComboBox<OpportunityStatus> statusCombo;
    @FXML
    private TextField locationField;
    @FXML
    private ComboBox<WorkMode> workModeCombo;
    @FXML
    private DatePicker deadlinePicker;
    @FXML
    private TextField minCgpaField;
    @FXML
    private TextField sourceUrlField;
    @FXML
    private TextField skillsField;
    @FXML
    private TextArea descriptionArea;

    private Opportunity editing;

    @FXML
    private void initialize() {
        Combos.fill(typeCombo, OpportunityType.values());
        Combos.fill(statusCombo, OpportunityStatus.values());
        Combos.fillOptional(workModeCombo, WorkMode.values(), "Not specified");
        typeCombo.getSelectionModel().select(OpportunityType.INTERNSHIP);
        statusCombo.getSelectionModel().select(OpportunityStatus.OPEN);
        FxFormats.configure(deadlinePicker);
    }

    /**
     * @param companies companies the opportunity can belong to
     * @param editing   the opportunity being edited, or {@code null} to add a new one
     */
    public void configure(List<Company> companies, Opportunity editing) {
        this.editing = editing;
        companyCombo.setItems(FXCollections.observableArrayList(companies));
        if (editing == null) {
            return;
        }
        companies.stream()
                .filter(company -> company.getCompanyId() == editing.getCompanyId())
                .findFirst()
                .ifPresent(company -> companyCombo.getSelectionModel().select(company));
        roleField.setText(editing.getRoleTitle());
        typeCombo.getSelectionModel().select(editing.getType());
        statusCombo.getSelectionModel().select(editing.getStatus());
        locationField.setText(editing.getLocation());
        workModeCombo.getSelectionModel().select(editing.getWorkMode());
        deadlinePicker.setValue(editing.getDeadline());
        minCgpaField.setText(editing.getMinimumCgpa() == null ? "" : editing.getMinimumCgpa().toPlainString());
        sourceUrlField.setText(editing.getSourceUrl());
        skillsField.setText(String.join(", ", editing.getRequiredSkills()));
        descriptionArea.setText(editing.getDescription());
    }

    @Override
    public Opportunity collect() {
        Company company = companyCombo.getValue();
        if (company == null) {
            throw new ValidationException("Company is required.");
        }
        Opportunity opportunity = new Opportunity(company.getCompanyId(), roleField.getText(), typeCombo.getValue());
        opportunity.setCompanyName(company.getCompanyName());
        opportunity.setLocation(locationField.getText());
        opportunity.setWorkMode(workModeCombo.getValue());
        opportunity.setDeadline(deadlinePicker.getValue());
        opportunity.setMinimumCgpa(parseScore(minCgpaField.getText()));
        opportunity.setSourceUrl(sourceUrlField.getText());
        opportunity.setRequiredSkills(Arrays.asList(skillsField.getText().split(",")));
        opportunity.setStatus(statusCombo.getValue());
        opportunity.setDescription(descriptionArea.getText());
        if (editing != null) {
            opportunity.setOpportunityId(editing.getOpportunityId());
        }
        return opportunity;
    }

    @Override
    public void focusFirstField() {
        if (editing == null) {
            companyCombo.requestFocus();
        } else {
            roleField.requestFocus();
        }
    }

    private static BigDecimal parseScore(String text) {
        String value = text == null ? "" : text.trim();
        if (value.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Minimum CGPA must be a number such as 8.25.");
        }
    }
}
