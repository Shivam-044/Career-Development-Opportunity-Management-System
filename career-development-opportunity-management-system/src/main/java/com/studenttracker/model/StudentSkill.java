package com.studenttracker.model;

import com.studenttracker.enums.Proficiency;
import com.studenttracker.enums.SkillStatus;
import com.studenttracker.util.Validators;

/** The student's progress in one skill. */
public final class StudentSkill {

    private Long studentId;
    private Long skillId;
    private String skillName;
    private Proficiency proficiency;
    private SkillStatus learningStatus;
    private String notes;

    public StudentSkill(String skillName, Proficiency proficiency, SkillStatus learningStatus) {
        this.skillName = Validators.requireText(skillName, "Skill name", 100);
        this.proficiency = Validators.requireNonNull(proficiency, "Proficiency");
        this.learningStatus = Validators.requireNonNull(learningStatus, "Learning status");
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getSkillId() {
        return skillId;
    }

    public void setSkillId(Long skillId) {
        this.skillId = skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    public Proficiency getProficiency() {
        return proficiency;
    }

    public void setProficiency(Proficiency proficiency) {
        this.proficiency = Validators.requireNonNull(proficiency, "Proficiency");
    }

    public SkillStatus getLearningStatus() {
        return learningStatus;
    }

    public void setLearningStatus(SkillStatus learningStatus) {
        this.learningStatus = Validators.requireNonNull(learningStatus, "Learning status");
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = Validators.optionalText(notes, "Notes", 500);
    }
}
