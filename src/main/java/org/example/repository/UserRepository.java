package org.example.repository;

import org.example.config.DbConfig;
import org.example.dto.UserDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class UserRepository {

    public Optional<UserDTO> findByUsername(String username) {
        String sql = """
            SELECT username, password, role, state
            FROM qlsv.users
            WHERE username = ?
        """;

        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                return Optional.of(new UserDTO(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("state")
                ));
            }

        } catch (Exception e) {
            throw new RuntimeException("findByUsername failed", e);
        }
    }

    // Vì PK là username, hàm này chỉ gọi lại findByUsername cho tương thích AuthService cũ
    public Optional<UserDTO> findByUserId(String userId) {
        return findByUsername(userId);
    }

    public void updateLastLogin(String username) {
        String sql = "UPDATE qlsv.users SET last_login = NOW() WHERE username = ?";

        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.executeUpdate();

        } catch (Exception ignored) {}
    }

    public void updatePassword(String username, String newPassword) {
        String sql = "UPDATE qlsv.users SET password = ? WHERE username = ?";

        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, newPassword);
            ps.setString(2, username);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("updatePassword failed", e);
        }
    }
}
