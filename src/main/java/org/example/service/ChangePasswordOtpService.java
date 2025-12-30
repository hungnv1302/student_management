package org.example.service;

import org.example.repository.ChangePasswordRepository;
import org.example.repository.PasswordOtpRepository;
import org.example.repository.UserEmailRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;

public class ChangePasswordOtpService {

    private final PasswordOtpRepository otpRepo = new PasswordOtpRepository();
    private final UserEmailRepository emailRepo = new UserEmailRepository();
    private final ChangePasswordRepository passRepo = new ChangePasswordRepository();
    private final EmailService emailService = new EmailService();

    private final SecureRandom rnd = new SecureRandom();

    public String sendOtpToUserEmail(String username) {
        try {
            String email = emailRepo.getEmailByUsername(username);
            if (email == null || email.isBlank())
                throw new RuntimeException("Tài khoản chưa có email. Vào Hồ sơ cá nhân cập nhật email trước.");

            // check state
            String state = passRepo.getState(username);
            if (state == null) throw new RuntimeException("Tài khoản không tồn tại.");
            if (!"ACTIVE".equalsIgnoreCase(state))
                throw new RuntimeException("Tài khoản đang ở trạng thái: " + state);

            String otp = String.format("%06d", rnd.nextInt(1_000_000));
            LocalDateTime exp = LocalDateTime.now().plusMinutes(5);

            otpRepo.upsertOtp(username, otp, exp);
            emailService.sendOtp(email, otp);

            return maskEmail(email);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Không thể gửi OTP: " + e.getMessage(), e);
        }
    }

    public void verifyOldPasswordOtpAndChangePassword(String username, String oldPassword, String otp, String newPassword) {
        try {
            if (newPassword == null || newPassword.isBlank())
                throw new IllegalArgumentException("Mật khẩu mới không được rỗng.");
            if (newPassword.length() < 6)
                throw new IllegalArgumentException("Mật khẩu mới phải >= 6 ký tự.");

            // state check
            String state = passRepo.getState(username);
            if (state == null) throw new RuntimeException("Tài khoản không tồn tại.");
            if (!"ACTIVE".equalsIgnoreCase(state))
                throw new RuntimeException("Tài khoản đang ở trạng thái: " + state);

            // old password check (BẮT BUỘC)
            boolean okOld = passRepo.verifyOldPassword(username, oldPassword);
            if (!okOld) throw new RuntimeException("Mật khẩu hiện tại không đúng.");

            // otp check
            var row = otpRepo.getOtp(username);
            if (row == null) throw new RuntimeException("Bạn chưa bấm gửi OTP hoặc OTP đã bị xoá.");

            if (java.time.LocalDateTime.now().isAfter(row.expiresAt()))
                throw new RuntimeException("OTP đã hết hạn. Hãy bấm gửi lại OTP.");

            if (otp == null || !otp.trim().equals(row.otp()))
                throw new RuntimeException("OTP không đúng.");

            // update
            int updated = passRepo.updatePassword(username, newPassword);
            if (updated != 1) throw new RuntimeException("Không cập nhật được mật khẩu.");

            otpRepo.deleteOtp(username);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Đổi mật khẩu thất bại: " + e.getMessage(), e);
        }
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return "***" + email.substring(at);
        return email.charAt(0) + "***" + email.substring(at - 1);
    }

    // Quên mật khẩu
    public void verifyOtpAndResetPassword(String username, String otp, String newPassword) {
        try {
            if (username == null || username.isBlank())
                throw new IllegalArgumentException("Username không được rỗng.");

            if (newPassword == null || newPassword.isBlank())
                throw new IllegalArgumentException("Mật khẩu mới không được rỗng.");
            if (newPassword.length() < 6)
                throw new IllegalArgumentException("Mật khẩu mới phải >= 6 ký tự.");

            // state check
            String state = passRepo.getState(username);
            if (state == null) throw new RuntimeException("Tài khoản không tồn tại.");
            if (!"ACTIVE".equalsIgnoreCase(state))
                throw new RuntimeException("Tài khoản đang ở trạng thái: " + state);

            // otp check
            var row = otpRepo.getOtp(username);
            if (row == null) throw new RuntimeException("Bạn chưa bấm gửi OTP hoặc OTP đã bị xoá.");

            if (java.time.LocalDateTime.now().isAfter(row.expiresAt()))
                throw new RuntimeException("OTP đã hết hạn. Hãy bấm gửi lại OTP.");

            if (otp == null || !otp.trim().equals(row.otp()))
                throw new RuntimeException("OTP không đúng.");

            // update password
            int updated = passRepo.updatePassword(username, newPassword);
            if (updated != 1) throw new RuntimeException("Không cập nhật được mật khẩu.");

            otpRepo.deleteOtp(username);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Quên mật khẩu thất bại: " + e.getMessage(), e);
        }
    }

}
