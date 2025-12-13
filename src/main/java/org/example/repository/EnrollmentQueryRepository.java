package org.example.repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentQueryRepository {

    public int sumCreditsEnrolled(String studentId, String semester, int year, Connection c) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(s.credit), 0) AS total
            FROM public.enrollments e
            JOIN public.class_sections cs ON cs.class_id = e.class_id
            JOIN public.subjects s ON s.subject_id = cs.subject_id
            WHERE e.student_id = ?
              AND e.status = 'ENROLLED'
              AND cs.semester = ?
              AND cs.year = ?
            """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, semester);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("total");
            }
        }
    }

    public List<String> getEnrolledClassIds(String studentId, String semester, int year, Connection c) throws SQLException {
        String sql = """
            SELECT e.class_id
            FROM public.enrollments e
            JOIN public.class_sections cs ON cs.class_id = e.class_id
            WHERE e.student_id = ?
              AND e.status = 'ENROLLED'
              AND cs.semester = ?
              AND cs.year = ?
            """;
        List<String> ids = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, semester);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getString("class_id"));
            }
        }
        return ids;
    }
}
