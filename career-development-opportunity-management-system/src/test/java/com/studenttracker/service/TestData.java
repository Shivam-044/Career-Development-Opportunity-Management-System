package com.studenttracker.service;

import com.studenttracker.enums.OpportunityType;
import com.studenttracker.model.Company;
import com.studenttracker.model.Opportunity;
import com.studenttracker.model.Student;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/** Shared fixtures: a fixed clock (13 Nov 2026, 09:00 UTC) and small builders. */
final class TestData {

    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-13T09:00:00Z"), ZoneOffset.UTC);
    static final LocalDate TODAY = LocalDate.of(2026, 11, 13);

    private TestData() {
    }

    static Student student(InMemoryStudentDao dao, String cgpa) {
        Student student = new Student("Test Student");
        student.setCgpa(new BigDecimal(cgpa));
        dao.insert(student);
        return student;
    }

    static Company company(InMemoryCompanyDao dao, String name) {
        Company company = new Company(name);
        dao.insert(company);
        return company;
    }

    static Opportunity opportunity(InMemoryOpportunityDao dao, Company company, String role, LocalDate deadline) {
        Opportunity opportunity = new Opportunity(company.getCompanyId(), role, OpportunityType.INTERNSHIP);
        opportunity.setCompanyName(company.getCompanyName());
        opportunity.setDeadline(deadline);
        dao.insert(opportunity);
        return opportunity;
    }
}
