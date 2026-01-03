package org.example.service;

import org.example.dto.LecturerProfileDTO;
import org.example.repository.LecturerProfileRepository;
import java.sql.SQLException;
import java.util.Objects;

public class LecturerProfileService {
    private final LecturerProfileRepository repo = new LecturerProfileRepository();

    public LecturerProfileDTO loadProfile(String lecturerId) {
        if (lecturerId == null) return null;
        try {
            return repo.getLecturerProfile(lecturerId);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải hồ sơ: " + e.getMessage());
        }
    }

    public void updateContact(String id, String phone, String email, String address) {
        try {
            repo.updateContact(id, phone, email, address);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật: " + e.getMessage());
        }
    }
}