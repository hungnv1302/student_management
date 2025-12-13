package org.example.config;

import org.example.service.security.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

public class SeedAccountsFull {

    public static void main(String[] args) throws Exception {
        try (Connection c = DbConfig.getInstance().getConnection()) {
            c.setAutoCommit(false);
            try {
                int a = seedAdmins(c, 3);
                int l = seedLecturers(c, 20);
                int s = seedStudents(c, 50);

                c.commit();
                System.out.println("✅ Seed done");
                System.out.println("Admins inserted: " + a + " (pass Admin@123)");
                System.out.println("Lecturers inserted: " + l + " (pass Lec@123)");
                System.out.println("Students inserted: " + s + " (pass Stu@123)");
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private static int seedAdmins(Connection c, int count) throws Exception {
        int inserted = 0;
        for (int i = 1; i <= count; i++) {
            String username = "admin" + String.format("%02d", i);
            String staffId  = "AD" + String.format("%02d", i);

            if (existsUsername(c, username)) continue;

            String personId = UUID.randomUUID().toString();
            insertPerson(c, personId, "Admin " + i, "admin" + i + "@qlsv.local");

            String userId = UUID.randomUUID().toString();
            insertUser(c, userId, username, "Admin@123", "ADMIN", personId);

            insertAdmin(c, staffId, personId, "Training", "Admin");

            inserted++;
        }
        return inserted;
    }

    private static int seedLecturers(Connection c, int count) throws Exception {
        int inserted = 0;
        for (int i = 1; i <= count; i++) {
            String username  = "lecturer" + String.format("%02d", i);
            String lecturerId = "GV" + String.format("%03d", i);

            if (existsUsername(c, username)) continue;

            String personId = UUID.randomUUID().toString();
            insertPerson(c, personId, "Lecturer " + i, "lecturer" + i + "@qlsv.local");

            String userId = UUID.randomUUID().toString();
            insertUser(c, userId, username, "Lec@123", "LECTURER", personId);

            insertLecturer(c, lecturerId, personId, "CNTT", "MSc", "Lecturer");

            inserted++;
        }
        return inserted;
    }

    private static int seedStudents(Connection c, int count) throws Exception {
        int inserted = 0;
        for (int i = 1; i <= count; i++) {
            String username  = "student" + String.format("%03d", i);
            String studentId = "SV" + String.format("%03d", i);

            if (existsUsername(c, username)) continue;

            String personId = UUID.randomUUID().toString();
            insertPerson(c, personId, "Student " + i, "student" + i + "@qlsv.local");

            String userId = UUID.randomUUID().toString();
            insertUser(c, userId, username, "Stu@123", "STUDENT", personId);

            insertStudentProfile(c, studentId, personId);

            inserted++;
        }
        return inserted;
    }

    // ===== helpers =====

    private static boolean existsUsername(Connection c, String username) throws Exception {
        String sql = "SELECT 1 FROM public.users WHERE username = ? LIMIT 1";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            return ps.executeQuery().next();
        }
    }

    private static void insertPerson(Connection c, String personId, String fullName, String email) throws Exception {
        String sql = """
            INSERT INTO public.persons(person_id, full_name, gender, phone_number, email, address)
            VALUES (?, ?, 'OTHER', '0000000000', ?, 'VN')
            ON CONFLICT (person_id) DO NOTHING
        """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, personId);
            ps.setString(2, fullName);
            ps.setString(3, email);
            ps.executeUpdate();
        }
    }

    private static void insertUser(Connection c, String userId, String username, String rawPassword, String role, String personId) throws Exception {
        String salt = PasswordUtil.newSaltBase64();
        String hash = PasswordUtil.hashPasswordBase64(rawPassword.toCharArray(), salt);

        String sql = """
    INSERT INTO public.users(user_id, username, password_hash, password_salt, role, state, person_id)
    VALUES (?, ?, ?, ?, ?, 'ACTIVE', ?)
    ON CONFLICT (username) DO UPDATE
    SET role = EXCLUDED.role,
        person_id = EXCLUDED.person_id
""";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, username);
            ps.setString(3, hash);
            ps.setString(4, salt);
            ps.setString(5, role);
            ps.setString(6, personId);
            ps.executeUpdate();
        }
    }

    private static void insertStudentProfile(Connection c, String studentId, String personId) throws Exception {
        String sql = """
            INSERT INTO public.students_profile(student_id, person_id, department, major, class_name, admission_year, gpa, training_score, status)
            VALUES (?, ?, 'CNTT', 'KTPM', 'K67', 2022, 0, 0, 'STUDYING')
            ON CONFLICT (student_id) DO NOTHING
        """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, personId);
            ps.executeUpdate();
        }
    }

    private static void insertLecturer(Connection c, String lecturerId, String personId, String dept, String degree, String position) throws Exception {
        String sql = """
            INSERT INTO public.lecturers(lecturer_id, person_id, department, degree, position)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (lecturer_id) DO NOTHING
        """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, lecturerId);
            ps.setString(2, personId);
            ps.setString(3, dept);
            ps.setString(4, degree);
            ps.setString(5, position);
            ps.executeUpdate();
        }
    }

    private static void insertAdmin(Connection c, String staffId, String personId, String dept, String position) throws Exception {
        String sql = """
            INSERT INTO public.admins(staff_id, person_id, department, position)
            VALUES (?, ?, ?, ?)
            ON CONFLICT (staff_id) DO NOTHING
        """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, staffId);
            ps.setString(2, personId);
            ps.setString(3, dept);
            ps.setString(4, position);
            ps.executeUpdate();
        }
    }
}
