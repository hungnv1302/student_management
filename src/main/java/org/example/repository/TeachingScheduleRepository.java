package org.example.repository;

import org.example.config.DbConfig;
import org.example.dto.AssignedClassDto;
import org.example.dto.StudentInClassDto;

import java.sql.*;
import java.util.*;

public class TeachingScheduleRepository {

    public record Term(int termYear, int termSem) {}

    public Optional<String> resolveLecturerId(String login) throws SQLException {
        if (login == null || login.isBlank()) return Optional.empty();
        String v = login.trim();

        // login is lecturer_id?
        String sql1 = "SELECT lecturer_id FROM qlsv.lecturers WHERE lecturer_id = ?";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql1)) {
            ps.setString(1, v);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(rs.getString("lecturer_id"));
            }
        }

        // login is email? (lecturer_id = persons.person_id)
        String sql2 = """
            SELECT l.lecturer_id
            FROM qlsv.lecturers l
            JOIN qlsv.persons p ON p.person_id = l.lecturer_id
            WHERE LOWER(p.email) = LOWER(?)
            LIMIT 1
        """;
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql2)) {
            ps.setString(1, v);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(rs.getString("lecturer_id"));
            }
        }

        return Optional.empty();
    }

    /** Lấy danh sách các term mà giảng viên có lớp (để fill filter) */
    public List<Term> findTermsOfLecturer(String lecturerId) throws SQLException {
        String sql = """
            SELECT DISTINCT term_year, term_sem
            FROM qlsv.sections
            WHERE lecturer_id = ?
            ORDER BY term_year DESC, term_sem DESC
        """;
        List<Term> out = new ArrayList<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Term(rs.getInt("term_year"), rs.getInt("term_sem")));
                }
            }
        }
        return out;
    }

    /** Lấy lớp theo term (year/sem) */
    public List<AssignedClassDto> findAssignedClassesByLecturerInTerm(
            String lecturerId, int termYear, int termSem) throws SQLException {

        String sql = """
            SELECT
                sec.class_id,
                sub.subject_name,
                COALESCE(COUNT(DISTINCT e.enroll_id), 0) AS student_count,
                COALESCE(
                    STRING_AGG(
                        (CASE WHEN ts.day_of_week = 8 THEN 'CN' ELSE 'T' || ts.day_of_week::text END)
                        || ' ' || TO_CHAR(ts.start_time, 'HH24:MI')
                        || '-' || TO_CHAR(ts.end_time, 'HH24:MI')
                        || COALESCE(' (' || COALESCE(ts.room, sec.room) || ')', ''),
                        '; ' ORDER BY ts.day_of_week, ts.start_time
                    ),
                    'Chưa có lịch'
                ) AS time_info
            FROM qlsv.sections sec
            JOIN qlsv.subjects sub ON sub.subject_id = sec.subject_id
            LEFT JOIN qlsv.enrollments e ON e.class_id = sec.class_id
            LEFT JOIN qlsv.time_slots ts ON ts.class_id = sec.class_id
            WHERE sec.lecturer_id = ?
              AND sec.term_year = ?
              AND sec.term_sem  = ?
            GROUP BY sec.class_id, sub.subject_name
            ORDER BY sec.class_id
        """;

        List<AssignedClassDto> out = new ArrayList<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, lecturerId);
            ps.setInt(2, termYear);
            ps.setInt(3, termSem);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new AssignedClassDto(
                            rs.getString("class_id"),
                            rs.getString("subject_name"),
                            rs.getInt("student_count"),
                            rs.getString("time_info")
                    ));
                }
            }
        }
        return out;
    }

    /** Lấy tất cả lớp (không lọc term) */
    public List<AssignedClassDto> findAssignedClassesByLecturerAllTerms(String lecturerId) throws SQLException {
        String sql = """
            SELECT
                sec.class_id,
                sub.subject_name,
                COALESCE(COUNT(DISTINCT e.enroll_id), 0) AS student_count,
                (sec.term_year::text || '.' || sec.term_sem::text) AS time_info
            FROM qlsv.sections sec
            JOIN qlsv.subjects sub ON sub.subject_id = sec.subject_id
            LEFT JOIN qlsv.enrollments e ON e.class_id = sec.class_id
            WHERE sec.lecturer_id = ?
            GROUP BY sec.class_id, sub.subject_name, sec.term_year, sec.term_sem
            ORDER BY sec.term_year DESC, sec.term_sem DESC, sec.class_id
        """;
        List<AssignedClassDto> out = new ArrayList<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new AssignedClassDto(
                            rs.getString("class_id"),
                            rs.getString("subject_name"),
                            rs.getInt("student_count"),
                            rs.getString("time_info") // ở chế độ allTerms: hiển thị "2024.2"
                    ));
                }
            }
        }
        return out;
    }

    public List<StudentInClassDto> findStudentsInClass(String classId) throws SQLException {
        String sql = """
            SELECT st.student_id, p.full_name, p.email
            FROM qlsv.enrollments e
            JOIN qlsv.students st ON st.student_id = e.student_id
            JOIN qlsv.persons  p  ON p.person_id  = st.student_id
            WHERE e.class_id = ?
            ORDER BY st.student_id
        """;

        List<StudentInClassDto> out = new ArrayList<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new StudentInClassDto(
                            rs.getString("student_id"),
                            rs.getString("full_name"),
                            rs.getString("email")
                    ));
                }
            }
        }
        return out;
    }
}
