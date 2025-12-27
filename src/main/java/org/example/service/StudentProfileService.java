package org.example.service;

import org.example.dto.StudentProfileDTO;
import org.example.repository.StudentProfileRepository;
import org.example.repository.StudentRepository;

import java.sql.SQLException;
import java.util.Objects;

public class StudentProfileService {

    private final StudentProfileRepository studentRepo = new StudentProfileRepository();

    public StudentProfileDTO loadProfile(String studentId) {
        Objects.requireNonNull(studentId, "studentId is null");
        try {
            StudentProfileDTO dto = studentRepo.getStudentProfile(studentId);
            if (dto == null) throw new RuntimeException("Không tìm thấy hồ sơ sinh viên: " + studentId);
            return dto;
        } catch (SQLException e) {
            throw new RuntimeException("DB error loadProfile: " + e.getMessage(), e);
        }
    }

    public void updateContact(String studentId, String phone, String email, String address) {
        Objects.requireNonNull(studentId, "studentId is null");

        // validate nhẹ (tuỳ cháu muốn chặt hơn)
        if (email != null && !email.isBlank() && !email.contains("@")) {
            throw new RuntimeException("Email không hợp lệ.");
        }

        try {
            // person_id = student_id theo hàm get_student_profile của cháu (JOIN p.person_id = s.student_id)
            studentRepo.updateMyContact(studentId,
                    phone == null ? null : phone.trim(),
                    email == null ? null : email.trim(),
                    address == null ? null : address.trim()
            );
        } catch (SQLException e) {
            throw new RuntimeException("DB error updateContact: " + e.getMessage(), e);
        }
    }
}
