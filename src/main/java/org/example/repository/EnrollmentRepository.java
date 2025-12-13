package org.example.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EnrollmentRepository {

    public boolean existsStudentInClass(String studentId, String classId, Connection c) throws SQLException {
        String sql = "SELECT 1 FROM public.enrollments WHERE student_id = ? AND class_id = ? AND status = 'ENROLLED'";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, classId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean insert(String enrollmentId, String studentId, String classId, Connection c) throws SQLException {
        String sql = """
            INSERT INTO public.enrollments(enrollment_id, student_id, class_id, status)
            VALUES (?, ?, ?, 'ENROLLED')
            """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, enrollmentId);
            ps.setString(2, studentId);
            ps.setString(3, classId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean delete(String studentId, String classId, Connection c) throws SQLException {
        String sql = "DELETE FROM public.enrollments WHERE student_id = ? AND class_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, classId);
            return ps.executeUpdate() == 1;
        }
    }
}
