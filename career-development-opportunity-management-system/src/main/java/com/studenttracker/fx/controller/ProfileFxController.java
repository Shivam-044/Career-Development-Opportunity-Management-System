package com.studenttracker.fx.controller;

import com.studenttracker.exception.ValidationException;
import com.studenttracker.fx.support.FxAnimations;
import com.studenttracker.fx.support.FxContext;
import com.studenttracker.fx.support.FxFormats;
import com.studenttracker.model.Student;
import java.math.BigDecimal;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

public final class ProfileFxController extends BaseFxController {

    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField rollNumberField;
    @FXML
    private TextField degreeField;
    @FXML
    private TextField departmentField;
    @FXML
    private TextField graduationYearField;
    @FXML
    private TextField cgpaField;
    @FXML
    private Label messageLabel;
    @FXML
    private Button saveButton;
    @FXML
    private Button discardButton;
    @FXML
    private Label previewInitials;
    @FXML
    private Label previewName;
    @FXML
    private Label previewDetail;
    @FXML
    private Label previewCgpa;
    @FXML
    private ProgressBar previewCgpaBar;
    @FXML
    private Label previewRoll;
    @FXML
    private Label previewEmail;

    public ProfileFxController(FxContext ctx) {
        super(ctx);
    }

    @Override
    public void onShow() {
        load(services.students()::findProfile, this::fill, saveButton, discardButton);
    }

    @FXML
    private void save() {
        Student student;
        try {
            student = readForm();
        } catch (ValidationException ex) {
            showMessage(ex.getMessage(), true);
            return;
        }
        hideMessage();
        load(() -> services.students().save(student), saved -> {
            showPreview(saved);
            showMessage("Profile saved.", false);
            ctx.toasts().success("Profile saved.");
            ctx.fireProfileChanged();
        }, saveButton, discardButton);
    }

    @FXML
    private void discard() {
        hideMessage();
        onShow();
    }

    private Student readForm() {
        Student student = new Student(fullNameField.getText());
        student.setEmail(emailField.getText());
        student.setRollNumber(rollNumberField.getText());
        student.setDegree(degreeField.getText());
        student.setDepartment(departmentField.getText());
        student.setGraduationYear(parseYear(graduationYearField.getText()));
        student.setCgpa(parseScore(cgpaField.getText()));
        return student;
    }

    private void fill(Optional<Student> profile) {
        if (profile.isEmpty()) {
            for (TextField field : new TextField[] {fullNameField, emailField, rollNumberField, degreeField,
                    departmentField, graduationYearField, cgpaField}) {
                field.clear();
            }
            showPreview(null);
            showMessage("No profile yet. Add your name and save to get started.", false);
            return;
        }
        Student student = profile.get();
        fullNameField.setText(student.getFullName());
        emailField.setText(nullToEmpty(student.getEmail()));
        rollNumberField.setText(nullToEmpty(student.getRollNumber()));
        degreeField.setText(nullToEmpty(student.getDegree()));
        departmentField.setText(nullToEmpty(student.getDepartment()));
        graduationYearField.setText(student.getGraduationYear() == null ? "" : student.getGraduationYear().toString());
        cgpaField.setText(student.getCgpa() == null ? "" : student.getCgpa().toPlainString());
        showPreview(student);
    }

    private void showPreview(Student student) {
        if (student == null) {
            previewInitials.setText("?");
            previewName.setText("Your profile");
            previewDetail.setText("Fill in the form to see it here.");
            previewCgpa.setText("-");
            previewCgpaBar.setProgress(0);
            previewRoll.setText("-");
            previewEmail.setText("-");
            return;
        }
        previewInitials.setText(FxFormats.initials(student.getFullName()));
        previewName.setText(student.getFullName());

        StringBuilder detail = new StringBuilder();
        append(detail, student.getDegree());
        append(detail, student.getDepartment());
        if (student.getGraduationYear() != null) {
            append(detail, "Class of " + student.getGraduationYear());
        }
        previewDetail.setText(detail.length() == 0 ? "No academic details yet." : detail.toString());

        BigDecimal cgpa = student.getCgpa();
        previewCgpa.setText(cgpa == null ? "-" : cgpa.toPlainString() + " / 10");
        FxAnimations.animateProgress(previewCgpaBar, cgpa == null ? 0 : cgpa.doubleValue() / 10.0);
        previewRoll.setText(student.getRollNumber() == null ? "-" : student.getRollNumber());
        previewEmail.setText(student.getEmail() == null ? "-" : student.getEmail());
    }

    private static void append(StringBuilder builder, String part) {
        if (part != null && !part.isBlank()) {
            if (builder.length() > 0) {
                builder.append("  |  ");
            }
            builder.append(part);
        }
    }

    private void showMessage(String text, boolean error) {
        messageLabel.setText(text);
        messageLabel.getStyleClass().removeAll("form-error", "form-hint");
        messageLabel.getStyleClass().add(error ? "form-error" : "form-hint");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
        if (error) {
            FxAnimations.shake(messageLabel);
        }
    }

    private void hideMessage() {
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }

    private static Integer parseYear(String text) {
        String value = text == null ? "" : text.trim();
        if (value.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Graduation year must be a whole number.");
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
            throw new ValidationException("CGPA must be a number such as 8.25.");
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
