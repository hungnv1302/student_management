package org.example.repository;

import org.example.config.DbConfig;
import org.example.dto.StudentScoreRow;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LecturerScoreRepository {

    /** check giảng viên có được phân công lớp không (dựa teaching_assignments) */
    public boolean isAssigned(String lecturerId, String classId) throws SQLException {
        String sql = """
            SELECT 1
            FROM qlsv.teaching_assignments
            WHERE lecturer_id = ?
              AND class_id = ?
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

    /** load danh sách SV của lớp + tên từ persons.full_name */
    public List<StudentScoreRow> findByClass(String lecturerId, String classId) throws SQLException {
        String sql = """
        SELECT
            e.enroll_id,
            e.student_id,
            p.full_name,
            e.midterm_score,
            e.final_score,
            e.total_score,
            e.status,
            e.is_finalized
        FROM qlsv.enrollments e
        JOIN qlsv.persons p ON p.person_id = e.student_id
        WHERE e.class_id = ?
        ORDER BY e.student_id
    """;

        List<StudentScoreRow> out = new ArrayList<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, classId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StudentScoreRow r = new StudentScoreRow();
                    r.setEnrollId(rs.getInt("enroll_id"));
                    r.setStudentId(rs.getString("student_id"));
                    r.setFullName(rs.getString("full_name"));
                    r.setMidtermScore((BigDecimal) rs.getObject("midterm_score"));
                    r.setFinalScore((BigDecimal) rs.getObject("final_score"));
                    r.setTotalScore((BigDecimal) rs.getObject("total_score"));
                    r.setStatus(rs.getString("status"));
                    r.setFinalized(rs.getBoolean("is_finalized"));
                    out.add(r);
                }
            }
        }
        return out;
    }


    /**
     * Lưu điểm: chỉ ghi vào ô NULL, và chỉ khi chưa chốt.
     * (COALESCE giúp không overwrite điểm đã có)
     */
    public int updateScoresOnlyIfNull(int enrollId,
                                      BigDecimal midterm,
                                      BigDecimal fin,
                                      BigDecimal total) throws SQLException {
        String sql = """
            UPDATE qlsv.enrollments
            SET
              midterm_score = COALESCE(midterm_score, ?),
              final_score   = COALESCE(final_score, ?),
              total_score   = COALESCE(total_score, ?),
              updated_at    = NOW()
            WHERE enroll_id = ?
              AND is_finalized = FALSE
        """;
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, midterm);
            ps.setObject(2, fin);
            ps.setObject(3, total);
            ps.setInt(4, enrollId);
            return ps.executeUpdate();
        }
    }

    /** chốt lớp: khóa tất cả enrollments của class */
    public int finalizeClass(String classId) throws SQLException {
        String sql = """
            UPDATE qlsv.enrollments
            SET is_finalized = TRUE,
                updated_at = NOW()
            WHERE class_id = ?
              AND is_finalized = FALSE
        """;
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, classId);
            return ps.executeUpdate();
        }
    }
}
