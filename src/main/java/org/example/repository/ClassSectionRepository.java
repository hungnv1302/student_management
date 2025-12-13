package org.example.repository;

import org.example.config.DbConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClassSectionRepository {

    public boolean existsById(String classId) throws SQLException {
        String sql = "SELECT 1 FROM public.class_sections WHERE class_id = ?";

        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int getCapacity(String classId, Connection c) throws SQLException {
        String sql = "SELECT capacity FROM public.class_sections WHERE class_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Không tìm thấy class_section: " + classId);
                return rs.getInt("capacity");
            }
        }
    }

    public int countEnrolled(String classId, Connection c) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM public.enrollments WHERE class_id = ? AND status = 'ENROLLED'";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("cnt");
            }
        }
    }
}
