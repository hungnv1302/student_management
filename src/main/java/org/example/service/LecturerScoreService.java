package org.example.service;

import org.example.dto.StudentScoreRow;
import org.example.repository.LecturerScoreRepository;

import java.math.BigDecimal;
import java.util.List;

public class LecturerScoreService {

    private final LecturerScoreRepository repo = new LecturerScoreRepository();

    public List<StudentScoreRow> load(String lecturerId, String classId) {
        try {
            if (!repo.isAssigned(lecturerId, classId)) {
                throw new IllegalStateException("Bạn không được phân công lớp " + classId);
            }
            return repo.findByClass(lecturerId, classId);
        } catch (Exception e) {
            throw new RuntimeException("Không tải được danh sách: " + e.getMessage(), e);
        }
    }

    /** lưu: chỉ lưu các ô NULL (DB sẽ tự bảo vệ bằng COALESCE) */
    public void save(String lecturerId, String classId, List<StudentScoreRow> rows) {
        try {
            if (!repo.isAssigned(lecturerId, classId)) {
                throw new IllegalStateException("Bạn không được phân công lớp " + classId);
            }

            for (StudentScoreRow r : rows) {
                if (r.isFinalized()) continue;

                validate(r.getMidtermScore());
                validate(r.getFinalScore());
                validate(r.getTotalScore());

                repo.updateScoresOnlyIfNull(
                        r.getEnrollId(),
                        r.getMidtermScore(),
                        r.getFinalScore(),
                        r.getTotalScore()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Lưu điểm thất bại: " + e.getMessage(), e);
        }
    }

    /** chốt: bắt buộc không còn NULL rồi mới chốt */
    public void finalizeScores(String lecturerId, String classId, List<StudentScoreRow> rows) {
        try {
            // check đủ điểm
            for (StudentScoreRow r : rows) {
                if (r.isFinalized()) continue;
                if (r.getMidtermScore() == null || r.getFinalScore() == null || r.getTotalScore() == null) {
                    throw new IllegalStateException("Không thể chốt: vẫn còn sinh viên chưa đủ điểm (NULL).");
                }
            }

            // lưu trước (phòng trường hợp có sửa nhưng chưa save)
            save(lecturerId, classId, rows);

            // chốt
            repo.finalizeClass(classId);

        } catch (Exception e) {
            throw new RuntimeException("Chốt điểm thất bại: " + e.getMessage(), e);
        }
    }

    private void validate(BigDecimal x) {
        if (x == null) return;
        if (x.compareTo(BigDecimal.ZERO) < 0 || x.compareTo(new BigDecimal("10")) > 0) {
            throw new IllegalArgumentException("Điểm phải nằm trong [0..10]");
        }
    }
}
