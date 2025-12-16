package org.example.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class DbConfig {

    private static volatile DbConfig instance;
    private final HikariDataSource dataSource;

    private DbConfig() {
        Properties props = new Properties();

        try (InputStream is = DbConfig.class
                .getClassLoader()
                .getResourceAsStream("database.properties")) {

            if (is == null) {
                throw new IllegalStateException(
                        "Không tìm thấy database.properties trong src/main/resources");
            }
            props.load(is);

        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc database.properties", e);
        }

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(require(props, "db.url"));
        cfg.setUsername(require(props, "db.username"));
        cfg.setPassword(props.getProperty("db.password", ""));

        cfg.setMaximumPoolSize(intProp(props, "db.pool.maxSize", 10));
        cfg.setMinimumIdle(intProp(props, "db.pool.minIdle", 2));
        cfg.setIdleTimeout(longProp(props, "db.pool.idleTimeoutMs", 300_000));
        cfg.setConnectionTimeout(longProp(props, "db.pool.connectionTimeoutMs", 10_000));

        cfg.setPoolName("QLSV-HikariPool");
        cfg.setAutoCommit(true);

        this.dataSource = new HikariDataSource(cfg);
    }

    // =========================
    // Helpers đọc properties
    // =========================
    private static String require(Properties p, String key) {
        String v = p.getProperty(key);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Thiếu cấu hình: " + key);
        }
        return v.trim();
    }

    private static int intProp(Properties p, String key, int def) {
        String v = p.getProperty(key);
        if (v == null || v.isBlank()) return def;
        return Integer.parseInt(v.trim());
    }

    private static long longProp(Properties p, String key, long def) {
        String v = p.getProperty(key);
        if (v == null || v.isBlank()) return def;
        return Long.parseLong(v.trim());
    }

    // =========================
    // Singleton
    // =========================
    public static DbConfig getInstance() {
        if (instance == null) {
            synchronized (DbConfig.class) {
                if (instance == null) {
                    instance = new DbConfig();
                }
            }
        }
        return instance;
    }

    // =========================
    // ✅ STATIC getConnection
    // =========================
    public static Connection getConnection() throws SQLException {
        return getInstance().dataSource.getConnection();
    }

    // =========================
    // Shutdown pool (optional)
    // =========================
    public static void shutdown() {
        if (instance != null &&
                instance.dataSource != null &&
                !instance.dataSource.isClosed()) {

            instance.dataSource.close();
        }
    }
}
