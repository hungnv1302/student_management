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
import org.example.service.ChangePasswordOtpService;

public class ForgotPasswordController {

    @FXML private Label lblInfo;
    @FXML private Label lblSentTo;
    @FXML private Label lblError;

    @FXML private TextField tfUsername;
    @FXML private TextField tfOtp;
    @FXML private PasswordField pfNew;
    @FXML private PasswordField pfConfirm;

    private final ChangePasswordOtpService service = new ChangePasswordOtpService();

    @FXML
    private void sendOtpHandle() {
        try {
            clearError();
            String username = safe(tfUsername.getText());

            if (username.isBlank()) {
                showError("Vui lòng nhập username.");
                return;
            }

            String masked = service.sendOtpToUserEmail(username);
            lblSentTo.setText("Đã gửi tới: " + masked);
            lblInfo.setText("OTP có hiệu lực 5 phút. Nhập OTP + mật khẩu mới để đặt lại.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void confirmHandle() {
        try {
            clearError();

            String username = safe(tfUsername.getText());
            String otp = safe(tfOtp.getText());
            String p1 = pfNew.getText() == null ? "" : pfNew.getText();
            String p2 = pfConfirm.getText() == null ? "" : pfConfirm.getText();

            if (username.isBlank()) { showError("Username không được rỗng."); return; }
            if (!otp.matches("\\d{6}")) { showError("OTP phải gồm đúng 6 chữ số."); return; }

            if (p1.isBlank()) { showError("Mật khẩu mới không được rỗng."); return; }
            if (!p1.equals(p2)) { showError("Mật khẩu nhập lại không khớp."); return; }

            // ✅ quên mật khẩu: CHỈ cần OTP + mật khẩu mới
            service.verifyOtpAndResetPassword(username, otp, p1);

            lblInfo.setText("Thành công! Bạn có thể quay lại đăng nhập bằng mật khẩu mới.");
            tfOtp.clear(); pfNew.clear(); pfConfirm.clear();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void backToLoginHandle(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/app/auth/LoginScene.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Đăng nhập");
            stage.centerOnScreen();
        } catch (Exception e) {
            showError("Không quay lại được: " + e.getMessage());
        }
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }

    private void showError(String msg) {
        lblError.setText(msg == null ? "Có lỗi xảy ra." : msg);
    }

    private void clearError() {
        lblError.setText("");
    }
}
