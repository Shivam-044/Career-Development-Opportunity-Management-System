package com.studenttracker.dao;

import com.studenttracker.model.Student;
import java.util.Optional;

public interface StudentDAO {

    /** Returns the single stored profile, if one exists. */
    Optional<Student> find();

    long insert(Student student);

    void update(Student student);
}
