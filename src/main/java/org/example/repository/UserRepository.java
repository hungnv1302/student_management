package org.example.repository;

import org.example.config.DbConfig;

import java.sql.*;
import java.util.Optional;

public class UserRepository {

    public record UserRow(
            String userId,
            String username,
            String passwordHash,
            String passwordSalt,
            String role,
            String state,
            Timestamp lastLogin
    ) {}

    public Optional<UserRow> findByUsername(String username) throws SQLException {
        String sql = """
            SELECT user_id, username, password_hash, password_salt, role, state, last_login
            FROM public.users
            WHERE username = ?
        """;
        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new UserRow(
                        rs.getString("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("password_salt"),
                        rs.getString("role"),
                        rs.getString("state"),
                        rs.getTimestamp("last_login")
                ));
            }
        }
    }

    public boolean updateLastLogin(String userId) throws SQLException {
        String sql = "UPDATE public.users SET last_login = NOW() WHERE user_id = ?";
        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, userId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean updatePassword(String userId, String newHash, String newSalt) throws SQLException {
        String sql = "UPDATE public.users SET password_hash=?, password_salt=? WHERE user_id=?";
        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setString(2, newSalt);
            ps.setString(3, userId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean insertUser(String userId, String username, String hash, String salt, String role) throws SQLException {
        String sql = """
        INSERT INTO public.users(user_id, username, password_hash, password_salt, role, state)
        VALUES (?, ?, ?, ?, ?, 'ACTIVE')
        ON CONFLICT (username) DO NOTHING
    """;
        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, username);
            ps.setString(3, hash);
            ps.setString(4, salt);
            ps.setString(5, role);
            return ps.executeUpdate() == 1;
        }
    }
}
