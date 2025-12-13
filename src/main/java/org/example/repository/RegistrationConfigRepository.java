package org.example.repository;

import org.example.config.DbConfig;

import java.sql.*;

public class RegistrationConfigRepository {

    public record RegistrationPolicy(boolean isOpen, int maxCredits) {}

    public RegistrationPolicy getPolicy(String semester, int year, Connection c) throws SQLException {
        String sql = """
            SELECT is_open, max_credits
            FROM public.registration_config
            WHERE semester = ? AND year = ?
            """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, semester);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    // không có config thì coi như đóng
                    return new RegistrationPolicy(false, 0);
                }
                return new RegistrationPolicy(rs.getBoolean("is_open"), rs.getInt("max_credits"));
            }
        }
    }
}
