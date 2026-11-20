package com.studenttracker.controller;

import com.studenttracker.model.Student;
import com.studenttracker.service.StudentService;
import com.studenttracker.ui.support.BackgroundTask;
import com.studenttracker.ui.support.Notifier;
import java.awt.Component;
import java.util.Optional;
import java.util.function.Consumer;

public final class StudentController {

    private final StudentService service;
    private final Component owner;

    public StudentController(StudentService service, Component owner) {
        this.service = service;
        this.owner = owner;
    }

    public void load(Consumer<Optional<Student>> onLoaded) {
        BackgroundTask.load(service::findProfile, onLoaded, error -> Notifier.error(owner, error));
    }

    /** Saves the profile; {@code onFailed} runs after the error has been shown, so the form can be re-enabled. */
    public void save(Student student, Consumer<Student> onSaved, Runnable onFailed) {
        BackgroundTask.load(() -> service.save(student), onSaved, error -> {
            Notifier.error(owner, error);
            onFailed.run();
        });
    }
}
