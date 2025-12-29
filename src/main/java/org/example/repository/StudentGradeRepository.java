package org.example.repository;

import org.example.dto.GradeRow;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class StudentGradeRepository {

    public List<GradeRow> getStudentGrades(String studentId) {
        if (studentId == null || studentId.isBlank()) return Collections.emptyList();

        String sql = """
            SELECT
                e.enroll_id,
                sec.term_year, sec.term_sem,
                e.class_id,
                sec.subject_id,
                sub.subject_name,
                sub.credit,
                e.midterm_score,
                e.final_score,
                e.total_score,
                e.is_finalized,
                gs.letter,
                gs.point4
            FROM qlsv.enrollments e
            JOIN qlsv.sections sec ON sec.class_id = e.class_id
            JOIN qlsv.subjects sub ON sub.subject_id = sec.subject_id
            LEFT JOIN qlsv.grade_scale gs
                   ON e.total_score IS NOT NULL
                  AND e.total_score <@ gs.score_range
            WHERE e.student_id = ?
              AND e.status <> 'DROPPED'
            ORDER BY sec.term_year, sec.term_sem, e.class_id
        """;

        try {
            return DbFn.queryList(sql,
                    ps -> ps.setString(1, studentId),
                    rs -> {
                        GradeRow r = new GradeRow(
                                rs.getInt("term_year"),
                                rs.getShort("term_sem"),
                                rs.getString("class_id"),
                                rs.getString("subject_id"),
                                rs.getString("subject_name"),
                                rs.getObject("credit", Integer.class),
                                rs.getBigDecimal("midterm_score"),
                                rs.getBigDecimal("final_score"),
                                rs.getBigDecimal("total_score"),
                                rs.getString("letter"),
                                rs.getBigDecimal("point4"),
                                rs.getObject("is_finalized", Boolean.class)
                        );
                        r.setEnrollId(rs.getObject("enroll_id", Integer.class));
                        return r;
                    }
            );
        } catch (SQLException e) {
            throw new RuntimeException("Load grades failed: " + e.getMessage(), e);
        }
    }
}
