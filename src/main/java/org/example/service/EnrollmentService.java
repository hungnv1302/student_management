package org.example.service;

import org.example.domain.ClassSection;
import org.example.domain.Student;
import org.example.domain.TimeSlot;
import org.example.domain.enums.StudentStatus;
import org.example.repository.*;
import org.example.service.exception.BusinessException;
import org.example.util.TimeSlotUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class EnrollmentService {

    private final StudentRepository studentRepo = new StudentRepository();
    private final SubjectRepository subjectRepo = new SubjectRepository();
    private final ClassSectionRepository classRepo = new ClassSectionRepository();
    private final EnrollmentRepository enrollmentRepo = new EnrollmentRepository();
    private final TimeSlotRepository timeSlotRepo = new TimeSlotRepository();
    private final RegistrationConfigRepository configRepo = new RegistrationConfigRepository();

    public void register(String studentId, String classId) throws SQLException {
        if (studentId == null || studentId.isBlank()) throw new BusinessException("studentId trống");
        if (classId == null || classId.isBlank()) throw new BusinessException("classId trống");

        Student st = studentRepo.findById(studentId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy sinh viên"));

        // ✅ Chỉ cho đăng ký khi STUDYING
        if (st.getStatus() == null || st.getStatus() != StudentStatus.STUDYING) {
            throw new BusinessException("Sinh viên không ở trạng thái STUDYING");
        }

        ClassSection cs = classRepo.findById(classId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy lớp học phần"));

        if (cs.getSubject() == null) throw new BusinessException("Lớp học phần chưa gắn môn học");

        // 1) Check mở/đóng đăng ký
        var cfg = configRepo.findBySemesterYear(cs.getSemester(), cs.getYear())
                .orElseThrow(() -> new BusinessException("Chưa cấu hình đăng ký cho kỳ này"));

        if (!cfg.isOpen()) throw new BusinessException("Hệ thống đang đóng đăng ký");

        LocalDateTime now = LocalDateTime.now();
        if (cfg.openAt() != null && now.isBefore(cfg.openAt())) throw new BusinessException("Chưa đến thời gian mở đăng ký");
        if (cfg.closeAt() != null && now.isAfter(cfg.closeAt())) throw new BusinessException("Đã hết thời gian đăng ký");

        // 2) Đã đăng ký lớp này chưa
        if (enrollmentRepo.isEnrolled(studentId, classId)) {
            throw new BusinessException("Bạn đã đăng ký lớp này rồi");
        }

        // 3) Capacity
        int current = classRepo.countEnrolled(classId);
        if (current >= cs.getCapacity()) throw new BusinessException("Lớp đã đủ số lượng");

        // 4) Max credits
        int currentCredits = enrollmentRepo.sumEnrolledCreditsInTerm(studentId, cs.getSemester(), cs.getYear());
        int newCredits = cs.getSubject().getCredit();

        if (currentCredits + newCredits > cfg.maxCredits()) {
            throw new BusinessException("Vượt quá số tín chỉ tối đa (" + cfg.maxCredits() + ")");
        }

        // 5) Trùng lịch (chỉ trong kỳ)
        List<String> enrolledClassIds = enrollmentRepo.findEnrolledClassIdsInTerm(studentId, cs.getSemester(), cs.getYear());
        List<TimeSlot> newSlots = timeSlotRepo.findByClassId(classId);

        for (String oldClassId : enrolledClassIds) {
            List<TimeSlot> oldSlots = timeSlotRepo.findByClassId(oldClassId);

            for (TimeSlot a : newSlots) {
                for (TimeSlot b : oldSlots) {
                    if (TimeSlotUtil.isOverlap(a, b)) {
                        throw new BusinessException("Trùng lịch với lớp: " + oldClassId);
                    }
                }
            }
        }

        // 6) Tiên quyết
        var prereqIds = subjectRepo.findPrereqSubjectIds(cs.getSubject().getSubjectID());
        for (String prereqSubId : prereqIds) {
            boolean ok = enrollmentRepo.hasPassedSubject(studentId, prereqSubId);
            if (!ok) throw new BusinessException("Chưa đạt môn tiên quyết: " + prereqSubId);
        }

        // 7) Ghi enrollment
        enrollmentRepo.insertEnrollment(UUID.randomUUID().toString(), studentId, classId);
    }

    public void cancel(String studentId, String classId) throws SQLException {
        if (!enrollmentRepo.isEnrolled(studentId, classId)) {
            throw new BusinessException("Bạn chưa đăng ký lớp này");
        }
        enrollmentRepo.cancelEnrollment(studentId, classId);
    }
}
