package com.studenttracker.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class CsvWriterTest {

    @Test
    void leavesPlainValuesUntouched() {
        assertEquals("Java Intern", CsvWriter.escape("Java Intern"));
    }

    @Test
    void quotesValuesThatContainCommas() {
        assertEquals("\"Pune, India\"", CsvWriter.escape("Pune, India"));
    }

    @Test
    void doublesEmbeddedQuotes() {
        assertEquals("\"said \"\"hello\"\"\"", CsvWriter.escape("said \"hello\""));
    }

    @Test
    void quotesValuesWithLineBreaks() {
        assertEquals("\"line one\nline two\"", CsvWriter.escape("line one\nline two"));
    }

    @Test
    void writesNullAsAnEmptyCell() {
        assertEquals("", CsvWriter.escape(null));
    }

    @Test
    void writesAByteOrderMarkAndWindowsLineEndings() throws IOException {
        Path file = Files.createTempFile("csv-writer-test", ".csv");
        try {
            try (CsvWriter writer = new CsvWriter(file)) {
                writer.writeRow(List.of("a", "b,c"));
                writer.writeRow(List.of("1", "2"));
            }
            String content = Files.readString(file, StandardCharsets.UTF_8);
            assertEquals("\uFEFFa,\"b,c\"\r\n1,2\r\n", content);
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
