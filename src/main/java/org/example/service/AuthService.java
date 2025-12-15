package org.example.service;

import org.example.repository.UserRepository;
import org.example.service.exception.BusinessException;

import java.sql.SQLException;
import java.util.UUID;

public class AuthService {

    private final UserRepository userRepo;

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

        String pwDb = u.password();
        if (pwDb == null || !pwDb.equals(password)) {
            throw new BusinessException("Sai tài khoản hoặc mật khẩu");
        }

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
        if (oldPw == null || oldPw.isBlank()) throw new BusinessException("Mật khẩu cũ trống");
        if (newPw == null || newPw.length() < 3) throw new BusinessException("Mật khẩu mới quá ngắn");

        var opt = userRepo.findByUserId(currentUserId);
        if (opt.isEmpty()) throw new BusinessException("User không tồn tại");

        var u = opt.get();
        if (u.password() == null || !u.password().equals(oldPw)) {
            throw new BusinessException("Mật khẩu cũ không đúng");
        }

        userRepo.updatePassword(currentUserId, newPw);
    }

    public String forgotPasswordGenerateTemp(String username) throws SQLException {
        if (username == null || username.isBlank()) throw new BusinessException("Username trống");

        var opt = userRepo.findByUsername(username.trim());
        if (opt.isEmpty()) throw new BusinessException("Không tìm thấy username");

        String temp = "Tmp@" + UUID.randomUUID().toString().substring(0, 8);
        userRepo.updatePassword(opt.get().userId(), temp);
        return temp;
    }

    public String getCurrentUserId() { return currentUserId; }
    public String getCurrentRole() { return currentRole; }
}
