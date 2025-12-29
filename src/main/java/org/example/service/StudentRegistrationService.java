package org.example.service;

import org.example.dto.EnrolledRow;
import org.example.dto.MyEnrollmentRow;
import org.example.dto.OpenClassRow;
import org.example.repository.EnrollmentRepository;
import org.example.service.exception.BusinessException;
import org.postgresql.util.PSQLException;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class StudentRegistrationService {

    private final EnrollmentRepository enrollmentRepo = new EnrollmentRepository();

    /** Danh sách lớp mở kỳ đang mở đăng ký + eligibility/reason (Java tự tính) */
    public List<OpenClassRow> getOpenClasses(String studentId) {
        Objects.requireNonNull(studentId, "studentId is null");
        try {
            return enrollmentRepo.listOpenSectionsForStudent(studentId);
        } catch (SQLException e) {
            throw new BusinessException(toNiceMessage(e));
        }
    }

    /** Search trên Java */
    public List<OpenClassRow> searchOpenClasses(String studentId, String keyword) {
        Objects.requireNonNull(studentId, "studentId is null");
        String k = keyword == null ? "" : keyword.trim().toLowerCase();

        List<OpenClassRow> all = getOpenClasses(studentId);
        if (k.isBlank()) return all;

        return all.stream().filter(r ->
                safe(r.getClassId()).contains(k) ||
                        safe(r.getSubjectId()).contains(k) ||
                        safe(r.getSubjectName()).contains(k) ||
                        String.valueOf(r.getCredit()).contains(k) ||
                        safe(r.getLecturerName()).contains(k) ||
                        safe(r.getScheduleText()).contains(k)
        ).toList();
    }

    /** Lớp SV đã đăng ký trong kỳ đang mở (open term) */
    public List<EnrolledRow> getEnrolledClasses(String studentId) {
        Objects.requireNonNull(studentId, "studentId is null");
        try {
            List<MyEnrollmentRow> rows = enrollmentRepo.listMyOpenEnrollments(studentId);

            return rows.stream()
                    .map(r -> new EnrolledRow(
                            r.getClassId(),
                            r.getSubjectName(),
                            safeKeep(r.getScheduleText()),
                            r.getStatus()
                    ))
                    .toList();

        } catch (SQLException e) {
            throw new BusinessException(toNiceMessage(e));
        }
    }

    public void register(String studentId, String classId) {
        Objects.requireNonNull(studentId, "studentId is null");
        Objects.requireNonNull(classId, "classId is null");
        try {
            enrollmentRepo.enrollStudent(studentId, classId);
        } catch (SQLException e) {
            throw new BusinessException(toNiceMessage(e));
        }
    }

    public void drop(String studentId, String classId) {
        Objects.requireNonNull(studentId, "studentId is null");
        Objects.requireNonNull(classId, "classId is null");
        try {
            enrollmentRepo.dropEnrollment(studentId, classId);
        } catch (SQLException e) {
            throw new BusinessException(toNiceMessage(e));
        }
    }

    /** Tổng tín chỉ SV đã đăng ký trong open term */
    public int getOpenCredits(String studentId) {
        Objects.requireNonNull(studentId, "studentId is null");
        try {
            return enrollmentRepo.getOpenCredits(studentId);
        } catch (SQLException e) {
            throw new BusinessException(toNiceMessage(e));
        }
    }

    private String safe(String s) { return s == null ? "" : s.toLowerCase(); }
    private String safeKeep(String s) { return s == null ? "" : s; }

    private String toNiceMessage(SQLException e) {
        // Ưu tiên message custom do mình throw (SQLException("..."))
        if (e.getMessage() != null && !e.getMessage().isBlank()) {
            // nếu là error từ server cũng sẽ nằm ở đây, ok luôn
        }

        if (e instanceof PSQLException pe && pe.getServerErrorMessage() != null) {
            String m = pe.getServerErrorMessage().getMessage();
            if (m != null && !m.isBlank()) return m;
        }

        if ("23505".equals(e.getSQLState())) return "Bạn đã đăng ký lớp này rồi.";
        return e.getMessage() != null ? e.getMessage() : ("Lỗi hệ thống: " + e);
    }
}
