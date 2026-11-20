package com.studenttracker.service;

import com.studenttracker.dao.ApplicationDAO;
import com.studenttracker.dao.InterviewDAO;
import com.studenttracker.export.DataExporter;
import com.studenttracker.model.Interview;
import com.studenttracker.model.JobApplication;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ExportService {

    private final ApplicationDAO applicationDao;
    private final InterviewDAO interviewDao;
    private final DataExporter<JobApplication> applicationExporter;
    private final DataExporter<Interview> interviewExporter;

    public ExportService(ApplicationDAO applicationDao, InterviewDAO interviewDao,
                         DataExporter<JobApplication> applicationExporter,
                         DataExporter<Interview> interviewExporter) {
        this.applicationDao = applicationDao;
        this.interviewDao = interviewDao;
        this.applicationExporter = applicationExporter;
        this.interviewExporter = interviewExporter;
    }

    /** Writes every application to the file and returns how many rows were exported. */
    public int exportApplications(Path destination) throws IOException {
        List<JobApplication> rows = applicationDao.search("", null);
        applicationExporter.export(destination, rows);
        return rows.size();
    }

    public int exportInterviews(Path destination) throws IOException {
        List<Interview> rows = interviewDao.findAll();
        interviewExporter.export(destination, rows);
        return rows.size();
    }
}
