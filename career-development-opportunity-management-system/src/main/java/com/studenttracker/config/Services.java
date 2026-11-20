package com.studenttracker.config;

import com.studenttracker.dao.ApplicationDAO;
import com.studenttracker.dao.CompanyDAO;
import com.studenttracker.dao.InterviewDAO;
import com.studenttracker.dao.OpportunityDAO;
import com.studenttracker.dao.SkillDAO;
import com.studenttracker.dao.StudentDAO;
import com.studenttracker.dao.jdbc.JdbcApplicationDAO;
import com.studenttracker.dao.jdbc.JdbcCompanyDAO;
import com.studenttracker.dao.jdbc.JdbcInterviewDAO;
import com.studenttracker.dao.jdbc.JdbcOpportunityDAO;
import com.studenttracker.dao.jdbc.JdbcSkillDAO;
import com.studenttracker.dao.jdbc.JdbcStudentDAO;
import com.studenttracker.export.CsvApplicationExporter;
import com.studenttracker.export.CsvInterviewExporter;
import com.studenttracker.service.ApplicationService;
import com.studenttracker.service.CompanyService;
import com.studenttracker.service.DashboardService;
import com.studenttracker.service.ExportService;
import com.studenttracker.service.InterviewService;
import com.studenttracker.service.OpportunityService;
import com.studenttracker.service.SkillService;
import com.studenttracker.service.StudentService;
import java.time.Clock;

/** The object graph for the running application: JDBC DAOs wired into the services. */
public record Services(
        StudentService students,
        SkillService skills,
        CompanyService companies,
        OpportunityService opportunities,
        ApplicationService applications,
        InterviewService interviews,
        DashboardService dashboard,
        ExportService exports) {

    public static Services createDefault() {
        Clock clock = Clock.systemDefaultZone();

        StudentDAO studentDao = new JdbcStudentDAO();
        SkillDAO skillDao = new JdbcSkillDAO();
        CompanyDAO companyDao = new JdbcCompanyDAO();
        OpportunityDAO opportunityDao = new JdbcOpportunityDAO();
        ApplicationDAO applicationDao = new JdbcApplicationDAO();
        InterviewDAO interviewDao = new JdbcInterviewDAO();

        return new Services(
                new StudentService(studentDao),
                new SkillService(skillDao, studentDao),
                new CompanyService(companyDao),
                new OpportunityService(opportunityDao, companyDao, clock),
                new ApplicationService(applicationDao, opportunityDao, studentDao, clock),
                new InterviewService(interviewDao, applicationDao, clock),
                new DashboardService(studentDao, skillDao, opportunityDao, applicationDao, interviewDao, clock),
                new ExportService(applicationDao, interviewDao,
                        new CsvApplicationExporter(), new CsvInterviewExporter()));
    }
}
