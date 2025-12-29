package org.example.repository;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class PasswordOtpRepository {

    public void upsertOtp(String username, String otp, LocalDateTime expiresAt) throws SQLException {
        String sql = """
            INSERT INTO qlsv.password_otp(username, otp_code, expires_at)
            VALUES (?, ?, ?)
            ON CONFLICT (username)
            DO UPDATE SET otp_code = EXCLUDED.otp_code, expires_at = EXCLUDED.expires_at
        """;
        DbFn.exec(sql, ps -> {
            ps.setString(1, username);
            ps.setString(2, otp);
            ps.setTimestamp(3, Timestamp.valueOf(expiresAt));
        });
    }

    public OtpRow getOtp(String username) throws SQLException {
        String sql = "SELECT otp_code, expires_at FROM qlsv.password_otp WHERE username = ?";
        return DbFn.queryOne(sql,
                ps -> ps.setString(1, username),
                rs -> new OtpRow(rs.getString("otp_code"),
                        rs.getTimestamp("expires_at").toLocalDateTime())
        );
    }

    public void deleteOtp(String username) throws SQLException {
        String sql = "DELETE FROM qlsv.password_otp WHERE username = ?";
        DbFn.exec(sql, ps -> ps.setString(1, username));
    }

    public record OtpRow(String otp, LocalDateTime expiresAt) {}
}
