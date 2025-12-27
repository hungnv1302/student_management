package org.example.service;

import org.example.dto.TodayScheduleRow;
import org.example.repository.RegistrationConfigRepository;
import org.example.dto.MyEnrollmentRow;
import org.example.dto.OpenClassRow;
import org.example.dto.ScheduleRow;
import org.example.repository.EnrollmentRepository;
import org.example.service.exception.BusinessException;
import org.postgresql.util.PSQLException;

import java.sql.SQLException;
import java.util.List;

public class EnrollmentService {
    private final EnrollmentRepository repo = new EnrollmentRepository();

    public void enroll(String studentId, String classId) {
        try {
            repo.enrollStudent(studentId, classId);
        } catch (SQLException e) {
            throw new BusinessException(toNiceMessage(e));
        }
    }

    public void drop(String studentId, String classId) {
        try {
            repo.dropEnrollment(studentId, classId);
        } catch (SQLException e) {
            throw new BusinessException(toNiceMessage(e));
        }
    }

    public List<OpenClassRow> listAvailable(String studentId) {
        try {
            return repo.listAvailableSections(studentId);
        } catch (SQLException e) {
            throw new BusinessException(toNiceMessage(e));
        }
    }

    public List<MyEnrollmentRow> listMine(String studentId) {
        try {
            return repo.listMyEnrollments(studentId);
        } catch (SQLException e) {
            throw new BusinessException(toNiceMessage(e));
        }
    }

    public List<ScheduleRow> schedule(String studentId, short termNo) {
        try {
            return repo.getStudentSchedule(studentId, termNo);
        } catch (SQLException e) {
            throw new BusinessException(toNiceMessage(e));
        }
    }

    private String toNiceMessage(SQLException e) {
        // message từ RAISE EXCEPTION thường nằm ở ServerErrorMessage
        if (e instanceof PSQLException pe && pe.getServerErrorMessage() != null) {
            String m = pe.getServerErrorMessage().getMessage();
            if (m != null && !m.isBlank()) return m;
        }
        // unique violation (vd insert enroll trùng) -> báo thân thiện
        if ("23505".equals(e.getSQLState())) return "Bạn đã đăng ký lớp này rồi.";
        return "Lỗi hệ thống: " + e.getMessage();
    }

    public java.util.List<TodayScheduleRow> scheduleToday(String studentId) {
        try {
            Short openTerm = new RegistrationConfigRepository().getOpenTerm();
            if (openTerm == null) return java.util.List.of(); // chưa mở kỳ nào
            return repo.getStudentScheduleToday(studentId, openTerm);
        } catch (java.sql.SQLException e) {
            throw new BusinessException(toNiceMessage(e));
        }
    }
}
