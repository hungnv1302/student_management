package org.example.repository;

import org.example.config.DbConfig;
import org.example.domain.Subject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class SubjectRepository {

    public Optional<Subject> findById(String subjectId) throws SQLException {
        String sql = """
            SELECT subject_id, subject_name, credit, department, is_required
            FROM qlsv.subjects
            WHERE subject_id = ?
        """;

        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, subjectId);

            try (ResultSet rs = ps.executeQuery()) {
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
    }
}
