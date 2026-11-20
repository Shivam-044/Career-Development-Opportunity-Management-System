package com.studenttracker.dao.jdbc;

import com.studenttracker.dao.SkillDAO;
import com.studenttracker.enums.Proficiency;
import com.studenttracker.enums.SkillStatus;
import com.studenttracker.model.Skill;
import com.studenttracker.model.StudentSkill;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcSkillDAO implements SkillDAO {

    @Override
    public List<Skill> findCatalog() {
        return JdbcUtil.execute("Couldn't load the skill list.", connection -> {
            List<Skill> skills = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT skill_id, skill_name FROM skills ORDER BY skill_name");
                 ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    skills.add(new Skill(rs.getLong("skill_id"), rs.getString("skill_name")));
                }
            }
            return skills;
        });
    }

    @Override
    public List<StudentSkill> findByStudent(long studentId) {
        String sql = "SELECT ss.student_id, ss.skill_id, s.skill_name, ss.proficiency, "
                + "ss.learning_status, ss.notes "
                + "FROM student_skills ss JOIN skills s ON s.skill_id = ss.skill_id "
                + "WHERE ss.student_id = ? ORDER BY s.skill_name";
        return JdbcUtil.execute("Couldn't load your skills.", connection -> {
            List<StudentSkill> skills = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, studentId);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        skills.add(map(rs));
                    }
                }
            }
            return skills;
        });
    }

    @Override
    public boolean exists(long studentId, String skillName) {
        String sql = "SELECT 1 FROM student_skills ss JOIN skills s ON s.skill_id = ss.skill_id "
                + "WHERE ss.student_id = ? AND s.skill_name = ?";
        return JdbcUtil.execute("Couldn't check your skills.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, studentId);
                statement.setString(2, skillName);
                try (ResultSet rs = statement.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    @Override
    public void insert(StudentSkill skill) {
        JdbcUtil.executeInTransaction("Couldn't save the skill.", connection -> {
            long skillId = SkillCatalog.findOrCreate(connection, skill.getSkillName());
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO student_skills (student_id, skill_id, proficiency, learning_status, notes) "
                            + "VALUES (?, ?, ?, ?, ?)")) {
                statement.setLong(1, skill.getStudentId());
                statement.setLong(2, skillId);
                statement.setString(3, skill.getProficiency().name());
                statement.setString(4, skill.getLearningStatus().name());
                statement.setString(5, skill.getNotes());
                statement.executeUpdate();
            }
            skill.setSkillId(skillId);
            return null;
        });
    }

    @Override
    public void update(StudentSkill skill) {
        JdbcUtil.perform("Couldn't save the skill.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE student_skills SET proficiency = ?, learning_status = ?, notes = ? "
                            + "WHERE student_id = ? AND skill_id = ?")) {
                statement.setString(1, skill.getProficiency().name());
                statement.setString(2, skill.getLearningStatus().name());
                statement.setString(3, skill.getNotes());
                statement.setLong(4, skill.getStudentId());
                statement.setLong(5, skill.getSkillId());
                statement.executeUpdate();
            }
        });
    }

    @Override
    public void delete(long studentId, long skillId) {
        JdbcUtil.perform("Couldn't remove the skill.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM student_skills WHERE student_id = ? AND skill_id = ?")) {
                statement.setLong(1, studentId);
                statement.setLong(2, skillId);
                statement.executeUpdate();
            }
        });
    }

    private static StudentSkill map(ResultSet rs) throws SQLException {
        StudentSkill skill = new StudentSkill(
                rs.getString("skill_name"),
                Proficiency.valueOf(rs.getString("proficiency")),
                SkillStatus.valueOf(rs.getString("learning_status")));
        skill.setStudentId(rs.getLong("student_id"));
        skill.setSkillId(rs.getLong("skill_id"));
        skill.setNotes(rs.getString("notes"));
        return skill;
    }
}
