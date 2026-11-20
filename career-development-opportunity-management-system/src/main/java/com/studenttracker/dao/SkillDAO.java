package com.studenttracker.dao;

import com.studenttracker.model.Skill;
import com.studenttracker.model.StudentSkill;
import java.util.List;

public interface SkillDAO {

    List<Skill> findCatalog();

    List<StudentSkill> findByStudent(long studentId);

    boolean exists(long studentId, String skillName);

    /** Adds the skill to the catalogue if needed, then links it to the student. */
    void insert(StudentSkill skill);

    void update(StudentSkill skill);

    void delete(long studentId, long skillId);
}
