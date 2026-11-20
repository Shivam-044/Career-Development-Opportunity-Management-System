package com.studenttracker.model;

/** An entry in the shared skill catalogue. */
public final class Skill {

    private final long skillId;
    private final String skillName;

    public Skill(long skillId, String skillName) {
        this.skillId = skillId;
        this.skillName = skillName;
    }

    public long getSkillId() {
        return skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    @Override
    public String toString() {
        return skillName;
    }
}
