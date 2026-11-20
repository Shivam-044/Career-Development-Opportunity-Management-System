package com.studenttracker.ui.panels;

import com.studenttracker.config.Services;
import com.studenttracker.controller.StudentController;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.model.Student;
import com.studenttracker.ui.support.Fields;
import com.studenttracker.ui.support.FormPanel;
import com.studenttracker.ui.support.Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class ProfilePanel extends FeaturePanel {

    private static final long serialVersionUID = 1L;

    private final transient StudentController controller;
    private final JTextField fullName = Fields.text(28);
    private final JTextField email = Fields.text(28);
    private final JTextField rollNumber = Fields.text(28);
    private final JTextField degree = Fields.text(28);
    private final JTextField department = Fields.text(28);
    private final JTextField graduationYear = Fields.text(8);
    private final JTextField cgpa = Fields.text(8);
    private final JLabel message = new JLabel(" ");
    private final JButton save = Theme.primaryButton("Save profile");

    public ProfilePanel(Services services) {
        super("Profile", "Your academic details. Skills and applications are linked to this profile.");
        controller = new StudentController(services.students(), this);

        FormPanel form = new FormPanel();
        form.addRow("Full name", fullName, true);
        form.addRow("Email", email);
        form.addRow("Roll number", rollNumber);
        form.addRow("Degree", degree);
        form.addRow("Department", department);
        form.addRow("Graduation year", graduationYear);
        form.addRow("CGPA", cgpa);

        JButton reset = Theme.secondaryButton("Discard changes");
        reset.addActionListener(event -> {
            showMessage(" ", Theme.MUTED);
            onShow();
        });
        save.addActionListener(event -> saveProfile());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(save);
        buttons.add(reset);
        message.setFont(Theme.font(Font.PLAIN, 13f));

        JPanel footer = new JPanel(new BorderLayout(0, 8));
        footer.setOpaque(false);
        footer.add(buttons, BorderLayout.NORTH);
        footer.add(message, BorderLayout.SOUTH);

        JPanel card = Theme.card(24);
        card.setLayout(new BorderLayout(0, 12));
        card.add(form, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(card, BorderLayout.NORTH);
        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(holder, BorderLayout.WEST);
        card.setPreferredSize(new Dimension(560, card.getPreferredSize().height));
        add(left, BorderLayout.CENTER);
    }

    @Override
    public void onShow() {
        controller.load(this::fill);
    }

    private void fill(Optional<Student> profile) {
        Student student = profile.orElse(null);
        fullName.setText(student == null ? "" : student.getFullName());
        email.setText(student == null ? "" : nullToEmpty(student.getEmail()));
        rollNumber.setText(student == null ? "" : nullToEmpty(student.getRollNumber()));
        degree.setText(student == null ? "" : nullToEmpty(student.getDegree()));
        department.setText(student == null ? "" : nullToEmpty(student.getDepartment()));
        graduationYear.setText(student == null || student.getGraduationYear() == null
                ? "" : student.getGraduationYear().toString());
        cgpa.setText(student == null || student.getCgpa() == null ? "" : student.getCgpa().toPlainString());
        if (student == null) {
            showMessage("No profile yet. Fill in your name and save to get started.", Theme.MUTED);
        }
    }

    private void saveProfile() {
        Student student;
        try {
            student = new Student(fullName.getText());
            student.setEmail(email.getText());
            student.setRollNumber(rollNumber.getText());
            student.setDegree(degree.getText());
            student.setDepartment(department.getText());
            student.setGraduationYear(Fields.parseWholeNumber(graduationYear.getText(), "Graduation year"));
            student.setCgpa(Fields.parseScore(cgpa.getText(), "CGPA"));
        } catch (ValidationException ex) {
            showMessage(ex.getMessage(), Theme.DANGER);
            return;
        }
        save.setEnabled(false);
        controller.save(student,
                saved -> {
                    save.setEnabled(true);
                    showMessage("Profile saved.", Theme.SUCCESS);
                },
                () -> save.setEnabled(true));
    }

    private void showMessage(String text, Color color) {
        message.setForeground(color);
        message.setText(text);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
