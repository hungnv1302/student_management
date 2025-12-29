package org.example.repository;

import org.example.dto.TermDTO;

import java.sql.SQLException;

public class RegistrationConfigRepository {

    public TermDTO getOpenTerm() throws SQLException {
        String sql = "SELECT term_year, term_sem FROM qlsv.registration_config WHERE id=1";
        return DbFn.queryOne(sql, null, rs -> {
            TermDTO t = new TermDTO();
            t.setTermYear(rs.getInt("term_year"));
            t.setTermSem(rs.getShort("term_sem"));
            return t;
        });
    }

    public TermDTO getCurrentTerm() throws SQLException {
        // current term = kỳ trước của open term (đúng như function get_current_term của bạn)
        TermDTO open = getOpenTerm();
        int y = open.getTermYear();
        short s = open.getTermSem();
        TermDTO cur = new TermDTO();
        if (s == 1) { cur.setTermYear(y - 1); cur.setTermSem((short) 2); }
        else        { cur.setTermYear(y);     cur.setTermSem((short) 1); }
        return cur;
    }

}
