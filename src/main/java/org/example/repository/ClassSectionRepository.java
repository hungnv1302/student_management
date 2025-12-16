package org.example.repository;

import org.example.config.DbConfig;
import org.example.domain.ClassSection;
import org.example.domain.Subject;

import java.sql.*;
import java.util.Optional;

public class ClassSectionRepository {

    private final SubjectRepository subjectRepo = new SubjectRepository();

    public Optional<ClassSection> findById(String classId) throws SQLException {
        String sql = "SELECT * FROM class_sections WHERE class_id = ?";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, classId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Optional.empty();

            ClassSection cs = new ClassSection();
            cs.setClassID(rs.getString("class_id"));
            cs.setSemester(rs.getString("semester"));
            cs.setYear(rs.getInt("year"));
            cs.setCapacity(rs.getInt("capacity"));
            cs.setRoom(rs.getString("room"));

            // gắn Subject object vào ClassSection (domain cháu có Subject)
            String subjectId = rs.getString("subject_id");
            Subject subject = subjectRepo.findById(subjectId).orElse(null);
            cs.setSubject(subject);

            return Optional.of(cs);
        }
    }

    public int countEnrolled(String classId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE class_id = ? AND status = 'ENROLLED'";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, classId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }
}
