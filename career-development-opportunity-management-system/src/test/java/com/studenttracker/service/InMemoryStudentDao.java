package com.studenttracker.service;

import com.studenttracker.dao.StudentDAO;
import com.studenttracker.model.Student;
import java.util.Optional;

class InMemoryStudentDao implements StudentDAO {

    private Student stored;
    private long nextId = 1;

    @Override
    public Optional<Student> find() {
        return Optional.ofNullable(stored);
    }

    @Override
    public long insert(Student student) {
        student.setStudentId(nextId++);
        stored = student;
        return student.getStudentId();
    }

    @Override
    public void update(Student student) {
        stored = student;
    }
}
