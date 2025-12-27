package org.example.repository;

import org.example.config.DbConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class DbFn {
    private DbFn() {}

    public static <T> List<T> queryList(String sql, SqlBinder binder, RowMapper<T> mapper) throws SQLException {
        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            if (binder != null) binder.bind(ps);

            try (ResultSet rs = ps.executeQuery()) {
                List<T> out = new ArrayList<>();
                while (rs.next()) out.add(mapper.map(rs));
                return out;
            }
        }
    }

    public static <T> T queryOne(String sql, SqlBinder binder, RowMapper<T> mapper) throws SQLException {
        List<T> list = queryList(sql, binder, mapper);
        return list.isEmpty() ? null : list.get(0);
    }

    public static void exec(String sql, SqlBinder binder) throws SQLException {
        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            if (binder != null) binder.bind(ps);
            ps.execute();
        }
    }

    @FunctionalInterface
    public interface SqlBinder { void bind(PreparedStatement ps) throws SQLException; }

    @FunctionalInterface
    public interface RowMapper<T> { T map(ResultSet rs) throws SQLException; }
}
