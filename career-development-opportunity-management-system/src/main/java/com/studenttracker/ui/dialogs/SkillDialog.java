package com.studenttracker.ui.dialogs;

import com.studenttracker.enums.Proficiency;
import com.studenttracker.enums.SkillStatus;
import com.studenttracker.model.StudentSkill;
import com.studenttracker.ui.support.TextAreaField;
import java.awt.Component;
import java.util.List;
import javax.swing.JComboBox;

public final class SkillDialog extends FormDialog<StudentSkill> {

    private static final long serialVersionUID = 1L;

    private final JComboBox<String> name;
    private final JComboBox<Proficiency> proficiency = new JComboBox<>(Proficiency.values());
    private final JComboBox<SkillStatus> status = new JComboBox<>(SkillStatus.values());
    private final TextAreaField notes = new TextAreaField(3, 24);
    private final transient StudentSkill editing;

    /** @param editing the skill being edited, or {@code null} to add a new one */
    public SkillDialog(Component owner, List<String> catalogNames, StudentSkill editing) {
        super(owner, editing == null ? "Add skill" : "Edit skill", editing == null ? "Add skill" : "Save changes");
        this.editing = editing;

        String[] options = editing == null
                ? catalogNames.toArray(new String[0])
                : new String[] {editing.getSkillName()};
        name = new JComboBox<>(options);
        name.setEditable(editing == null);
        if (editing == null) {
            name.setSelectedItem("");
        } else {
            name.setEnabled(false);
            proficiency.setSelectedItem(editing.getProficiency());
            status.setSelectedItem(editing.getLearningStatus());
            notes.setText(editing.getNotes());
        }

        form().addRow("Skill", name, true);
        form().addRow("Proficiency", proficiency, true);
        form().addRow("Status", status, true);
        form().addRow("Notes", notes);
        finishLayout(owner);
    }

    @Override
    protected StudentSkill collect() {
        Object typed = name.getSelectedItem();
        StudentSkill skill = new StudentSkill(
                typed == null ? "" : typed.toString(),
                (Proficiency) proficiency.getSelectedItem(),
                (SkillStatus) status.getSelectedItem());
        skill.setNotes(notes.getText());
        if (editing != null) {
            skill.setStudentId(editing.getStudentId());
            skill.setSkillId(editing.getSkillId());
        }
        return skill;
    }
}
