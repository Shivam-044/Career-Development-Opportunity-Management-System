package com.studenttracker.service;

import com.studenttracker.dao.InterviewDAO;
import com.studenttracker.enums.InterviewResult;
import com.studenttracker.model.Interview;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class InMemoryInterviewDao implements InterviewDAO {

    private final Map<Long, Interview> interviews = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public long insert(Interview interview) {
        long id = nextId++;
        interview.setInterviewId(id);
        interviews.put(id, interview);
        return id;
    }

    @Override
    public void update(Interview interview) {
        interviews.put(interview.getInterviewId(), interview);
    }

    @Override
    public void delete(long id) {
        interviews.remove(id);
    }

    @Override
    public Optional<Interview> findById(long id) {
        return Optional.ofNullable(interviews.get(id));
    }

    @Override
    public List<Interview> findAll() {
        return interviews.values().stream()
                .sorted(Comparator.comparing(Interview::getScheduledAt).reversed())
                .toList();
    }

    @Override
    public List<Interview> findUpcoming(LocalDateTime from, int limit) {
        return interviews.values().stream()
                .filter(interview -> interview.getResult() != InterviewResult.CANCELLED)
                .filter(interview -> !interview.getScheduledAt().isBefore(from))
                .sorted(Comparator.comparing(Interview::getScheduledAt))
                .limit(limit)
                .toList();
    }

    @Override
    public int countUpcoming(LocalDateTime from) {
        return findUpcoming(from, Integer.MAX_VALUE).size();
    }
}
