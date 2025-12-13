package org.example.service;

import org.example.repository.UserRepository;
import org.example.service.exception.BusinessException;
import org.example.service.security.PasswordUtil;

import java.sql.SQLException;
import java.util.UUID;

public class AuthService {

    private final UserRepository userRepo;

    // session đơn giản (sau này có thể tách SessionContext)
    private String currentUserId;
    private String currentRole;

    public AuthService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public boolean login(String username, String password) throws SQLException {
        if (username == null || username.isBlank()) throw new BusinessException("Username trống");
        if (password == null || password.isBlank()) throw new BusinessException("Password trống");

        var opt = userRepo.findByUsername(username.trim());
        if (opt.isEmpty()) throw new BusinessException("Sai tài khoản hoặc mật khẩu");

        var u = opt.get();
        if ("LOCKED".equalsIgnoreCase(u.state())) throw new BusinessException("Tài khoản bị khóa");

        boolean ok = PasswordUtil.verify(password.toCharArray(), u.passwordSalt(), u.passwordHash());
        if (!ok) throw new BusinessException("Sai tài khoản hoặc mật khẩu");

        userRepo.updateLastLogin(u.userId());

        currentUserId = u.userId();
        currentRole = u.role();
        return true;
    }

    public void logout() {
        currentUserId = null;
        currentRole = null;
    }

    public void changePassword(String oldPw, String newPw) throws SQLException {
        if (currentUserId == null) throw new BusinessException("Chưa đăng nhập");
        if (newPw == null || newPw.length() < 6) throw new BusinessException("Mật khẩu mới quá ngắn");

        // cần load user hiện tại để verify old
        // (đơn giản: find by username không có, nên dùng findByUsername trong UI hoặc viết findById)
        throw new BusinessException("Chưa có findById. Bác sẽ thêm nếu cháu muốn dùng changePassword.");
    }

    // Quên mật khẩu: bản tối thiểu (chưa gửi email)
    public String forgotPasswordGenerateTemp(String username) throws SQLException {
        var opt = userRepo.findByUsername(username.trim());
        if (opt.isEmpty()) throw new BusinessException("Không tìm thấy username");

        // tạo mật khẩu tạm
        String temp = "Tmp@" + UUID.randomUUID().toString().substring(0, 8);

        String salt = PasswordUtil.newSaltBase64();
        String hash = PasswordUtil.hashPasswordBase64(temp.toCharArray(), salt);

        userRepo.updatePassword(opt.get().userId(), hash, salt);
        return temp; // UI có thể show/hoặc gửi mail sau
    }

    public String getCurrentUserId() { return currentUserId; }
    public String getCurrentRole() { return currentRole; }
}
