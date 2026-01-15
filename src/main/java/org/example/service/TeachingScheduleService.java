package org.example.service;

import org.example.dto.AssignedClassDto;
import org.example.dto.StudentInClassDto;
import org.example.repository.TeachingScheduleRepository;

import java.util.List;

public class TeachingScheduleService {

    private final TeachingScheduleRepository repo;

    public TeachingScheduleService() {
        this(new TeachingScheduleRepository());
    }

    public TeachingScheduleService(TeachingScheduleRepository repo) {
        this.repo = repo;
    }

    /** Chỉ dùng khi bạn thật sự cần resolve từ loginName (username/email) */
    public String resolveLecturerIdOrThrow(String login) {
        try {
            return repo.resolveLecturerId(login)
                    .orElseThrow(() -> new IllegalStateException(
                            "Không xác định được lecturer_id từ tài khoản: " + login +
                                    ". Hãy kiểm tra mapping users.person_id hoặc đăng nhập bằng email/mã GV."
                    ));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi resolve lecturer_id.", e);
        }
    }

    public List<TeachingScheduleRepository.Term> getTermsOfLecturer(String lecturerId) {
        try {
            return repo.findTermsOfLecturer(lecturerId);
        } catch (Exception e) {
            throw new RuntimeException("Không tải được danh sách kỳ học.", e);
        }
    }

    public List<AssignedClassDto> getAssignedClassesDefault(String lecturerId) {
        var terms = getTermsOfLecturer(lecturerId);
        if (terms.isEmpty()) return List.of();
        return getAssignedClassesAllTerms(lecturerId);
    }

    public List<AssignedClassDto> getAssignedClassesInTerm(String lecturerId, int year, int sem) {
        try {
            return repo.findAssignedClassesByLecturerInTerm(lecturerId, year, sem);
        } catch (Exception e) {
            throw new RuntimeException("Không tải được lớp theo kỳ.", e);
        }
    }

    public List<AssignedClassDto> getAssignedClassesAllTerms(String lecturerId) {
        try {
            return repo.findAssignedClassesByLecturerAllTerms(lecturerId);
        } catch (Exception e) {
            throw new RuntimeException("Không tải được lớp (tất cả kỳ).", e);
        }
    }

    public List<StudentInClassDto> getStudentsInClass(String classId) {
        try {
            return repo.findStudentsInClass(classId);
        } catch (Exception e) {
            throw new RuntimeException("Không tải được danh sách sinh viên.", e);
        }
    }
}
