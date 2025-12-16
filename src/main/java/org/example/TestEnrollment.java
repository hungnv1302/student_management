package org.example;

import org.example.service.EnrollmentService;
import org.example.service.exception.BusinessException;

public class TestEnrollment {
    public static void main(String[] args) {
        EnrollmentService service = new EnrollmentService();

        String studentId = "10003";
        String classId   = "502";

        try {
            service.register(studentId, classId);
            System.out.println("✅ Đăng ký thành công!");
        } catch (BusinessException e) {
            System.out.println("❌ Lỗi nghiệp vụ: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Lỗi hệ thống:");
            e.printStackTrace();
        }
    }
}
