package org.example.repository;

import java.sql.SQLException;

public class ChangePasswordRepository {

    public String getState(String username) throws SQLException {
        String sql = "SELECT state FROM qlsv.users WHERE username = ?";
        return DbFn.queryScalar(sql,
                ps -> ps.setString(1, username),
                rs -> rs.getString("state"));
    }

    public boolean verifyOldPassword(String username, String oldPassword) throws SQLException {
        String sql = """
            SELECT 1
            FROM qlsv.users
            WHERE username = ?
              AND COALESCE(password, '') = COALESCE(?, '')
        """;

        Integer one = DbFn.queryScalar(sql,
                ps -> {
                    ps.setString(1, username);
                    ps.setString(2, oldPassword == null ? "" : oldPassword);
                },
                rs -> 1);

        return one != null;
    }

    public int updatePassword(String username, String newPassword) throws SQLException {
        String sql = """
            UPDATE qlsv.users
            SET password = ?
            WHERE username = ?
        """;

        return DbFn.execUpdate(sql,
                ps -> {
                    ps.setString(1, newPassword);
                    ps.setString(2, username);
                });
    }
}
