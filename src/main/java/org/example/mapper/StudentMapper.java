package org.example.mapper;

import org.example.domain.Student;
import org.example.domain.enums.Gender;
import org.example.domain.enums.StudentStatus;
import org.example.dto.StudentDTO;

public class StudentMapper {

    // ===== DTO -> ENTITY =====
    public static Student toEntity(StudentDTO dto) {
        if (dto == null) return null;

        Student s = new Student();
        s.setStudentID(dto.getStudentID());
        s.setFullName(dto.getFullName());
        s.setDateOfBirth(dto.getDateOfBirth());
        s.setPhoneNumber(dto.getPhoneNumber());
        s.setEmail(dto.getEmail());
        s.setAddress(dto.getAddress());

        s.setDepartment(dto.getDepartment());
        s.setMajor(dto.getMajor());
        s.setClassName(dto.getClassName());
        s.setAdmissionYear(dto.getAdmissionYear());
        s.setGpa(dto.getGpa() == null ? 0 : dto.getGpa());
        s.setTrainingScore(dto.getTrainingScore() == null ? 0 : dto.getTrainingScore());

        // ===== ENUM mapping =====
        if (dto.getGender() != null) {
            s.setGender(Gender.valueOf(dto.getGender()));
        }

        if (dto.getStatus() != null) {
            s.setStatus(StudentStatus.valueOf(dto.getStatus()));
        }

        return s;
    }

    // ===== ENTITY -> DTO =====
    public static StudentDTO toDTO(Student s) {
        if (s == null) return null;

        StudentDTO dto = new StudentDTO();
        dto.setStudentID(s.getStudentID());
        dto.setFullName(s.getFullName());
        dto.setDateOfBirth(s.getDateOfBirth());
        dto.setPhoneNumber(s.getPhoneNumber());
        dto.setEmail(s.getEmail());
        dto.setAddress(s.getAddress());

        dto.setDepartment(s.getDepartment());
        dto.setMajor(s.getMajor());
        dto.setClassName(s.getClassName());
        dto.setAdmissionYear(s.getAdmissionYear());
        dto.setGpa(s.getGpa());
        dto.setTrainingScore(s.getTrainingScore());

        // ===== ENUM -> String (cho UI dễ dùng) =====
        if (s.getGender() != null) {
            dto.setGender(s.getGender().name());
        }

        if (s.getStatus() != null) {
            dto.setStatus(s.getStatus().name());
        }

        return dto;
    }
}
