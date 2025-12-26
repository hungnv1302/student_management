package org.example.service;

import org.example.dto.ScheduleRow;
import org.example.repository.EnrollmentRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class StudentScheduleService {

    private final EnrollmentRepository repo = new EnrollmentRepository();

    public short getCurrentOpenTerm() {
        try {
            return repo.getOpenTerm();
        } catch (SQLException e) {
            throw new RuntimeException("DB error getOpenTerm: " + e.getMessage(), e);
        }
    }

    public List<ScheduleRow> getScheduleForCurrentTerm(String studentId) {
        Objects.requireNonNull(studentId, "studentId is null");
        try {
            short termNo = repo.getOpenTerm();
            return repo.getStudentSchedule(studentId, termNo);
        } catch (SQLException e) {
            throw new RuntimeException("DB error getStudentSchedule: " + e.getMessage(), e);
        }
    }
}
