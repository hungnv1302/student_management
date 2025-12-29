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
import org.example.repository.UserRepository;
import org.example.service.AuthService;
import org.example.service.SessionContext;
import org.example.service.exception.BusinessException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService(new UserRepository());

    @FXML
    public void loginHandle(ActionEvent event) {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        try {
            clearError();

            // ✅ AuthService.login sẽ tự set SessionContext.set(username, role)
            authService.login(username, password);

            // ✅ Lấy role từ session
            String role = SessionContext.getRole();
            if (role == null) {
                setError("Không lấy được role sau khi đăng nhập.");
                return;
            }

            switch (role.toUpperCase()) {
                case "LECTURER" -> switchScene(event, "/app/lecturer/LecturerScene.fxml", "Lecturer Dashboard");
                case "STUDENT" -> switchScene(event, "/app/student/StudentScene.fxml", "Student Dashboard");
                default -> setError("Role không hợp lệ: " + role);
            }

        } catch (BusinessException be) {
            setError(be.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            setError("Lỗi hệ thống!");
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
        if (errorLabel != null) errorLabel.setText(msg);
        else System.out.println("[LoginError] " + msg);
    }

    private void clearError() {
        if (errorLabel != null) errorLabel.setText("");
    }
}
