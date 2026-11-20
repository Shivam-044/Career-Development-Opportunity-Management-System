package com.studenttracker.service;

import com.studenttracker.dao.StudentDAO;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.model.Student;
import java.util.Optional;

/** Owns the single student profile: it is created once and edited from then on. */
public class StudentService {

    private final StudentDAO studentDao;

    public StudentService(StudentDAO studentDao) {
        this.studentDao = studentDao;
    }

    public Optional<Student> findProfile() {
        return studentDao.find();
    }

    public Student requireProfile() {
        return studentDao.find().orElseThrow(() -> new ValidationException(
                "Create your profile first. Skills and applications are linked to it."));
    }

    public Student save(Student student) {
        Optional<Student> existing = studentDao.find();
        if (existing.isPresent()) {
            student.setStudentId(existing.get().getStudentId());
            studentDao.update(student);
        } else {
            student.setStudentId(studentDao.insert(student));
        }
        return student;
    }
}
