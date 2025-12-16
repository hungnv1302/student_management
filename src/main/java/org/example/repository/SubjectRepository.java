package org.example.repository;

import org.example.config.DbConfig;
import org.example.domain.Subject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubjectRepository {

    public Optional<Subject> findById(String subjectId) throws SQLException {
        String sql = """
            SELECT subject_id, subject_name, credit, department, is_required
            FROM subjects
            WHERE subject_id = ?
        """;
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, subjectId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Optional.empty();

            Subject s = new Subject(
                    rs.getString("subject_id"),
                    rs.getString("subject_name"),
                    rs.getInt("credit"),
                    null,
                    rs.getString("department"),
                    rs.getBoolean("is_required")
            );
            return Optional.of(s);
        }
    }

    public List<String> findPrereqSubjectIds(String subjectId) throws SQLException {
        String sql = "SELECT prereq_subject_id FROM subject_prerequisites WHERE subject_id = ?";
        List<String> ids = new ArrayList<>();

        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, subjectId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getString(1));
        }
        return ids;
    }
}
