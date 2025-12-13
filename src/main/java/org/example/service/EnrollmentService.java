package org.example.service;

import org.example.config.DbConfig;
import org.example.repository.*;
import org.example.service.exception.BusinessException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class EnrollmentService {

    private final ClassSectionRepository classRepo;
    private final EnrollmentRepository enrollRepo;

    private final ClassSemesterRepository classSemRepo;
    private final RegistrationConfigRepository regRepo;
    private final SubjectCreditRepository creditRepo;
    private final EnrollmentQueryRepository enrollQueryRepo;
    private final ScheduleConflictRepository conflictRepo;
    private final PrerequisiteRepository prereqRepo;

    public EnrollmentService(
            ClassSectionRepository classRepo,
            EnrollmentRepository enrollRepo,
            ClassSemesterRepository classSemRepo,
            RegistrationConfigRepository regRepo,
            SubjectCreditRepository creditRepo,
            EnrollmentQueryRepository enrollQueryRepo,
            ScheduleConflictRepository conflictRepo,
            PrerequisiteRepository prereqRepo
    ) {
        this.classRepo = classRepo;
        this.enrollRepo = enrollRepo;
        this.classSemRepo = classSemRepo;
        this.regRepo = regRepo;
        this.creditRepo = creditRepo;
        this.enrollQueryRepo = enrollQueryRepo;
        this.conflictRepo = conflictRepo;
        this.prereqRepo = prereqRepo;
    }

    public boolean register(String studentId, String classId) throws SQLException {
        if (studentId == null || studentId.isBlank()) throw new BusinessException("StudentID không hợp lệ");
        if (classId == null || classId.isBlank()) throw new BusinessException("ClassID không hợp lệ");

        try (Connection c = DbConfig.getInstance().getConnection()) {
            c.setAutoCommit(false);
            try {
                // (0) class tồn tại?
                if (!classRepo.existsById(classId)) throw new BusinessException("Lớp học phần không tồn tại: " + classId);

                // (1) lấy semester/year của class + policy
                var semYear = classSemRepo.getSemYear(classId, c);
                var policy = regRepo.getPolicy(semYear.semester(), semYear.year(), c);
                if (!policy.isOpen()) throw new BusinessException("Hiện đang ĐÓNG đăng ký cho " + semYear.semester() + " - " + semYear.year());

                // (2) đã đăng ký chưa
                if (enrollRepo.existsStudentInClass(studentId, classId, c)) {
                    throw new BusinessException("Sinh viên đã đăng ký lớp này rồi");
                }

                // (3) lớp full?
                int capacity = classRepo.getCapacity(classId, c);
                int enrolled = classRepo.countEnrolled(classId, c);
                if (enrolled >= capacity) throw new BusinessException("Lớp đã đầy");

                // (4) giới hạn tín chỉ
                int currentCredits = enrollQueryRepo.sumCreditsEnrolled(studentId, semYear.semester(), semYear.year(), c);
                int newCredits = creditRepo.getCreditByClassId(classId, c);
                if (currentCredits + newCredits > policy.maxCredits()) {
                    throw new BusinessException("Vượt quá tín chỉ tối đa (" + policy.maxCredits() + "). Hiện tại: " + currentCredits + ", thêm: " + newCredits);
                }

                // (5) check trùng lịch với các lớp đã đăng ký cùng kỳ
                List<String> enrolledClasses = enrollQueryRepo.getEnrolledClassIds(studentId, semYear.semester(), semYear.year(), c);
                for (String otherClassId : enrolledClasses) {
                    if (conflictRepo.isConflict(classId, otherClassId, c)) {
                        throw new BusinessException("Trùng lịch với lớp đã đăng ký: " + otherClassId);
                    }
                }

                // (6) check prereq
                if (!prereqRepo.hasAllPrerequisites(studentId, classId, c)) {
                    throw new BusinessException("Chưa đạt môn tiên quyết để đăng ký lớp này");
                }

                // (7) insert enrollment
                String enrollmentId = UUID.randomUUID().toString();
                boolean ok = enrollRepo.insert(enrollmentId, studentId, classId, c);

                c.commit();
                return ok;
            } catch (Exception e) {
                c.rollback();
                if (e instanceof BusinessException) throw (BusinessException) e;
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public boolean drop(String studentId, String classId) throws SQLException {
        // giữ code drop như cũ là OK
        if (studentId == null || studentId.isBlank()) throw new BusinessException("StudentID không hợp lệ");
        if (classId == null || classId.isBlank()) throw new BusinessException("ClassID không hợp lệ");

        try (Connection c = DbConfig.getInstance().getConnection()) {
            c.setAutoCommit(false);
            try {
                if (!enrollRepo.existsStudentInClass(studentId, classId, c)) {
                    throw new BusinessException("Sinh viên chưa đăng ký lớp này");
                }
                boolean ok = enrollRepo.delete(studentId, classId, c);
                c.commit();
                return ok;
            } catch (Exception e) {
                c.rollback();
                if (e instanceof BusinessException) throw (BusinessException) e;
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }
}
