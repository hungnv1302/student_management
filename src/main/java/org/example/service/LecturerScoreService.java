package org.example.service;

import org.example.dto.GradeRowDto;
import org.example.repository.LecturerScoreRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class LecturerScoreService {

    private final LecturerScoreRepository repo = new LecturerScoreRepository();

    public List<GradeRowDto> load(String lecturerId, String classId) throws Exception {
        if (classId == null || classId.isBlank()) {
            throw new IllegalArgumentException("Bạn chưa nhập mã lớp.");
        }
        if (!repo.isAssigned(lecturerId, classId)) {
            throw new IllegalStateException("Bạn không được phân công lớp này.");
        }
        return repo.loadRows(lecturerId, classId.trim());
    }

    public BigDecimal computeTotal(BigDecimal mid, BigDecimal fin) {
        if (mid == null || fin == null) return null;
        return mid.multiply(new BigDecimal("0.5"))
                .add(fin.multiply(new BigDecimal("0.5")))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void save(String lecturerId, String classId, List<GradeRowDto> rows) throws Exception {
        if (!repo.isAssigned(lecturerId, classId)) {
            throw new IllegalStateException("Bạn không được phân công lớp này.");
        }

        for (GradeRowDto r : rows) {
            if (r.isFinalized()) continue; // Bỏ qua sinh viên đã chốt điểm

            // Validate điểm trong khoảng 0-10
            validateScore(r.getMidterm(), "Điểm giữa kỳ");
            validateScore(r.getFin(), "Điểm cuối kỳ");

            // Lưu điểm (KHÔNG tính total ở đây)
            repo.saveDraft(r.getEnrollId(), r.getMidterm(), r.getFin());
        }
    }

    // Tính điểm học phần cho toàn bộ lớp
    public int calculateAllFinalGrades(String lecturerId, String classId) throws Exception {
        if (!repo.isAssigned(lecturerId, classId)) {
            throw new IllegalStateException("Bạn không được phân công lớp này.");
        }

        int updated = repo.calculateFinalGrades(lecturerId, classId);

        if (updated == 0) {
            throw new IllegalStateException(
                    "Không có sinh viên nào đủ điều kiện tính điểm. " +
                            "Sinh viên phải có đủ điểm GK và CK."
            );
        }

        return updated;
    }

    // Validate điểm phải từ 0-10
    private void validateScore(BigDecimal score, String label) throws IllegalArgumentException {
        if (score == null) return; // NULL được phép

        if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(BigDecimal.TEN) > 0) {
            throw new IllegalArgumentException(
                    label + " phải trong khoảng 0-10 (giá trị nhập: " + score + ")"
            );
        }
    }
}