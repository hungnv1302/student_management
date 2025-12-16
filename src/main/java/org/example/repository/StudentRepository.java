package org.example.repository;

import org.example.config.DbConfig;
import org.example.domain.Student;
import org.example.domain.enums.Gender;
import org.example.domain.enums.StudentStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

public class StudentRepository {

    public Optional<Student> findById(String studentId) throws SQLException {
        String sql = """
            SELECT student_id, full_name, date_of_birth, gender, phone_number, email, address,
                   department, major, class_name, admission_year, gpa, training_score, status
            FROM students
            WHERE student_id = ?
        """;

        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Optional.empty();

            // Domain Student của cháu có constructor đầy đủ: (personID,..., studentID,...)
            // Ở bảng students không có person_id trong ảnh, nên mình set personID = student_id (an toàn để chạy).
            String personId = rs.getString("student_id");

            LocalDate dob = rs.getDate("date_of_birth") == null ? null : rs.getDate("date_of_birth").toLocalDate();
            Gender gender = rs.getString("gender") == null ? null : Gender.valueOf(rs.getString("gender").toUpperCase());
            StudentStatus status = rs.getString("status") == null ? null : StudentStatus.valueOf(rs.getString("status").toUpperCase());

            Student st = new Student(
                    personId,
                    rs.getString("full_name"),
                    dob,
                    gender,
                    rs.getString("phone_number"),
                    rs.getString("email"),
                    rs.getString("address"),
                    rs.getString("student_id"),
                    rs.getString("department"),
                    rs.getString("major"),
                    rs.getString("class_name"),
                    rs.getInt("admission_year"),
                    rs.getDouble("gpa"),
                    rs.getInt("training_score"),
                    status
            );
            return Optional.of(st);
        }
    }
}
