package org.example.config;

import org.example.service.security.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

public class SeedUsersFull {

    // Password mặc định để test
    private static final String ADMIN_PW = "admin123";
    private static final String LECTURER_PW = "lecturer123";
    private static final String STUDENT_PW = "student123";

    public static void main(String[] args) throws Exception {
        seedAdmins();
        seedLecturers();
        seedStudents();
        System.out.println("✅ SeedUsersFull DONE");
        System.out.println("ADMIN: admin01/admin123");
        System.out.println("LECTURER: lecturer01/lecturer123");
        System.out.println("STUDENT: student001/student123");
    }

    private static void upsertUser(String username, String role, String rawPassword) throws Exception {
        String salt = PasswordUtil.newSaltBase64();
        String hash = PasswordUtil.hashPasswordBase64(rawPassword.toCharArray(), salt);

        // giả sử bảng users có các cột: user_id, username, password_hash, password_salt, role, state
        // và username UNIQUE
        String sql = """
            INSERT INTO public.users (user_id, username, password_hash, password_salt, role, state)
            VALUES (?, ?, ?, ?, ?, 'ACTIVE')
            ON CONFLICT (username) DO UPDATE SET
                password_hash = EXCLUDED.password_hash,
                password_salt = EXCLUDED.password_salt,
                role          = EXCLUDED.role,
                state         = 'ACTIVE'
        """;

        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, username);
            ps.setString(3, hash);
            ps.setString(4, salt);
            ps.setString(5, role);
            ps.executeUpdate();
        }
    }

    private static void seedAdmins() throws Exception {
        for (int i = 1; i <= 3; i++) {
            String u = String.format("admin%02d", i);
            upsertUser(u, "ADMIN", ADMIN_PW);
        }
    }

    private static void seedLecturers() throws Exception {
        for (int i = 1; i <= 20; i++) {
            String u = String.format("lecturer%02d", i);
            upsertUser(u, "LECTURER", LECTURER_PW);
        }
    }

    private static void seedStudents() throws Exception {
        for (int i = 1; i <= 50; i++) {
            String u = String.format("student%03d", i);
            upsertUser(u, "STUDENT", STUDENT_PW);
        }
    }
}
