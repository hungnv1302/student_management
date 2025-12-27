package org.example.service;

import org.example.dto.*;
import org.example.repository.EnrollmentRepository;
import org.example.repository.RegistrationConfigRepository;
import org.example.service.exception.BusinessException;
import org.postgresql.util.PSQLException;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class StudentRegistrationService {

    private final EnrollmentRepository enrollmentRepo = new EnrollmentRepository();
    private final RegistrationConfigRepository regRepo = new RegistrationConfigRepository();

    /** Danh sách lớp còn chỗ & đang OPEN ở kỳ đang mở đăng ký (DB tự lọc) */
    public List<OpenClassRow> getOpenClasses(String studentId) {
        Objects.requireNonNull(studentId, "studentId is null");
        try {
            return enrollmentRepo.listAvailableSections(studentId);
        } catch (SQLException e) {
            throw new BusinessException(toNiceMessage(e));
        }
    }

    /**
     * Search: vì function hiện tại chưa có tham số keyword,
     * nên search trên Java từ danh sách lớp mở (nhẹ, an toàn).
     */
    public List<OpenClassRow> searchOpenClasses(String studentId, String keyword) {
        Objects.requireNonNull(studentId, "studentId is null");
        String k = keyword == null ? "" : keyword.trim().toLowerCase();

        List<OpenClassRow> all = getOpenClasses(studentId);
        if (k.isBlank()) return all;

        return all.stream().filter(r ->
                safe(r.getClassId()).contains(k) ||
                        safe(r.getSubjectId()).contains(k) ||
                        safe(r.getSubjectName()).contains(k) ||
                        String.valueOf(r.getCredit()).contains(k)
        ).toList();
    }

    /** Lớp cháu đã đăng ký trong kỳ đang mở (DB tự join subjects/sections) */
    public List<EnrolledRow> getEnrolledClasses(String studentId) {
        try {
            List<MyEnrollmentRow> rows = enrollmentRepo.listMyEnrollments(studentId);

            // timeText: nếu muốn hiển thị lịch, ta sẽ gọi get_student_schedule() để build timeText.
            // Tạm thời cho "" để build chạy trước.
            return rows.stream()
                    .map(r -> new EnrolledRow(r.getClassId(), r.getSubjectName(), "", r.getStatus()))
                    .toList();

        } catch (SQLException e) {
            throw new BusinessException(toNiceMessage(e));
        }
    }

    /** Đăng ký lớp: DB tự check mở đăng ký, full, trùng lịch, 24 tín... */
    public void register(String studentId, String classId) {
        Objects.requireNonNull(studentId, "studentId is null");
        Objects.requireNonNull(classId, "classId is null");
        try {
            enrollmentRepo.enrollStudent(studentId, classId);
        } catch (SQLException e) {
            throw new BusinessException(toNiceMessage(e));
        }
    }

    /** Hủy đăng ký: DB tự check còn mở đăng ký + chưa finalized */
    public void drop(String studentId, String classId) {
        Objects.requireNonNull(studentId, "studentId is null");
        Objects.requireNonNull(classId, "classId is null");
        try {
            enrollmentRepo.dropEnrollment(studentId, classId);
        } catch (SQLException e) {
            throw new BusinessException(toNiceMessage(e));
        }
    }

    /** Thời khóa biểu theo term */
    public List<ScheduleRow> getSchedule(String studentId, short termNo) {
        Objects.requireNonNull(studentId, "studentId is null");
        try {
            return enrollmentRepo.getStudentSchedule(studentId, termNo);
        } catch (SQLException e) {
            throw new BusinessException(toNiceMessage(e));
        }
    }

    /** Lịch hôm nay: tự lấy open term (get_open_term) */
    public List<TodayScheduleRow> getScheduleToday(String studentId) {
        Objects.requireNonNull(studentId, "studentId is null");
        try {
            Short openTerm = regRepo.getOpenTerm();
            if (openTerm == null) return List.of();
            return enrollmentRepo.getStudentScheduleToday(studentId, openTerm);
        } catch (SQLException e) {
            throw new BusinessException(toNiceMessage(e));
        }
    }

    private String safe(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private String toNiceMessage(SQLException e) {
        if (e instanceof PSQLException pe && pe.getServerErrorMessage() != null) {
            String m = pe.getServerErrorMessage().getMessage();
            if (m != null && !m.isBlank()) return m;
        }
        if ("23505".equals(e.getSQLState())) return "Bạn đã đăng ký lớp này rồi.";
        return "Lỗi hệ thống: " + e.getMessage();
    }

    public int getCurrentCredits(String studentId) {
        try {
            return enrollmentRepo.getCurrentCredits(studentId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error getCurrentCredits: " + e.getMessage(), e);
        }
    }

}
