package com.studenttracker.service;

import com.studenttracker.dao.SkillDAO;
import com.studenttracker.dao.StudentDAO;
import com.studenttracker.enums.SkillStatus;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.model.Skill;
import com.studenttracker.model.Student;
import com.studenttracker.model.StudentSkill;
import java.util.List;
import java.util.Optional;

public class SkillService {

    private final SkillDAO skillDao;
    private final StudentDAO studentDao;

    public SkillService(SkillDAO skillDao, StudentDAO studentDao) {
        this.skillDao = skillDao;
        this.studentDao = studentDao;
    }

    public List<StudentSkill> list() {
        return list(null);
    }

    /** Lists the student's skills, optionally only those with the given learning status. */
    public List<StudentSkill> list(SkillStatus status) {
        Optional<Student> student = studentDao.find();
        if (student.isEmpty()) {
            return List.of();
        }
        List<StudentSkill> skills = skillDao.findByStudent(student.get().getStudentId());
        if (status == null) {
            return skills;
        }
        return skills.stream().filter(skill -> skill.getLearningStatus() == status).toList();
    }

    public List<Skill> catalog() {
        return skillDao.findCatalog();
    }

    public void add(StudentSkill skill) {
        long studentId = requireStudentId();
        if (skillDao.exists(studentId, skill.getSkillName())) {
            throw new ValidationException(skill.getSkillName() + " is already in your skill list.");
        }
        skill.setStudentId(studentId);
        skillDao.insert(skill);
    }

    public void update(StudentSkill skill) {
        skill.setStudentId(requireStudentId());
        skillDao.update(skill);
    }

    public void remove(StudentSkill skill) {
        skillDao.delete(requireStudentId(), skill.getSkillId());
    }

    private long requireStudentId() {
        return studentDao.find()
                .orElseThrow(() -> new ValidationException(
                        "Create your profile before adding skills. Use the Profile screen first."))
                .getStudentId();
    }
}
