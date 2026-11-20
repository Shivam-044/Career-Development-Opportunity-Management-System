package com.studenttracker.export;

import com.studenttracker.model.JobApplication;
import com.studenttracker.util.CsvWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class CsvApplicationExporter implements DataExporter<JobApplication> {

    @Override
    public void export(Path destination, List<JobApplication> rows) throws IOException {
        try (CsvWriter writer = new CsvWriter(destination)) {
            writer.writeRow(List.of("Company", "Role", "Status", "Applied on", "Notes"));
            for (JobApplication application : rows) {
                writer.writeRow(List.of(
                        application.getCompanyName(),
                        application.getRoleTitle(),
                        application.getStatus().toString(),
                        application.getAppliedOn().toString(),
                        nullToEmpty(application.getNotes())));
            }
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
