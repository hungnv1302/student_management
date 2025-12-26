package org.example.repository;

import org.example.config.DbConfig;
import org.example.domain.ClassSection;
import org.example.domain.Subject;

import java.sql.*;
import java.util.Optional;

public class ClassSectionRepository {

    private final SubjectRepository subjectRepo = new SubjectRepository();

    public Optional<ClassSection> findById(String classId) throws SQLException {
        String sql = """
            SELECT class_id, subject_id, term_no, capacity, status, room, note
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
                cs.setTermNo(rs.getShort("term_no"));
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

    /** Đếm SV đang học (IN_PROGRESS) giống rule trước của cháu */
    public int countEnrolledInProgress(String classId) throws SQLException {
        String sql = """
            SELECT COUNT(*) AS cnt
            FROM qlsv.enrollments
            WHERE class_id = ?
              AND status = 'IN_PROGRESS'
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
