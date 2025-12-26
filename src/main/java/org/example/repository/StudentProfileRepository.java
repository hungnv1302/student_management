package org.example.repository;

import org.example.dto.StudentProfileDTO;

import java.sql.SQLException;
import java.time.LocalDate;

public class StudentProfileRepository {

    public StudentProfileDTO getStudentProfile(String studentId) throws SQLException {
        String sql = "SELECT * FROM qlsv.get_student_profile(?)";
        return DbFn.queryOne(sql,
                ps -> ps.setString(1, studentId),
                rs -> {
                    StudentProfileDTO d = new StudentProfileDTO();
                    d.setStudentId(rs.getString("student_id"));
                    d.setFullName(rs.getString("full_name"));

                    // date -> LocalDate
                    LocalDate dob = rs.getObject("dob", LocalDate.class);
                    d.setDob(dob);

                    d.setGender(rs.getString("gender"));
                    d.setPhoneNumber(rs.getString("phone_number"));
                    d.setEmail(rs.getString("email"));
                    d.setAddress(rs.getString("address"));

                    d.setDepartment(rs.getString("department"));
                    d.setMajor(rs.getString("major"));
                    d.setClassName(rs.getString("class_name"));

                    Integer admissionYear = (Integer) rs.getObject("admission_year");
                    Integer trainingScore = (Integer) rs.getObject("training_score");
                    d.setAdmissionYear(admissionYear);
                    d.setTrainingScore(trainingScore);

                    d.setStatus(rs.getString("status"));
                    return d;
                });
    }

    /** SELECT qlsv.update_my_contact(p_person_id, p_phone, p_email, p_address) */
    public void updateMyContact(String personId, String phone, String email, String address) throws SQLException {
        String sql = "SELECT qlsv.update_my_contact(?, ?, ?, ?)";
        DbFn.exec(sql, ps -> {
            ps.setString(1, personId);
            ps.setString(2, phone);
            ps.setString(3, email);
            ps.setString(4, address);
        });
    }
}
