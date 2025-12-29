package org.example.repository;

import java.sql.SQLException;

public class UserEmailRepository {

    public String getEmailByUsername(String username) throws SQLException {
        String sql = """
            SELECT p.email
            FROM qlsv.users u
            JOIN qlsv.persons p ON p.person_id = u.username
            WHERE u.username = ?
        """;
        return DbFn.queryScalar(sql,
                ps -> ps.setString(1, username),
                rs -> rs.getString("email"));
    }
}