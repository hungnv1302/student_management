package org.example.repository;

import org.example.dto.LecturerProfileDTO;
import java.sql.SQLException;
import java.time.LocalDate;

public class LecturerProfileRepository {
    public LecturerProfileDTO getLecturerProfile(String lecturerId) throws SQLException {
        String sql = """
            SELECT l.lecturer_id, p.full_name, p.dob, p.gender, p.phone_number, p.email, p.address,
                   l.department, l.degree, l.position
            FROM qlsv.lecturers l
            JOIN qlsv.persons p ON p.person_id = l.lecturer_id
            WHERE l.lecturer_id = ?
        """;
        return DbFn.queryOne(sql, ps -> ps.setString(1, lecturerId), rs -> {
            LecturerProfileDTO d = new LecturerProfileDTO();
            d.setLecturerId(rs.getString("lecturer_id"));
            d.setFullName(rs.getString("full_name"));
            d.setDob(rs.getObject("dob", LocalDate.class));
            d.setGender(rs.getString("gender"));
            d.setPhoneNumber(rs.getString("phone_number"));
            d.setEmail(rs.getString("email"));
            d.setAddress(rs.getString("address"));
            d.setDepartment(rs.getString("department"));
            d.setDegree(rs.getString("degree"));
            d.setPosition(rs.getString("position"));
            return d;
        });
    }

    public void updateContact(String lecturerId, String phone, String email, String address) throws SQLException {
        String sql = "UPDATE qlsv.persons SET phone_number = ?, email = ?, address = ? WHERE person_id = ?";
        DbFn.exec(sql, ps -> {
            ps.setString(1, phone);
            ps.setString(2, email);
            ps.setString(3, address);
            ps.setString(4, lecturerId);
        });
    }
}