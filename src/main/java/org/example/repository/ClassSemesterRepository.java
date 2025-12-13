package org.example.repository;

import java.sql.*;

public class ClassSemesterRepository {
    public record SemYear(String semester, int year) {}

    public SemYear getSemYear(String classId, Connection c) throws SQLException {
        String sql = "SELECT semester, year FROM public.class_sections WHERE class_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Không tìm thấy class_section: " + classId);
                return new SemYear(rs.getString("semester"), rs.getInt("year"));
            }
        }
    }
}
