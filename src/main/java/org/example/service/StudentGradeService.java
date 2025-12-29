package org.example.service;

import org.example.dto.GradeRow;
import org.example.repository.ScoreReviewRequestRepository;
import org.example.repository.StudentGradeRepository;

import java.sql.SQLException;
import java.util.List;

public class StudentGradeService {
    private final StudentGradeRepository repo = new StudentGradeRepository();
    private final ScoreReviewRequestRepository reviewRepo = new ScoreReviewRequestRepository();

    public List<GradeRow> getStudentGrades(String studentId) {
        return repo.getStudentGrades(studentId);
    }

    public void createScoreReviewRequest(String studentId, int enrollId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new RuntimeException("Lý do phúc tra không được trống.");
        }
        try {
            reviewRepo.createRequest(studentId, enrollId, reason.trim());
        } catch (SQLException e) {
            throw new RuntimeException("Tạo yêu cầu phúc tra thất bại: " + e.getMessage(), e);
        }
    }
}
