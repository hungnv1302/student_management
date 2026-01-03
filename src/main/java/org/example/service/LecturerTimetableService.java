package org.example.service;

import org.example.dto.LecturerScheduleSlotDto;
import org.example.repository.LecturerTimetableRepository;

import java.util.LinkedHashMap;
import java.util.List;

public class LecturerTimetableService {

    private final LecturerTimetableRepository repo = new LecturerTimetableRepository();

    public LecturerTimetableRepository.Term getCurrentTermOrThrow() {
        try {
            return repo.getCurrentTerm()
                    .orElseThrow(() -> new IllegalStateException(
                            "registration_config(id=1) chưa có term_year/term_sem"));
        } catch (Exception e) {
            throw new RuntimeException("Không đọc được kỳ hiện tại", e);
        }
    }

    public LinkedHashMap<Integer, String> getShiftRanges() {
        try {
            return repo.loadShiftRanges();
        } catch (Exception e) {
            throw new RuntimeException("Không tải được shift_times", e);
        }
    }

    public List<LecturerScheduleSlotDto> getSlots(String lecturerId, int year, int sem) {
        try {
            return repo.findSlots(lecturerId, year, sem);
        } catch (Exception e) {
            throw new RuntimeException("Không tải được thời khóa biểu", e);
        }
    }
}
