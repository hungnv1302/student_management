package org.example.repository;

import java.sql.SQLException;

public class ScoreReviewRequestRepository {

    public int createRequest(String studentId, int enrollId, String reason) throws SQLException {
        String sql = """
            INSERT INTO qlsv.score_review_requests(enroll_id, student_id, lecturer_id, reason, status, old_total)
            SELECT
                e.enroll_id,
                e.student_id,
                s.lecturer_id,
                ?,
                'PENDING',
                e.total_score
            FROM qlsv.enrollments e
            JOIN qlsv.sections s ON s.class_id = e.class_id
            WHERE e.enroll_id = ?
              AND e.student_id = ?
            RETURNING request_id
        """;

        Integer id = DbFn.queryOne(sql, ps -> {
            ps.setString(1, reason);
            ps.setInt(2, enrollId);
            ps.setString(3, studentId);
        }, rs -> rs.getInt("request_id"));

        if (id == null) throw new SQLException("Không tạo được yêu cầu phúc tra.");
        return id;
    }

    public boolean hasPendingRequest(String studentId, int enrollId) throws SQLException {
        String sql = """
            SELECT 1
            FROM qlsv.score_review_requests
            WHERE student_id = ?
              AND enroll_id = ?
              AND status = 'PENDING'
            LIMIT 1
        """;
        Integer v = DbFn.queryOne(sql, ps -> {
            ps.setString(1, studentId);
            ps.setInt(2, enrollId);
        }, rs -> 1);
        return v != null;
    }
}
