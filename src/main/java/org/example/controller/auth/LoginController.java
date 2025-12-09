package org.example.controller.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.domain.User;
import org.example.repository.Database;
import org.example.service.AuthService;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private AuthService authService;

    @FXML
    public void initialize() {
        // Khởi tạo "cơ sở dữ liệu" giả và service đăng nhập
        authService = new AuthService(new Database());
    }

    @FXML
    private void handleButtonAction(ActionEvent event) {
        String username = usernameField != null ? usernameField.getText() : null;
        String password = passwordField != null ? passwordField.getText() : null;

        // Kiểm tra nhập trống
        if (username == null || username.isBlank() ||
                password == null || password.isBlank()) {
            showError("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
            return;
        }

        // Gọi service kiểm tra đăng nhập
        User user = authService.login(username, password);

        if (user == null) {
            // Sai tài khoản / mật khẩu
            showError("Sai tên đăng nhập hoặc mật khẩu!");
        } else {
            // Đăng nhập thành công
            showInfo("Đăng nhập thành công với vai trò: " + user.getRole());

            // TODO: ở đây sau này bạn chuyển sang màn hình chính tuỳ role
            // ví dụ: openMainScreen(user, event);
        }
    }

    @FXML
    private void handleForgetPasswordButton(ActionEvent event) {
        System.out.println(">>> handleForgetPasswordButton called"); // debug

        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/app/auth/forgetPassword.fxml")
            );

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Quên mật khẩu");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Đăng nhập thất bại");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
