package com.studenttracker.service;

import com.studenttracker.dao.SkillDAO;
import com.studenttracker.model.Skill;
import com.studenttracker.model.StudentSkill;
import java.util.ArrayList;
import java.util.List;

class InMemorySkillDao implements SkillDAO {

    private final List<StudentSkill> skills = new ArrayList<>();
    private long nextSkillId = 1;

    @Override
    public List<Skill> findCatalog() {
        return skills.stream().map(skill -> new Skill(skill.getSkillId(), skill.getSkillName())).toList();
    }

    @Override
    public List<StudentSkill> findByStudent(long studentId) {
        return skills.stream().filter(skill -> skill.getStudentId() == studentId).toList();
    }

    @Override
    public boolean exists(long studentId, String skillName) {
        return skills.stream().anyMatch(skill -> skill.getStudentId() == studentId
                && skill.getSkillName().equalsIgnoreCase(skillName));
    }

    @Override
    public void insert(StudentSkill skill) {
        skill.setSkillId(nextSkillId++);
        skills.add(skill);
    }

    @Override
    public void update(StudentSkill skill) {
        skills.removeIf(existing -> existing.getSkillId().equals(skill.getSkillId()));
        skills.add(skill);
    }

    @Override
    public void delete(long studentId, long skillId) {
        skills.removeIf(skill -> skill.getStudentId() == studentId && skill.getSkillId() == skillId);
    }
}
