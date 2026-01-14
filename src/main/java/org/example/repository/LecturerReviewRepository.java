package org.example.repository;

import org.example.config.DbConfig;
import org.example.dto.LecturerReviewDTO;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LecturerReviewRepository {

    /**
     * Lấy danh sách yêu cầu phúc tra của giảng viên
     */
    public List<LecturerReviewDTO> getReviewRequests(String lecturerId, String filterStatus) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT
              srr.request_id,
              srr.enroll_id,
              srr.student_id,
              p.full_name AS student_name,
              e.class_id,
              s.subject_name,
              srr.reason,
              srr.status,
              srr.created_at,
              srr.old_total,
              srr.new_total,
              srr.note
            FROM qlsv.score_review_requests srr
            JOIN qlsv.enrollments e ON e.enroll_id = srr.enroll_id
            JOIN qlsv.sections sec ON sec.class_id = e.class_id
            JOIN qlsv.subjects s ON s.subject_id = sec.subject_id
            JOIN qlsv.persons p ON p.person_id = srr.student_id
            WHERE srr.lecturer_id = ?
        """);

        // Lọc theo trạng thái nếu có
        if (filterStatus != null && !filterStatus.isEmpty() && !"ALL".equals(filterStatus)) {
            sql.append(" AND srr.status = ?");
        }

        sql.append(" ORDER BY srr.created_at DESC");

        List<LecturerReviewDTO> result = new ArrayList<>();

        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {

            ps.setString(1, lecturerId);

            if (filterStatus != null && !filterStatus.isEmpty() && !"ALL".equals(filterStatus)) {
                ps.setString(2, filterStatus);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LecturerReviewDTO dto = new LecturerReviewDTO();
                    dto.setRequestId(rs.getInt("request_id"));
                    dto.setEnrollId(rs.getInt("enroll_id"));
                    dto.setStudentId(rs.getString("student_id"));
                    dto.setStudentName(rs.getString("student_name"));
                    dto.setClassId(rs.getString("class_id"));
                    dto.setSubjectName(rs.getString("subject_name"));
                    dto.setReason(rs.getString("reason"));
                    dto.setStatus(rs.getString("status"));
                    dto.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    dto.setOldTotal((BigDecimal) rs.getObject("old_total"));
                    dto.setNewTotal((BigDecimal) rs.getObject("new_total"));
                    dto.setNote(rs.getString("note"));
                    result.add(dto);
                }
            }
        }

        return result;
    }

    /**
     * Lấy thông tin chi tiết 1 yêu cầu phúc tra
     */
    public LecturerReviewDTO getRequestDetail(int requestId, String lecturerId) throws SQLException {
        String sql = """
            SELECT
              srr.request_id,
              srr.enroll_id,
              srr.student_id,
              p.full_name AS student_name,
              e.class_id,
              s.subject_name,
              e.midterm_score,
              e.final_score,
              srr.reason,
              srr.status,
              srr.created_at,
              srr.old_total,
              srr.new_total,
              srr.note
            FROM qlsv.score_review_requests srr
            JOIN qlsv.enrollments e ON e.enroll_id = srr.enroll_id
            JOIN qlsv.sections sec ON sec.class_id = e.class_id
            JOIN qlsv.subjects s ON s.subject_id = sec.subject_id
            JOIN qlsv.persons p ON p.person_id = srr.student_id
            WHERE srr.request_id = ? AND srr.lecturer_id = ?
        """;

        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, requestId);
            ps.setString(2, lecturerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LecturerReviewDTO dto = new LecturerReviewDTO();
                    dto.setRequestId(rs.getInt("request_id"));
                    dto.setEnrollId(rs.getInt("enroll_id"));
                    dto.setStudentId(rs.getString("student_id"));
                    dto.setStudentName(rs.getString("student_name"));
                    dto.setClassId(rs.getString("class_id"));
                    dto.setSubjectName(rs.getString("subject_name"));
                    dto.setReason(rs.getString("reason"));
                    dto.setStatus(rs.getString("status"));
                    dto.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    dto.setOldTotal((BigDecimal) rs.getObject("old_total"));
                    dto.setNewTotal((BigDecimal) rs.getObject("new_total"));
                    dto.setNote(rs.getString("note"));
                    return dto;
                }
            }
        }

        return null;
    }

    /**
     * Chấp nhận yêu cầu phúc tra và cập nhật điểm
     */
    public void approveRequest(int requestId, BigDecimal newTotal, String note) throws SQLException {
        Connection c = null;
        try {
            c = DbConfig.getConnection();
            c.setAutoCommit(false);

            // 1. Lấy enroll_id
            int enrollId;
            String getEnrollSql = "SELECT enroll_id FROM qlsv.score_review_requests WHERE request_id = ?";
            try (PreparedStatement ps = c.prepareStatement(getEnrollSql)) {
                ps.setInt(1, requestId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Không tìm thấy yêu cầu phúc tra");
                    }
                    enrollId = rs.getInt("enroll_id");
                }
            }

            // 2. Cập nhật điểm trong enrollments
            String updateScoreSql = """
                UPDATE qlsv.enrollments
                SET total_score = ?,
                    updated_at = NOW()
                WHERE enroll_id = ?
            """;
            try (PreparedStatement ps = c.prepareStatement(updateScoreSql)) {
                ps.setBigDecimal(1, newTotal);
                ps.setInt(2, enrollId);
                ps.executeUpdate();
            }

            // 3. Cập nhật trạng thái yêu cầu phúc tra
            String updateRequestSql = """
                UPDATE qlsv.score_review_requests
                SET status = 'APPROVED',
                    new_total = ?,
                    note = ?,
                    handled_at = NOW()
                WHERE request_id = ?
            """;
            try (PreparedStatement ps = c.prepareStatement(updateRequestSql)) {
                ps.setBigDecimal(1, newTotal);
                ps.setString(2, note);
                ps.setInt(3, requestId);
                ps.executeUpdate();
            }

            c.commit();

        } catch (SQLException e) {
            if (c != null) {
                c.rollback();
            }
            throw e;
        } finally {
            if (c != null) {
                c.setAutoCommit(true);
                c.close();
            }
        }
    }

    /**
     * Từ chối yêu cầu phúc tra
     */
    public void rejectRequest(int requestId, String note) throws SQLException {
        String sql = """
            UPDATE qlsv.score_review_requests
            SET status = 'REJECTED',
                note = ?,
                handled_at = NOW()
            WHERE request_id = ?
        """;

        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, note);
            ps.setInt(2, requestId);

            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new SQLException("Không tìm thấy yêu cầu phúc tra hoặc đã được xử lý");
            }
        }
    }

    /**
     * Lấy điểm chi tiết của enrollment
     */
    public EnrollmentScore getEnrollmentScore(int enrollId) throws SQLException {
        String sql = """
            SELECT midterm_score, final_score, total_score
            FROM qlsv.enrollments
            WHERE enroll_id = ?
        """;

        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, enrollId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new EnrollmentScore(
                            (BigDecimal) rs.getObject("midterm_score"),
                            (BigDecimal) rs.getObject("final_score"),
                            (BigDecimal) rs.getObject("total_score")
                    );
                }
            }
        }

        return null;
    }

    // Inner class để trả về điểm
    public static class EnrollmentScore {
        public final BigDecimal midterm;
        public final BigDecimal finalScore;
        public final BigDecimal total;

        public EnrollmentScore(BigDecimal midterm, BigDecimal finalScore, BigDecimal total) {
            this.midterm = midterm;
            this.finalScore = finalScore;
            this.total = total;
        }
    }
}