package org.example.repository;

import java.sql.SQLException;

public class RegistrationConfigRepository {

    /** SELECT qlsv.get_open_term() */
    public Short getOpenTerm() throws SQLException {
        String sql = "SELECT qlsv.get_open_term() AS term_no";

        // queryOne trả về 1 object; ở đây map ra Short
        return DbFn.queryOne(sql, null, rs -> {
            short v = rs.getShort("term_no");   // ResultSet method
            return rs.wasNull() ? null : v;     // ResultSet method
        });
    }
}
