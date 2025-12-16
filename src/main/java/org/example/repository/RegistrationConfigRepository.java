package org.example.repository;

import org.example.config.DbConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class RegistrationConfigRepository {

    public record ConfigDTO(
            String semester,
            int year,
            boolean isOpen,
            int maxCredits,
            LocalDateTime openAt,
            LocalDateTime closeAt
    ) {}

    public Optional<ConfigDTO> findBySemesterYear(String semester, int year) throws SQLException {
        String sql = "SELECT * FROM registration_config WHERE semester = ? AND year = ?";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, semester);
            ps.setInt(2, year);

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Optional.empty();

            Timestamp openAt = rs.getTimestamp("open_at");
            Timestamp closeAt = rs.getTimestamp("close_at");

            return Optional.of(new ConfigDTO(
                    rs.getString("semester"),
                    rs.getInt("year"),
                    rs.getBoolean("is_open"),
                    rs.getInt("max_credits"),
                    openAt == null ? null : openAt.toLocalDateTime(),
                    closeAt == null ? null : closeAt.toLocalDateTime()
            ));
        }
    }
}
