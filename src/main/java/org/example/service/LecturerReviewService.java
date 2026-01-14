package org.example.service;

import org.example.dto.LecturerReviewDTO;
import org.example.repository.LecturerReviewRepository;

import java.math.BigDecimal;
import java.util.List;

public class LecturerReviewService {

    private final LecturerReviewRepository repo = new LecturerReviewRepository();

    /**
     * Lấy danh sách yêu cầu phúc tra
     */
    public List<LecturerReviewDTO> getReviewRequests(String lecturerId, String filterStatus) throws Exception {
        if (lecturerId == null || lecturerId.isBlank()) {
            throw new IllegalArgumentException("Lecturer ID không hợp lệ");
        }
        return repo.getReviewRequests(lecturerId, filterStatus);
    }

    /**
     * Lấy chi tiết yêu cầu phúc tra
     */
    public LecturerReviewDTO getRequestDetail(int requestId, String lecturerId) throws Exception {
        LecturerReviewDTO dto = repo.getRequestDetail(requestId, lecturerId);
        if (dto == null) {
            throw new IllegalStateException("Không tìm thấy yêu cầu phúc tra");
        }
        return dto;
    }

    /**
     * Chấp nhận yêu cầu phúc tra
     */
    public void approveRequest(int requestId, String lecturerId, BigDecimal newTotal, String note) throws Exception {
        // Validate
        if (newTotal == null) {
            throw new IllegalArgumentException("Điểm mới không được để trống");
        }

        if (newTotal.compareTo(BigDecimal.ZERO) < 0 || newTotal.compareTo(BigDecimal.TEN) > 0) {
            throw new IllegalArgumentException("Điểm mới phải trong khoảng 0-10");
        }

        // Kiểm tra yêu cầu có thuộc về giảng viên này không
        LecturerReviewDTO dto = repo.getRequestDetail(requestId, lecturerId);
        if (dto == null) {
            throw new IllegalStateException("Không tìm thấy yêu cầu phúc tra hoặc bạn không có quyền xử lý");
        }

        if (!"PENDING".equalsIgnoreCase(dto.getStatus())) {
            throw new IllegalStateException("Yêu cầu này đã được xử lý trước đó");
        }

        // Thực hiện chấp nhận
        repo.approveRequest(requestId, newTotal, note);
    }

    /**
     * Từ chối yêu cầu phúc tra
     */
    public void rejectRequest(int requestId, String lecturerId, String note) throws Exception {
        if (note == null || note.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do từ chối");
        }

        // Kiểm tra yêu cầu có thuộc về giảng viên này không
        LecturerReviewDTO dto = repo.getRequestDetail(requestId, lecturerId);
        if (dto == null) {
            throw new IllegalStateException("Không tìm thấy yêu cầu phúc tra hoặc bạn không có quyền xử lý");
        }

        if (!"PENDING".equalsIgnoreCase(dto.getStatus())) {
            throw new IllegalStateException("Yêu cầu này đã được xử lý trước đó");
        }

        // Thực hiện từ chối
        repo.rejectRequest(requestId, note);
    }

    /**
     * Lấy điểm chi tiết
     */
    public LecturerReviewRepository.EnrollmentScore getEnrollmentScore(int enrollId) throws Exception {
        return repo.getEnrollmentScore(enrollId);
    }
}