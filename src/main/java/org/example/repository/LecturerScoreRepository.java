package org.example.repository;

import org.example.config.DbConfig;
import org.example.dto.GradeRowDto;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LecturerScoreRepository {

    /** Kiểm tra giảng viên có được phân công lớp này không - SỬA: lấy cả từ sections.lecturer_id */
    public boolean isAssigned(String lecturerId, String classId) throws SQLException {
        String sql = """
            SELECT 1
            FROM qlsv.sections sec
            LEFT JOIN qlsv.teaching_assignments ta 
                ON ta.class_id = sec.class_id AND COALESCE(ta.role, 'MAIN') = 'MAIN'
            WHERE sec.class_id = ?
              AND (ta.lecturer_id = ? OR sec.lecturer_id = ?)
            LIMIT 1
        """;
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, classId);
            ps.setString(2, lecturerId);
            ps.setString(3, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Load danh sách điểm - SỬA: lấy cả từ sections.lecturer_id */
    public List<GradeRowDto> loadRows(String lecturerId, String classId) throws SQLException {
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
            JOIN qlsv.sections sec ON sec.class_id = e.class_id
            LEFT JOIN qlsv.teaching_assignments ta 
              ON ta.class_id = e.class_id AND COALESCE(ta.role, 'MAIN') = 'MAIN'
            WHERE e.class_id = ?
              AND (ta.lecturer_id = ? OR sec.lecturer_id = ?)
            ORDER BY e.student_id
        """;

        List<GradeRowDto> out = new ArrayList<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, classId);
            ps.setString(2, lecturerId);
            ps.setString(3, lecturerId);

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

    /** Lưu điểm (chỉ lưu midterm và final, KHÔNG tính total) */
    public void saveDraft(int enrollId, BigDecimal mid, BigDecimal fin) throws SQLException {
        String sql = """
            UPDATE qlsv.enrollments
            SET
              midterm_score = ?,
              final_score   = ?,
              updated_at    = NOW()
            WHERE enroll_id = ?
              AND is_finalized = FALSE
        """;
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, mid, Types.NUMERIC);
            ps.setObject(2, fin, Types.NUMERIC);
            ps.setInt(3, enrollId);
            ps.executeUpdate();
        }
    }

    /** Tính điểm học phần - SỬA: kiểm tra cả sections.lecturer_id */
    public int calculateFinalGrades(String lecturerId, String classId) throws SQLException {
        String sql = """
            UPDATE qlsv.enrollments e
            SET 
              total_score = (midterm_score * 0.5 + final_score * 0.5),
              is_finalized = TRUE,
              updated_at = NOW()
            WHERE e.class_id = ?
              AND e.is_finalized = FALSE
              AND e.midterm_score IS NOT NULL
              AND e.final_score IS NOT NULL
              AND EXISTS (
                SELECT 1 
                FROM qlsv.sections sec
                LEFT JOIN qlsv.teaching_assignments ta 
                  ON ta.class_id = sec.class_id AND COALESCE(ta.role, 'MAIN') = 'MAIN'
                WHERE sec.class_id = e.class_id 
                  AND (ta.lecturer_id = ? OR sec.lecturer_id = ?)
              )
        """;
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, classId);
            ps.setString(2, lecturerId);
            ps.setString(3, lecturerId);
            return ps.executeUpdate();
        }
    }
}