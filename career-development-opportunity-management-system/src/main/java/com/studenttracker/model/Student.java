package com.studenttracker.model;

import com.studenttracker.util.Validators;
import java.math.BigDecimal;

public final class Student {

    public static final int MIN_GRADUATION_YEAR = 2000;
    public static final int MAX_GRADUATION_YEAR = 2100;

    private Long studentId;
    private String fullName;
    private String email;
    private String rollNumber;
    private String degree;
    private String department;
    private Integer graduationYear;
    private BigDecimal cgpa;

    public Student(String fullName) {
        setFullName(fullName);
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = Validators.requireText(fullName, "Full name", 100);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Validators.optionalEmail(email, "Email", 150);
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = Validators.optionalText(rollNumber, "Roll number", 50);
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = Validators.optionalText(degree, "Degree", 100);
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = Validators.optionalText(department, "Department", 100);
    }

    public Integer getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(Integer graduationYear) {
        this.graduationYear = Validators.optionalRange(
                graduationYear, "Graduation year", MIN_GRADUATION_YEAR, MAX_GRADUATION_YEAR);
    }

    public BigDecimal getCgpa() {
        return cgpa;
    }

    public void setCgpa(BigDecimal cgpa) {
        this.cgpa = Validators.optionalScore(cgpa, "CGPA");
    }
}
