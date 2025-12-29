package org.example.service;

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
    private final StudentScheduleService termService = new StudentScheduleService();

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

    /** ✅ danh sách lớp mở cho SV (open term) */
    public List<OpenClassRow> listAvailable(String studentId) {
        try {
            return repo.listOpenSectionsForStudent(studentId);
        } catch (SQLException e) {
            throw new BusinessException(toNiceMessage(e));
        }
    }

    /** ✅ lớp SV đã đăng ký trong open term */
    public List<MyEnrollmentRow> listMine(String studentId) {
        try {
            return repo.listMyOpenEnrollments(studentId);
        } catch (SQLException e) {
            throw new BusinessException(toNiceMessage(e));
        }
    }

    /** ✅ TKB theo kỳ cụ thể */
    public List<ScheduleRow> schedule(String studentId, int termYear, short termSem) {
        try {
            return repo.getStudentSchedule(studentId, termYear, termSem);
        } catch (SQLException e) {
            throw new BusinessException(toNiceMessage(e));
        }
    }

    /** ✅ TKB kỳ đang học */
    public List<ScheduleRow> scheduleCurrentTerm(String studentId) {
        StudentScheduleService.TermKey t = termService.getCurrentTerm();
        return schedule(studentId, t.year(), t.sem());
    }

    /** ✅ TKB kỳ đăng ký (open term) */
    public List<ScheduleRow> scheduleOpenTerm(String studentId) {
        StudentScheduleService.TermKey t = termService.getOpenTerm();
        return schedule(studentId, t.year(), t.sem());
    }

    private String toNiceMessage(SQLException e) {
        if (e instanceof PSQLException pe && pe.getServerErrorMessage() != null) {
            String m = pe.getServerErrorMessage().getMessage();
            if (m != null && !m.isBlank()) return m;
        }
        if ("23505".equals(e.getSQLState())) return "Bạn đã đăng ký lớp này rồi.";
        return "Lỗi hệ thống: " + e.getMessage();
    }
}
