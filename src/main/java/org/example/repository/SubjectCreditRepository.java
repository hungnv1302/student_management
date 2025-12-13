package org.example.repository;

import java.sql.*;

public class SubjectCreditRepository {
    public int getCreditByClassId(String classId, Connection c) throws SQLException {
        String sql = """
            SELECT s.credit
            FROM public.class_sections cs
            JOIN public.subjects s ON s.subject_id = cs.subject_id
            WHERE cs.class_id = ?
            """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Không tìm thấy class/subject để lấy credit: " + classId);
                return rs.getInt("credit");
            }
        }
    }
}
