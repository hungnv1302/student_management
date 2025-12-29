package org.example.repository;

import org.example.dto.StudentProfileDTO;

import java.sql.SQLException;
import java.time.LocalDate;

public class StudentProfileRepository {

    // ✅ DÙNG 1 HÀM DUY NHẤT
    public StudentProfileDTO getStudentProfile(String studentId) throws SQLException {
        String sql = """
            SELECT
                s.student_id,
                p.full_name,
                p.dob,
                p.gender,
                p.phone_number,
                p.email,
                p.address,

                s.department,
                s.major,
                s.class_name,
                s.admission_year,
                s.training_score,
                s.status AS student_status
            FROM qlsv.students s
            JOIN qlsv.persons p ON p.person_id = s.student_id   -- ✅ FIX: students không có person_id
            WHERE s.student_id = ?
        """;

        return DbFn.queryOne(sql,
                ps -> ps.setString(1, studentId),
                rs -> {
                    StudentProfileDTO d = new StudentProfileDTO();
                    d.setStudentId(rs.getString("student_id"));

                    d.setFullName(rs.getString("full_name"));

                    LocalDate dob = rs.getObject("dob", LocalDate.class);
                    d.setDob(dob);

                    d.setGender(rs.getString("gender"));
                    d.setPhoneNumber(rs.getString("phone_number"));
                    d.setEmail(rs.getString("email"));
                    d.setAddress(rs.getString("address"));

                    d.setDepartment(rs.getString("department"));
                    d.setMajor(rs.getString("major"));
                    d.setClassName(rs.getString("class_name"));
                    d.setAdmissionYear((Integer) rs.getObject("admission_year"));
                    d.setTrainingScore((Integer) rs.getObject("training_score"));

                    d.setStatus(rs.getString("student_status")); // ✅ lấy theo alias
                    return d;
                }
        );
    }

    // ✅ update persons theo person_id = student_id
    public void updateMyContact(String studentId, String phone, String email, String address) throws SQLException {
        String sql = """
            UPDATE qlsv.persons
            SET phone_number = ?, email = ?, address = ?
            WHERE person_id = ?
        """;
        DbFn.exec(sql, ps -> {
            ps.setString(1, phone);
            ps.setString(2, email);
            ps.setString(3, address);
            ps.setString(4, studentId); // ✅ FIX: truyền studentId luôn
        });
    }

}
