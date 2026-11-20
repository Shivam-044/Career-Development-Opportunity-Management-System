package com.studenttracker.fx.controller.dialog;

import com.studenttracker.enums.Proficiency;
import com.studenttracker.enums.SkillStatus;
import com.studenttracker.fx.support.Combos;
import com.studenttracker.fx.support.DialogForm;
import com.studenttracker.model.StudentSkill;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;

public final class SkillDialogController implements DialogForm<StudentSkill> {

    @FXML
    private ComboBox<String> nameCombo;
    @FXML
    private ComboBox<Proficiency> proficiencyCombo;
    @FXML
    private ComboBox<SkillStatus> statusCombo;
    @FXML
    private TextArea notesArea;

    private StudentSkill editing;

    @FXML
    private void initialize() {
        Combos.fill(proficiencyCombo, Proficiency.values());
        Combos.fill(statusCombo, SkillStatus.values());
        proficiencyCombo.getSelectionModel().select(Proficiency.BEGINNER);
        statusCombo.getSelectionModel().select(SkillStatus.LEARNING);
    }

    /** @param editing the skill being edited, or {@code null} to add a new one */
    public void configure(List<String> catalogNames, StudentSkill editing) {
        this.editing = editing;
        if (editing == null) {
            nameCombo.setItems(FXCollections.observableArrayList(catalogNames));
            return;
        }
        nameCombo.setItems(FXCollections.observableArrayList(editing.getSkillName()));
        nameCombo.getSelectionModel().selectFirst();
        nameCombo.setEditable(false);
        nameCombo.setDisable(true);
        proficiencyCombo.getSelectionModel().select(editing.getProficiency());
        statusCombo.getSelectionModel().select(editing.getLearningStatus());
        notesArea.setText(editing.getNotes());
    }

    @Override
    public StudentSkill collect() {
        String name = editing == null ? nameCombo.getEditor().getText() : editing.getSkillName();
        StudentSkill skill = new StudentSkill(name, proficiencyCombo.getValue(), statusCombo.getValue());
        skill.setNotes(notesArea.getText());
        if (editing != null) {
            skill.setStudentId(editing.getStudentId());
            skill.setSkillId(editing.getSkillId());
        }
        return skill;
    }

    @Override
    public void focusFirstField() {
        if (editing == null) {
            nameCombo.requestFocus();
        } else {
            proficiencyCombo.requestFocus();
        }
    }
}
