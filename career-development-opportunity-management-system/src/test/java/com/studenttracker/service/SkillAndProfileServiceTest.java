package com.studenttracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.studenttracker.enums.Proficiency;
import com.studenttracker.enums.SkillStatus;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.model.Student;
import com.studenttracker.model.StudentSkill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SkillAndProfileServiceTest {

    private InMemoryStudentDao students;
    private StudentService profileService;
    private SkillService skillService;

    @BeforeEach
    void setUp() {
        students = new InMemoryStudentDao();
        profileService = new StudentService(students);
        skillService = new SkillService(new InMemorySkillDao(), students);
    }

    @Test
    void createsTheProfileOnFirstSave() {
        Student saved = profileService.save(new Student("Asha Verma"));

        assertEquals(1L, saved.getStudentId());
    }

    @Test
    void updatesTheExistingProfileInsteadOfCreatingASecond() {
        profileService.save(new Student("Asha Verma"));

        Student edited = profileService.save(new Student("Asha K. Verma"));

        assertEquals(1L, edited.getStudentId());
        assertEquals("Asha K. Verma", profileService.findProfile().get().getFullName());
    }

    @Test
    void skillsNeedAProfile() {
        StudentSkill java = new StudentSkill("Java", Proficiency.BEGINNER, SkillStatus.LEARNING);

        assertThrows(ValidationException.class, () -> skillService.add(java));
    }

    @Test
    void rejectsTheSameSkillTwice() {
        profileService.save(new Student("Asha Verma"));
        skillService.add(new StudentSkill("Java", Proficiency.BEGINNER, SkillStatus.LEARNING));

        assertThrows(ValidationException.class,
                () -> skillService.add(new StudentSkill("java", Proficiency.ADVANCED, SkillStatus.COMPLETED)));
    }

    @Test
    void filtersSkillsByLearningStatus() {
        profileService.save(new Student("Asha Verma"));
        skillService.add(new StudentSkill("Java", Proficiency.INTERMEDIATE, SkillStatus.LEARNING));
        skillService.add(new StudentSkill("Git", Proficiency.INTERMEDIATE, SkillStatus.COMPLETED));

        assertEquals(1, skillService.list(SkillStatus.LEARNING).size());
        assertEquals(2, skillService.list().size());
    }

    @Test
    void listIsEmptyWithoutAProfile() {
        assertTrue(skillService.list().isEmpty());
    }
}
