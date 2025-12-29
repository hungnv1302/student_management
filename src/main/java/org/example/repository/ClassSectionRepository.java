package org.example.repository;

import org.example.config.DbConfig;
import org.example.domain.ClassSection;
import org.example.domain.Subject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ClassSectionRepository {

    private final SubjectRepository subjectRepo = new SubjectRepository();

    public Optional<ClassSection> findById(String classId) throws SQLException {
        String sql = """
            SELECT class_id, subject_id, term_year, term_sem, capacity, status, room, note
            FROM qlsv.sections
            WHERE class_id = ?
        """;

        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, classId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                ClassSection cs = new ClassSection();
                cs.setClassID(rs.getString("class_id"));
                cs.setTermYear(rs.getInt("term_year"));
                cs.setTermSem(rs.getShort("term_sem"));
                cs.setCapacity(rs.getInt("capacity"));
                cs.setStatus(rs.getString("status"));
                cs.setRoom(rs.getString("room"));
                cs.setNote(rs.getString("note"));

                String subjectId = rs.getString("subject_id");
                Subject subject = subjectRepo.findById(subjectId).orElse(null);
                cs.setSubject(subject);

                return Optional.of(cs);
            }
        }
    }

    /** Đếm tổng số enrollment của lớp (khuyên dùng) */
    public int countEnrolled(String classId) throws SQLException {
        String sql = """
            SELECT COUNT(*)::int AS cnt
            FROM qlsv.enrollments
            WHERE class_id = ?
        """;

        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, classId);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("cnt");
            }
        }
    }
}
