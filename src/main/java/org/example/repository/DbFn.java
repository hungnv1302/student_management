package org.example.repository;

import org.example.config.DbConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class DbFn {
    private DbFn() {}

    // =========================
    // Query helpers (auto-connection)
    // =========================
    public static <T> List<T> queryList(String sql, SqlBinder binder, RowMapper<T> mapper) throws SQLException {
        try (Connection c = DbConfig.getInstance().getConnection()) {
            return queryList(c, sql, binder, mapper);
        }
    }

    public static <T> T queryOne(String sql, SqlBinder binder, RowMapper<T> mapper) throws SQLException {
        List<T> list = queryList(sql, binder, mapper);
        return list.isEmpty() ? null : list.get(0);
    }

    public static <T> T queryScalar(String sql, SqlBinder binder, ScalarMapper<T> mapper) throws SQLException {
        return queryOne(sql, binder, rs -> mapper.map(rs));
    }

    public static void exec(String sql, SqlBinder binder) throws SQLException {
        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (binder != null) binder.bind(ps);
            ps.execute();
        }
    }

    public static int execUpdate(String sql, SqlBinder binder) throws SQLException {
        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (binder != null) binder.bind(ps);
            return ps.executeUpdate();
        }
    }

    // =========================
    // Same helpers but with PROVIDED connection (for transactions)
    // =========================
    public static <T> List<T> queryList(Connection c, String sql, SqlBinder binder, RowMapper<T> mapper) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            if (binder != null) binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> out = new ArrayList<>();
                while (rs.next()) out.add(mapper.map(rs));
                return out;
            }
        }
    }

    public static <T> T queryOne(Connection c, String sql, SqlBinder binder, RowMapper<T> mapper) throws SQLException {
        List<T> list = queryList(c, sql, binder, mapper);
        return list.isEmpty() ? null : list.get(0);
    }

    public static <T> T queryScalar(Connection c, String sql, SqlBinder binder, ScalarMapper<T> mapper) throws SQLException {
        return queryOne(c, sql, binder, rs -> mapper.map(rs));
    }

    public static int execUpdate(Connection c, String sql, SqlBinder binder) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            if (binder != null) binder.bind(ps);
            return ps.executeUpdate();
        }
    }

    public static void exec(Connection c, String sql, SqlBinder binder) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            if (binder != null) binder.bind(ps);
            ps.execute();
        }
    }

    // =========================
    // Transaction helper
    // =========================
    public static <T> T tx(TxWork<T> work) throws SQLException {
        try (Connection c = DbConfig.getInstance().getConnection()) {
            boolean oldAuto = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                T result = work.run(c);
                c.commit();
                return result;
            } catch (Exception ex) {
                try { c.rollback(); } catch (SQLException ignore) {}
                if (ex instanceof SQLException se) throw se;
                throw new SQLException("Transaction failed: " + ex.getMessage(), ex);
            } finally {
                try { c.setAutoCommit(oldAuto); } catch (SQLException ignore) {}
            }
        }
    }

    // =========================
    // Functional interfaces
    // =========================
    @FunctionalInterface public interface SqlBinder { void bind(PreparedStatement ps) throws SQLException; }
    @FunctionalInterface public interface RowMapper<T> { T map(ResultSet rs) throws SQLException; }
    @FunctionalInterface public interface ScalarMapper<T> { T map(ResultSet rs) throws SQLException; }
    @FunctionalInterface public interface TxWork<T> { T run(Connection c) throws Exception; }
}
