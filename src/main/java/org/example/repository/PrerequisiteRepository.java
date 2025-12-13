package org.example.repository;

import java.sql.*;

public class PrerequisiteRepository {

    /**
     * TRUE nếu sinh viên đã COMPLETED tất cả môn tiên quyết
     */
    public boolean hasAllPrerequisites(String studentId, String classId, Connection c) throws SQLException {

        String sql = """
            WITH target_subject AS (
                SELECT subject_id
                FROM public.class_sections
                WHERE class_id = ?
            ),
            prereq AS (
                SELECT sp.prereq_subject_id
                FROM public.subject_prerequisites sp
                JOIN target_subject ts ON ts.subject_id = sp.subject_id
            ),
            completed AS (
                SELECT DISTINCT cs.subject_id
                FROM public.enrollments e
                JOIN public.class_sections cs ON cs.class_id = e.class_id
                WHERE e.student_id = ?
                  AND e.status = 'COMPLETED'
            )
            SELECT COUNT(*) AS missing
            FROM prereq p
            WHERE p.prereq_subject_id NOT IN (
                SELECT subject_id FROM completed
            )
            """;

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, classId);
            ps.setString(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("missing") == 0;
            }
        }
    }
}
