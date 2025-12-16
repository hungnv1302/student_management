package org.example.repository;

import org.example.config.DbConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentRepository {

    public boolean isEnrolled(String studentId, String classId) throws SQLException {
        String sql = """
            SELECT 1 FROM enrollments
            WHERE student_id = ?
              AND class_id = ?
              AND status = 'ENROLLED'
            LIMIT 1
        """;

        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ps.setString(2, classId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    public List<String> findEnrolledClassIdsInTerm(String studentId, String semester, int year) throws SQLException {
        String sql = """
            SELECT e.class_id
            FROM enrollments e
            JOIN class_sections cs ON cs.class_id = e.class_id
            WHERE e.student_id = ?
              AND e.status = 'ENROLLED'
              AND cs.semester = ?
              AND cs.year = ?
        """;

        List<String> ids = new ArrayList<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ps.setString(2, semester);
            ps.setInt(3, year);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getString(1));
        }
        return ids;
    }

    public int sumEnrolledCreditsInTerm(String studentId, String semester, int year) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(s.credit), 0) AS total_credits
            FROM enrollments e
            JOIN class_sections cs ON cs.class_id = e.class_id
            JOIN subjects s ON s.subject_id = cs.subject_id
            WHERE e.student_id = ?
              AND e.status = 'ENROLLED'
              AND cs.semester = ?
              AND cs.year = ?
        """;

        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ps.setString(2, semester);
            ps.setInt(3, year);

            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt("total_credits");
        }
    }

    /** ✅ Theo enum EnrollmentStatus của cháu: chỉ COMPLETED mới coi là đã đạt môn */
    public boolean hasPassedSubject(String studentId, String subjectId) throws SQLException {
        String sql = """
            SELECT 1
            FROM enrollments e
            JOIN class_sections cs ON cs.class_id = e.class_id
            WHERE e.student_id = ?
              AND cs.subject_id = ?
              AND e.status = 'COMPLETED'
            LIMIT 1
        """;

        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ps.setString(2, subjectId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    /** Insert: dùng NOW() cho created_at */
    public void insertEnrollment(String enrollmentId, String studentId, String classId) throws SQLException {
        String sql = """
            INSERT INTO enrollments(enrollment_id, student_id, class_id, status, created_at)
            VALUES (?, ?, ?, 'ENROLLED', NOW())
        """;

        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, enrollmentId);
            ps.setString(2, studentId);
            ps.setString(3, classId);
            ps.executeUpdate();
        }
    }

    public void cancelEnrollment(String studentId, String classId) throws SQLException {
        String sql = """
            UPDATE enrollments
            SET status = 'DROPPED'
            WHERE student_id = ?
              AND class_id = ?
              AND status = 'ENROLLED'
        """;

        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ps.setString(2, classId);
            ps.executeUpdate();
        }
    }
}
