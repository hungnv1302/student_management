package org.example.repository;

import org.example.config.DbConfig;
import org.example.domain.Student;
import org.example.domain.enums.Gender;
import org.example.domain.enums.StudentStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class StudentRepository {

    public Optional<Student> findById(String studentId) throws SQLException {
        String sql = """
            SELECT student_id, department, major, class_name, admission_year,
                   training_score, status,
                   p.full_name, p.dob, p.gender, p.phone_number, p.email, p.address
            FROM qlsv.students s
            JOIN qlsv.persons p ON p.person_id = s.student_id
            WHERE s.student_id = ?
        """;

        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                LocalDate dob = rs.getDate("dob") == null ? null : rs.getDate("dob").toLocalDate();
                Gender gender = rs.getString("gender") == null ? null : Gender.valueOf(rs.getString("gender").toUpperCase());
                StudentStatus status = rs.getString("status") == null ? null : StudentStatus.valueOf(rs.getString("status").toUpperCase());

                // personId == studentId (theo thiết kế)
                String personId = rs.getString("student_id");

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
                        0.0, // gpa/cpa không lưu ở students, CPA lấy từ view v_student_cpa
                        rs.getInt("training_score"),
                        status
                );

                return Optional.of(st);
            }
        }
    }
}
