package org.example.service;

import org.example.repository.ScoreReviewRequestRepository;

public class ScoreReviewRequestService {

    private final ScoreReviewRequestRepository repo = new ScoreReviewRequestRepository();

    public int createScoreReviewRequest(String studentId, int enrollId, String reason) {
        try {
            if (reason == null || reason.isBlank())
                throw new RuntimeException("Vui lòng nhập lý do phúc tra.");

            if (repo.hasPendingRequest(studentId, enrollId))
                throw new RuntimeException("Bạn đã gửi phúc tra cho môn này rồi (đang chờ xử lý).");

            return repo.createRequest(studentId, enrollId, reason.trim());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
