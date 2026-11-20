package com.studenttracker.export;

import com.studenttracker.model.Interview;
import com.studenttracker.util.CsvWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CsvInterviewExporter implements DataExporter<Interview> {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void export(Path destination, List<Interview> rows) throws IOException {
        try (CsvWriter writer = new CsvWriter(destination)) {
            writer.writeRow(List.of("Company", "Role", "Round", "Date and time", "Mode", "Result", "Feedback"));
            for (Interview interview : rows) {
                writer.writeRow(List.of(
                        interview.getCompanyName(),
                        interview.getRoleTitle(),
                        interview.getRoundName(),
                        TIMESTAMP.format(interview.getScheduledAt()),
                        interview.getMode().toString(),
                        interview.getResult().toString(),
                        interview.getFeedback() == null ? "" : interview.getFeedback()));
            }
        }
    }
}
