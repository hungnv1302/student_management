package org.example.service;

import org.example.dto.UserDTO;
import org.example.repository.UserRepository;
import org.example.service.exception.BusinessException;

import java.sql.SQLException;
import java.util.UUID;

public class AuthService {

    private final UserRepository userRepo;
    private UserDTO currentUser;

    public AuthService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    // ================= LOGIN =================
    public void login(String username, String password) throws SQLException {
        if (username == null || username.isBlank())
            throw new BusinessException("Vui lòng nhập tài khoản");
        if (password == null || password.isBlank())
            throw new BusinessException("Vui lòng nhập mật khẩu");

        var opt = userRepo.findByUsername(username.trim());
        if (opt.isEmpty())
            throw new BusinessException("Sai tài khoản hoặc mật khẩu");

        var u = opt.get();

        if ("LOCKED".equalsIgnoreCase(u.state()))
            throw new BusinessException("Tài khoản bị khóa");

        if (u.password() == null || !u.password().equals(password))
            throw new BusinessException("Sai tài khoản hoặc mật khẩu");

        userRepo.updateLastLogin(u.username());

        currentUser = u;

        // ✅ SET SESSION (CHUẨN)
        SessionContext.set(u.username(), u.role());
    }

    // ================= LOGOUT =================
    public void logout() {
        currentUser = null;
        SessionContext.clear();
    }

    // ================= CHANGE PASSWORD =================
    public void changePassword(String oldPw, String newPw) throws SQLException {
        if (currentUser == null)
            throw new BusinessException("Chưa đăng nhập");

        if (oldPw == null || oldPw.isBlank())
            throw new BusinessException("Mật khẩu cũ trống");

        if (newPw == null || newPw.length() < 3)
            throw new BusinessException("Mật khẩu mới quá ngắn");

        var opt = userRepo.findByUsername(currentUser.username());
        if (opt.isEmpty())
            throw new BusinessException("User không tồn tại");

        var u = opt.get();
        if (u.password() == null || !u.password().equals(oldPw))
            throw new BusinessException("Mật khẩu cũ không đúng");

        userRepo.updatePassword(u.username(), newPw);

        // refresh currentUser
        currentUser = new UserDTO(
                u.username(),
                newPw,
                u.role(),
                u.state()
        );

        // cập nhật lại session (username, role không đổi)
        SessionContext.set(currentUser.username(), currentUser.role());
    }

    // ================= FORGOT PASSWORD =================
    public String forgotPasswordGenerateTemp(String username) throws SQLException {
        if (username == null || username.isBlank())
            throw new BusinessException("Username trống");

        var opt = userRepo.findByUsername(username.trim());
        if (opt.isEmpty())
            throw new BusinessException("Không tìm thấy username");

        String temp = "Tmp@" + UUID.randomUUID().toString().substring(0, 8);
        userRepo.updatePassword(opt.get().username(), temp);
        return temp;
    }

    // ================= GETTER =================
    public UserDTO getCurrentUser() {
        return currentUser;
    }
}
