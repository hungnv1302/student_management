package org.example.service;

import org.example.dto.LecturerExamRow;
import org.example.dto.LecturerTimetableRow;
import org.example.repository.TimeSlotRepository;
import org.example.service.exception.BusinessException;
import org.postgresql.util.PSQLException;

import java.sql.SQLException;
import java.util.List;

public class LecturerScheduleService {
    private final TimeSlotRepository repo = new TimeSlotRepository();

    public List<LecturerTimetableRow> timetable(String lecturerId, short termNo) {
        try { return repo.lecturerTimetable(lecturerId, termNo); }
        catch (SQLException e) { throw new BusinessException(toNiceMessage(e)); }
    }

    public List<LecturerExamRow> examSchedule(String lecturerId, short termNo) {
        try { return repo.lecturerExamSchedule(lecturerId, termNo); }
        catch (SQLException e) { throw new BusinessException(toNiceMessage(e)); }
    }

    private String toNiceMessage(SQLException e) {
        if (e instanceof PSQLException pe && pe.getServerErrorMessage() != null) {
            String m = pe.getServerErrorMessage().getMessage();
            if (m != null && !m.isBlank()) return m;
        }
        return "Lỗi hệ thống: " + e.getMessage();
    }
}
