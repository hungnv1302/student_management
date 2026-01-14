package org.example.repository;

import org.example.config.DbConfig;
import org.example.dto.GradeRowDto;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LecturerScoreRepository {

    public boolean isAssigned(String lecturerId, String classId) throws SQLException {
        String sql = """
            SELECT 1
            FROM qlsv.teaching_assignments
            WHERE lecturer_id = ? AND class_id = ?
            LIMIT 1
        """;
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, lecturerId);
            ps.setString(2, classId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<GradeRowDto> loadRows(String lecturerId, String classId) throws SQLException {
        // ✅ join thẳng persons.person_id = enrollments.student_id (theo dữ liệu bạn chụp)
        String sql = """
            SELECT
              e.enroll_id,
              e.student_id,
              p.full_name,
              e.midterm_score,
              e.final_score,
              e.total_score,
              e.is_finalized
            FROM qlsv.enrollments e
            JOIN qlsv.persons p ON p.person_id = e.student_id
            WHERE e.class_id = ?
              AND EXISTS (
                SELECT 1 FROM qlsv.teaching_assignments ta
                WHERE ta.class_id = e.class_id AND ta.lecturer_id = ?
              )
            ORDER BY e.student_id
        """;

        List<GradeRowDto> out = new ArrayList<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, classId);
            ps.setString(2, lecturerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GradeRowDto r = new GradeRowDto();
                    r.setEnrollId(rs.getInt("enroll_id"));
                    r.setStudentId(rs.getString("student_id"));
                    r.setFullName(rs.getString("full_name"));
                    r.setMidterm((BigDecimal) rs.getObject("midterm_score"));
                    r.setFin((BigDecimal) rs.getObject("final_score"));
                    r.setTotal((BigDecimal) rs.getObject("total_score"));
                    r.setFinalized(rs.getBoolean("is_finalized"));
                    out.add(r);
                }
            }
        }
        return out;
    }

    // ✅ chỉ update ô NULL + chưa chốt (không overwrite)
    public void saveDraft(int enrollId, BigDecimal mid, BigDecimal fin, BigDecimal total) throws SQLException {
        String sql = """
        UPDATE qlsv.enrollments
        SET
          midterm_score = ?,
          final_score   = ?,
          total_score   = ?,
          updated_at    = NOW()
        WHERE enroll_id = ?
          AND is_finalized = FALSE
    """;
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, mid, Types.NUMERIC);
            ps.setObject(2, fin, Types.NUMERIC);
            ps.setObject(3, total, Types.NUMERIC);
            ps.setInt(4, enrollId);
            ps.executeUpdate();
        }
    }


    public void finalizeClass(String lecturerId, String classId) throws SQLException {
        String sql = """
            UPDATE qlsv.enrollments e
            SET is_finalized = TRUE,
                updated_at = NOW()
            WHERE e.class_id = ?
              AND e.is_finalized = FALSE
              AND EXISTS (
                SELECT 1 FROM qlsv.teaching_assignments ta
                WHERE ta.class_id = e.class_id AND ta.lecturer_id = ?
              )
        """;
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, classId);
            ps.setString(2, lecturerId);
            ps.executeUpdate();
        }
    }
}
