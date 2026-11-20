package com.studenttracker.export;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** Writes a list of records to a file. Each implementation chooses the format and columns. */
public interface DataExporter<T> {

    void export(Path destination, List<T> rows) throws IOException;
}
