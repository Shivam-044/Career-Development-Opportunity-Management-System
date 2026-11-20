package com.studenttracker.export;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.studenttracker.enums.InterviewMode;
import com.studenttracker.model.Interview;
import com.studenttracker.model.JobApplication;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class CsvExportersTest {

    @Test
    void exportsApplicationsWithNotesContainingCommas() throws IOException {
        JobApplication application = new JobApplication(1, 1, LocalDate.of(2026, 11, 2));
        application.setDisplayNames("Brightwave", "Java Intern");
        application.setNotes("Referred by a senior, follow up Friday");
        Path file = Files.createTempFile("applications", ".csv");
        try {
            new CsvApplicationExporter().export(file, List.of(application));

            assertEquals("\uFEFFCompany,Role,Status,Applied on,Notes\r\n"
                            + "Brightwave,Java Intern,Applied,2026-11-02,\"Referred by a senior, follow up Friday\"\r\n",
                    Files.readString(file, StandardCharsets.UTF_8));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void exportsInterviews() throws IOException {
        Interview interview = new Interview(1, "Technical round", LocalDateTime.of(2026, 11, 20, 15, 30),
                InterviewMode.ONLINE);
        interview.setDisplayNames("Brightwave", "Java Intern");
        Path file = Files.createTempFile("interviews", ".csv");
        try {
            new CsvInterviewExporter().export(file, List.of(interview));

            assertEquals("\uFEFFCompany,Role,Round,Date and time,Mode,Result,Feedback\r\n"
                            + "Brightwave,Java Intern,Technical round,2026-11-20 15:30,Online,Pending,\r\n",
                    Files.readString(file, StandardCharsets.UTF_8));
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
