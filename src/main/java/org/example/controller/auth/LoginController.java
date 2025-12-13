package org.example.controller.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.config.DbConfig;
import org.example.service.security.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    // login.fxml của cháu chưa có errorLabel -> để optional
    @FXML private Label errorLabel;

    @FXML
    public void loginHandle(ActionEvent event) {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        try {
            clearError();

            if (username.isEmpty() || password.isEmpty()) {
                setError("Vui lòng nhập đầy đủ tài khoản và mật khẩu.");
                return;
            }

            // 1) Lấy dữ liệu user từ DB
            UserRow u = findByUsername(username);
            if (u == null) {
                setError("Sai tài khoản hoặc mật khẩu.");
                return;
            }

            // 2) Check trạng thái
            if (u.state != null && u.state.equalsIgnoreCase("LOCKED")) {
                setError("Tài khoản bị khóa.");
                return;
            }

            // 3) Verify password (PBKDF2, KHỚP seed)
            boolean ok = PasswordUtil.verify(password.toCharArray(), u.passwordSalt, u.passwordHash);
            if (!ok) {
                setError("Sai tài khoản hoặc mật khẩu.");
                return;
            }

            // 4) Update last_login (tuỳ chọn)
            updateLastLogin(u.userId);

            // 5) Điều hướng theo role
            switch (u.role) {
                case "ADMIN" -> switchScene(event, "/app/admin/AdminScene.fxml", "Admin Dashboard");
                case "LECTURER" -> switchScene(event, "/app/lecturer/LecturerScene.fxml", "Lecturer Dashboard");
                case "STUDENT" -> switchScene(event, "/app/student/StudentScene.fxml", "Student Dashboard");
                default -> setError("Role không hợp lệ: " + u.role);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            setError("Lỗi hệ thống!");
        }
    }

    // ===================== DB =====================

    private UserRow findByUsername(String username) throws Exception {
        String sql = """
            SELECT user_id, username, password_hash, password_salt, role, state
            FROM public.users
            WHERE username = ?
        """;

        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                UserRow u = new UserRow();
                u.userId = rs.getString("user_id");
                u.username = rs.getString("username");
                u.passwordHash = rs.getString("password_hash");
                u.passwordSalt = rs.getString("password_salt");
                u.role = rs.getString("role");
                u.state = rs.getString("state");
                return u;
            }
        }
    }

    private void updateLastLogin(String userId) {
        String sql = "UPDATE public.users SET last_login = NOW() WHERE user_id = ?";
        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.executeUpdate();
        } catch (Exception ignored) {
            // không cho last_login làm crash login
        }
    }

    private static class UserRow {
        String userId;
        String username;
        String passwordHash;
        String passwordSalt;
        String role;
        String state;
    }

    // ===================== Scene =====================

    private void switchScene(ActionEvent event, String fxmlPath, String title) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.show();
    }

    // ===================== UI helpers =====================

    private void setError(String msg) {
        if (errorLabel != null) errorLabel.setText(msg);
        else System.out.println("[LoginError] " + msg);
    }

    private void clearError() {
        if (errorLabel != null) errorLabel.setText("");
    }
}
