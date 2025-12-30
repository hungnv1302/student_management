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
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import org.example.repository.UserRepository;
import org.example.service.AuthService;
import org.example.service.SessionContext;
import org.example.service.exception.BusinessException;

public class LoginController {

    @FXML private TextField usernameField;

    // Ẩn/hiện mật khẩu
    @FXML private PasswordField passwordField;      // chế độ ẨN
    @FXML private TextField passwordTextField;      // chế độ HIỆN
    @FXML private ToggleButton showPasswordToggle;  // cần gạt

    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService(new UserRepository());

    @FXML
    public void initialize() {
        // Đồng bộ text 2 chiều giữa 2 ô mật khẩu
        if (passwordTextField != null && passwordField != null) {
            passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        }

        // Mặc định: ẩn mật khẩu
        setPasswordVisible(false);

        // Mặc định: ẩn lỗi
        clearError();
    }

    @FXML
    public void loginHandle(ActionEvent event) {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        try {
            clearError();

            authService.login(username, password);

            String role = SessionContext.getRole();
            if (role == null) {
                setError("Không lấy được role sau khi đăng nhập.");
                return;
            }

            switch (role.toUpperCase()) {
                case "LECTURER" -> switchScene(event, "/app/lecturer/LecturerScene.fxml", "Lecturer Dashboard");
                case "STUDENT"  -> switchScene(event, "/app/student/StudentScene.fxml", "Student Dashboard");
                default         -> setError("Role không hợp lệ: " + role);
            }

        } catch (BusinessException be) {
            setError(be.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            setError("Lỗi hệ thống!");
        }
    }

    // ===== Toggle switch handler =====
    @FXML
    private void togglePassword(ActionEvent event) {
        boolean show = showPasswordToggle != null && showPasswordToggle.isSelected();
        setPasswordVisible(show);
    }

    // ===== Helpers =====
    private void setPasswordVisible(boolean visible) {
        // visible = true => hiện TextField, ẩn PasswordField
        if (passwordTextField != null) {
            passwordTextField.setVisible(visible);
            passwordTextField.setManaged(visible);
        }
        if (passwordField != null) {
            passwordField.setVisible(!visible);
            passwordField.setManaged(!visible);
        }
    }

    @FXML
    private void forgotPasswordHandle(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/app/auth/ForgotPasswordView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Quên mật khẩu");
            stage.centerOnScreen();
        } catch (Exception e) {
            errorLabel.setText("Không mở được màn quên mật khẩu: " + e.getMessage());
        }
    }

    private void switchScene(ActionEvent event, String fxmlPath, String title) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void setError(String msg) {
        if (errorLabel != null) {
            errorLabel.setManaged(true);
            errorLabel.setVisible(true);
            errorLabel.setText(msg);
        }
    }

    private void clearError() {
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }
}
