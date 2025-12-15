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
            SELECT user_id, username, password, role, state, person_id
            FROM public.users
            WHERE username = ?
        """;

        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                return Optional.of(new UserDTO(
                        rs.getString("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("state"),
                        rs.getString("person_id")
                ));
            }

        } catch (Exception e) {
            throw new RuntimeException("findByUsername failed", e);
        }
    }

    public Optional<UserDTO> findByUserId(String userId) {
        String sql = """
            SELECT user_id, username, password, role, state, person_id
            FROM public.users
            WHERE user_id = ?
        """;

        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                return Optional.of(new UserDTO(
                        rs.getString("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("state"),
                        rs.getString("person_id")
                ));
            }

        } catch (Exception e) {
            throw new RuntimeException("findByUserId failed", e);
        }
    }

    public void updateLastLogin(String userId) {
        String sql = "UPDATE public.users SET last_login = NOW() WHERE user_id = ?";

        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.executeUpdate();

        } catch (Exception ignored) {}
    }

    public void updatePassword(String userId, String newPassword) {
        String sql = "UPDATE public.users SET password = ? WHERE user_id = ?";

        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, newPassword);
            ps.setString(2, userId);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("updatePassword failed", e);
        }
    }
}
