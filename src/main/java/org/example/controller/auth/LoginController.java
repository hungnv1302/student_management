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
import org.example.controller.student.StudentDashboardController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
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

            UserRow u = findByUsername(username);
            if (u == null) {
                setError("Sai tài khoản hoặc mật khẩu.");
                return;
            }

            if (u.state != null && u.state.equalsIgnoreCase("LOCKED")) {
                setError("Tài khoản bị khóa.");
                return;
            }

            if (u.password == null || !u.password.equals(password)) {
                setError("Sai tài khoản hoặc mật khẩu.");
                return;
            }

            updateLastLogin(u.userId);

            String role = (u.role == null) ? "" : u.role.trim().toUpperCase();
            switch (role) {
                case "ADMIN" -> switchScene(event, "/app/admin/AdminScene.fxml", "Admin Dashboard", u);
                case "LECTURER" -> switchScene(event, "/app/lecturer/LecturerScene.fxml", "Lecturer Dashboard", u);
                case "STUDENT" -> switchScene(event, "/app/student/StudentScene.fxml", "Student Dashboard", u);
                default -> setError("Role không hợp lệ: " + u.role);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            setError("Lỗi hệ thống!");
        }
    }

    private UserRow findByUsername(String username) throws Exception {
        String sql = """
            SELECT user_id, username, password, role, state
            FROM public.users
            WHERE username = ?
        """;

        try (Connection c = DbConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                UserRow u = new UserRow();
                u.userId = rs.getString("user_id");   // ⚠️ giả định user_id chính là MSV dạng số
                u.username = rs.getString("username");
                u.password = rs.getString("password");
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
        } catch (Exception ignored) {}
    }

    private static class UserRow {
        String userId;
        String username;
        String password;
        String role;
        String state;
    }

    // ✅ SỬA Ở ĐÂY: dùng FXMLLoader để lấy controller và truyền context
    private void switchScene(ActionEvent event, String fxmlPath, String title, UserRow u) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        // ✅ Nếu là STUDENT dashboard thì truyền studentId/username
        if ("/app/student/StudentScene.fxml".equals(fxmlPath)) {
            StudentDashboardController dash = loader.getController();

            // studentId = MSV 8 chữ số (số) -> parse từ user_id
            Long studentId = Long.parseLong(u.userId);

            dash.setContext(studentId, u.username);
            dash.openDefaultView(); // load view mặc định sau khi đã có context
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void setError(String msg) {
        if (errorLabel != null) errorLabel.setText(msg);
        else System.out.println("[LoginError] " + msg);
    }

    private void clearError() {
        if (errorLabel != null) errorLabel.setText("");
    }
}
