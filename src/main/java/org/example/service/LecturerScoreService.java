package org.example.service;

import org.example.dto.GradeRowDto;
import org.example.repository.LecturerScoreRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class LecturerScoreService {

    private final LecturerScoreRepository repo = new LecturerScoreRepository();

    public List<GradeRowDto> load(String lecturerId, String classId) throws Exception {
        if (classId == null || classId.isBlank()) throw new IllegalArgumentException("Bạn chưa nhập mã lớp.");
        if (!repo.isAssigned(lecturerId, classId)) throw new IllegalStateException("Bạn không được phân công lớp này.");
        return repo.loadRows(lecturerId, classId.trim());
    }

    public BigDecimal computeTotal(BigDecimal mid, BigDecimal fin) {
        if (mid == null || fin == null) return null;
        return mid.multiply(new BigDecimal("0.4"))
                .add(fin.multiply(new BigDecimal("0.6")))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void save(String lecturerId, String classId, List<GradeRowDto> rows) throws Exception {
        if (!repo.isAssigned(lecturerId, classId)) throw new IllegalStateException("Bạn không được phân công lớp này.");

        for (GradeRowDto r : rows) {
            if (r.isFinalized()) continue;

            BigDecimal total = r.getTotal();
            if (total == null) total = computeTotal(r.getMidterm(), r.getFin());

            repo.saveDraft(r.getEnrollId(), r.getMidterm(), r.getFin(), total);
        }
    }

    public void finalizeAll(String lecturerId, String classId, List<GradeRowDto> rows) throws Exception {
        // chốt thì lưu trước
        save(lecturerId, classId, rows);
        repo.finalizeClass(lecturerId, classId);
    }
}
