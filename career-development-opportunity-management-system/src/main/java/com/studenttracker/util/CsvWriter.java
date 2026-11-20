package com.studenttracker.util;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Minimal RFC 4180 writer. The file starts with a UTF-8 byte order mark so Excel
 * detects the encoding instead of garbling accented characters.
 */
public final class CsvWriter implements Closeable {

    private static final String LINE_END = "\r\n";

    private final BufferedWriter out;

    public CsvWriter(Path destination) throws IOException {
        this.out = Files.newBufferedWriter(destination, StandardCharsets.UTF_8);
        out.write('\uFEFF');
    }

    public void writeRow(List<String> cells) throws IOException {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                line.append(',');
            }
            line.append(escape(cells.get(i)));
        }
        line.append(LINE_END);
        out.write(line.toString());
    }

    static String escape(String cell) {
        if (cell == null) {
            return "";
        }
        boolean needsQuotes = cell.indexOf(',') >= 0 || cell.indexOf('"') >= 0
                || cell.indexOf('\n') >= 0 || cell.indexOf('\r') >= 0;
        return needsQuotes ? '"' + cell.replace("\"", "\"\"") + '"' : cell;
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
