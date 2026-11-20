package com.studenttracker.dao.jdbc;

import com.studenttracker.dao.StudentDAO;
import com.studenttracker.model.Student;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class JdbcStudentDAO implements StudentDAO {

    private static final String SELECT = "SELECT student_id, full_name, email, roll_number, degree, "
            + "department, graduation_year, cgpa FROM student_profile ORDER BY student_id LIMIT 1";
    private static final String INSERT = "INSERT INTO student_profile "
            + "(full_name, email, roll_number, degree, department, graduation_year, cgpa) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE student_profile SET full_name = ?, email = ?, "
            + "roll_number = ?, degree = ?, department = ?, graduation_year = ?, cgpa = ? "
            + "WHERE student_id = ?";

    @Override
    public Optional<Student> find() {
        return JdbcUtil.execute("Couldn't load your profile.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(SELECT);
                 ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.<Student>empty();
            }
        });
    }

    @Override
    public long insert(Student student) {
        return JdbcUtil.execute("Couldn't save your profile.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
                bind(statement, student);
                statement.executeUpdate();
                return JdbcUtil.generatedKey(statement);
            }
        });
    }

    @Override
    public void update(Student student) {
        JdbcUtil.perform("Couldn't save your profile.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(UPDATE)) {
                bind(statement, student);
                statement.setLong(8, student.getStudentId());
                statement.executeUpdate();
            }
        });
    }

    private static void bind(PreparedStatement statement, Student student) throws SQLException {
        statement.setString(1, student.getFullName());
        statement.setString(2, student.getEmail());
        statement.setString(3, student.getRollNumber());
        statement.setString(4, student.getDegree());
        statement.setString(5, student.getDepartment());
        JdbcUtil.setInteger(statement, 6, student.getGraduationYear());
        statement.setBigDecimal(7, student.getCgpa());
    }

    private static Student map(ResultSet rs) throws SQLException {
        Student student = new Student(rs.getString("full_name"));
        student.setStudentId(rs.getLong("student_id"));
        student.setEmail(rs.getString("email"));
        student.setRollNumber(rs.getString("roll_number"));
        student.setDegree(rs.getString("degree"));
        student.setDepartment(rs.getString("department"));
        student.setGraduationYear(JdbcUtil.getInteger(rs, "graduation_year"));
        student.setCgpa(rs.getBigDecimal("cgpa"));
        return student;
    }
}
