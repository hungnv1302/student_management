package org.example.service;

import org.example.repository.ChangePasswordRepository;

import java.sql.SQLException;

public class ChangePasswordService {

    private final ChangePasswordRepository repo = new ChangePasswordRepository();

    public void changePassword(String username, String oldPassword, String newPassword) {
        try {
            if (username == null || username.isBlank())
                throw new IllegalArgumentException("Chưa đăng nhập.");

            if (newPassword == null || newPassword.isBlank())
                throw new IllegalArgumentException("Mật khẩu mới không được rỗng.");

            if (newPassword.length() < 6)
                throw new IllegalArgumentException("Mật khẩu mới phải >= 6 ký tự.");

            String state = repo.getState(username);
            if (state == null)
                throw new RuntimeException("Tài khoản không tồn tại.");

            if (!"ACTIVE".equalsIgnoreCase(state))
                throw new RuntimeException("Tài khoản đang ở trạng thái: " + state + " (không thể đổi mật khẩu).");

            boolean ok = repo.verifyOldPassword(username, oldPassword);
            if (!ok)
                throw new RuntimeException("Mật khẩu hiện tại không đúng.");

            int updated = repo.updatePassword(username, newPassword);
            if (updated != 1)
                throw new RuntimeException("Không cập nhật được mật khẩu.");

        } catch (RuntimeException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("DB error changePassword: " + e.getMessage(), e);
        }
    }
}
