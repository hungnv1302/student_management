package org.example.repository;

import org.example.config.DbConfig;
import org.example.domain.Student;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentRepository {

    // ===================== INSERT =====================
    public boolean insert(Student s) throws SQLException {
        String sql = """
            INSERT INTO students (
                student_id, full_name, date_of_birth, gender, phone_number, email, address,
                department, major, class_name, admission_year, gpa, training_score, status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, s.getStudentID());
            ps.setString(2, s.getFullName());
            setDate(ps, 3, s.getDateOfBirth());

            // Enum -> String (nếu domain dùng enum). Nếu domain dùng String thì sửa lại cho phù hợp.
            ps.setString(4, s.getGender() == null ? null : s.getGender().name());

            ps.setString(5, s.getPhoneNumber());
            ps.setString(6, s.getEmail());
            ps.setString(7, s.getAddress());

            ps.setString(8, s.getDepartment());
            ps.setString(9, s.getMajor());
            ps.setString(10, s.getClassName());
            setInteger(ps, 11, s.getAdmissionYear());

            ps.setDouble(12, s.getGpa());
            ps.setInt(13, s.getTrainingScore());

            ps.setString(14, s.getStatus() == null ? null : s.getStatus().name());

            return ps.executeUpdate() == 1;
        }
    }

    // ===================== FIND BY ID =====================
    public Optional<Student> findById(String studentId) throws SQLException {
        String sql = "SELECT * FROM students WHERE student_id = ?";

        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(mapRow(rs));
            }
        }
    }

    // ===================== FIND ALL =====================
    public List<Student> findAll() throws SQLException {
        String sql = "SELECT * FROM students ORDER BY student_id";
        List<Student> list = new ArrayList<>();

        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ===================== UPDATE =====================
    public boolean update(Student s) throws SQLException {
        String sql = """
            UPDATE students SET
                full_name = ?,
                date_of_birth = ?,
                gender = ?,
                phone_number = ?,
                email = ?,
                address = ?,
                department = ?,
                major = ?,
                class_name = ?,
                admission_year = ?,
                gpa = ?,
                training_score = ?,
                status = ?
            WHERE student_id = ?
            """;

        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, s.getFullName());
            setDate(ps, 2, s.getDateOfBirth());
            ps.setString(3, s.getGender() == null ? null : s.getGender().name());
            ps.setString(4, s.getPhoneNumber());
            ps.setString(5, s.getEmail());
            ps.setString(6, s.getAddress());

            ps.setString(7, s.getDepartment());
            ps.setString(8, s.getMajor());
            ps.setString(9, s.getClassName());
            setInteger(ps, 10, s.getAdmissionYear());

            ps.setDouble(11, s.getGpa());
            ps.setInt(12, s.getTrainingScore());
            ps.setString(13, s.getStatus() == null ? null : s.getStatus().name());

            ps.setString(14, s.getStudentID());

            return ps.executeUpdate() == 1;
        }
    }

    // ===================== DELETE =====================
    public boolean deleteById(String studentId) throws SQLException {
        String sql = "DELETE FROM students WHERE student_id = ?";

        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, studentId);
            return ps.executeUpdate() == 1;
        }
    }

    // ===================== Helpers =====================
    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();

        // Student fields
        s.setStudentID(rs.getString("student_id"));
        s.setDepartment(rs.getString("department"));
        s.setMajor(rs.getString("major"));
        s.setClassName(rs.getString("class_name"));

        Integer admissionYear = getInteger(rs, "admission_year");
        if (admissionYear != null) s.setAdmissionYear(admissionYear);

        s.setGpa(rs.getDouble("gpa"));
        s.setTrainingScore(rs.getInt("training_score"));

        // Person fields (Student extends Person)
        s.setFullName(rs.getString("full_name"));
        Date dob = rs.getDate("date_of_birth");
        if (dob != null) s.setDateOfBirth(dob.toLocalDate());

        String genderStr = rs.getString("gender");
        if (genderStr != null) {
            try { s.setGender(org.example.domain.enums.Gender.valueOf(genderStr)); }
            catch (Exception ignored) { /* nếu DB có giá trị lạ thì bỏ qua */ }
        }

        s.setPhoneNumber(rs.getString("phone_number"));
        s.setEmail(rs.getString("email"));
        s.setAddress(rs.getString("address"));

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try { s.setStatus(org.example.domain.enums.StudentStatus.valueOf(statusStr)); }
            catch (Exception ignored) { }
        }

        return s;
    }

    private static void setDate(PreparedStatement ps, int index, LocalDate date) throws SQLException {
        if (date == null) ps.setNull(index, Types.DATE);
        else ps.setDate(index, Date.valueOf(date));
    }

    private static void setInteger(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) ps.setNull(index, Types.INTEGER);
        else ps.setInt(index, value);
    }

    private static Integer getInteger(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }
}
