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

        // 1) login is lecturer_id?
        String sql1 = "SELECT lecturer_id FROM qlsv.lecturers WHERE lecturer_id = ?";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql1)) {
            ps.setString(1, v);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(rs.getString(1));
            }
        }

        // 2) login is email?
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
                if (rs.next()) return Optional.of(rs.getString(1));
            }
        }

        // 3) login is username?
        String sql3 = """
            SELECT l.lecturer_id
            FROM qlsv.users u
            JOIN qlsv.lecturers l ON l.lecturer_id = u.person_id
            WHERE u.username = ?
            LIMIT 1
        """;
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql3)) {
            ps.setString(1, v);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(rs.getString(1));
            }
        }

        return Optional.empty();
    }

    /** Lấy danh sách term - SỬA: lấy cả từ sections.lecturer_id */
    public List<Term> findTermsOfLecturer(String lecturerId) throws SQLException {
        String sql = """
            SELECT DISTINCT sec.term_year, sec.term_sem
            FROM qlsv.sections sec
            LEFT JOIN qlsv.teaching_assignments ta 
                ON ta.class_id = sec.class_id AND COALESCE(ta.role, 'MAIN') = 'MAIN'
            WHERE (ta.lecturer_id = ? OR sec.lecturer_id = ?)
            ORDER BY sec.term_year DESC, sec.term_sem DESC
        """;

        List<Term> out = new ArrayList<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, lecturerId);
            ps.setString(2, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Term(rs.getInt("term_year"), rs.getInt("term_sem")));
                }
            }
        }
        return out;
    }

    /** Lấy lớp theo term - SỬA: lấy cả từ sections.lecturer_id */
    public List<AssignedClassDto> findAssignedClassesByLecturerInTerm(
            String lecturerId, int termYear, int termSem) throws SQLException {

        String sql = """
            WITH student_cnt AS (
                SELECT class_id, COUNT(DISTINCT student_id) AS student_count
                FROM qlsv.enrollments
                GROUP BY class_id
            ),
            time_info AS (
                SELECT
                    ts.class_id,
                    STRING_AGG(
                        (CASE WHEN ts.day_of_week = 8 THEN 'CN' ELSE 'T' || ts.day_of_week::text END)
                        || ' ' || TO_CHAR(ts.start_time, 'HH24:MI')
                        || '-' || TO_CHAR(ts.end_time, 'HH24:MI')
                        || COALESCE(' (' || COALESCE(ts.room, sec.room) || ')', ''),
                        '; ' ORDER BY ts.day_of_week, ts.start_time
                    ) AS time_info
                FROM qlsv.time_slots ts
                JOIN qlsv.sections sec ON sec.class_id = ts.class_id
                GROUP BY ts.class_id
            )
            SELECT
                sec.class_id,
                sub.subject_name,
                COALESCE(sc.student_count, 0) AS student_count,
                COALESCE(ti.time_info, 'Chưa có lịch') AS time_info
            FROM qlsv.sections sec
            JOIN qlsv.subjects sub ON sub.subject_id = sec.subject_id
            LEFT JOIN qlsv.teaching_assignments ta 
                ON ta.class_id = sec.class_id AND COALESCE(ta.role, 'MAIN') = 'MAIN'
            LEFT JOIN student_cnt sc ON sc.class_id = sec.class_id
            LEFT JOIN time_info  ti ON ti.class_id = sec.class_id
            WHERE (ta.lecturer_id = ? OR sec.lecturer_id = ?)
              AND sec.term_year = ?
              AND sec.term_sem  = ?
            ORDER BY sec.class_id
        """;

        List<AssignedClassDto> out = new ArrayList<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, lecturerId);
            ps.setString(2, lecturerId);
            ps.setInt(3, termYear);
            ps.setInt(4, termSem);

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

    /** Lấy tất cả lớp - SỬA: lấy cả từ sections.lecturer_id */
    public List<AssignedClassDto> findAssignedClassesByLecturerAllTerms(String lecturerId) throws SQLException {
        String sql = """
            SELECT
                sec.class_id,
                sub.subject_name,
                COALESCE(COUNT(DISTINCT e.student_id), 0) AS student_count,
                (sec.term_year::text || '.' || sec.term_sem::text) AS time_info
            FROM qlsv.sections sec
            JOIN qlsv.subjects sub ON sub.subject_id = sec.subject_id
            LEFT JOIN qlsv.teaching_assignments ta 
                ON ta.class_id = sec.class_id AND COALESCE(ta.role, 'MAIN') = 'MAIN'
            LEFT JOIN qlsv.enrollments e ON e.class_id = sec.class_id
            WHERE (ta.lecturer_id = ? OR sec.lecturer_id = ?)
            GROUP BY sec.class_id, sub.subject_name, sec.term_year, sec.term_sem
            ORDER BY sec.term_year DESC, sec.term_sem DESC, sec.class_id
        """;

        List<AssignedClassDto> out = new ArrayList<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, lecturerId);
            ps.setString(2, lecturerId);

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

    /** Danh sách SV trong lớp */
    public List<StudentInClassDto> findStudentsInClass(String classId) throws SQLException {
        String sql = """
            SELECT DISTINCT st.student_id, p.full_name, p.email
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