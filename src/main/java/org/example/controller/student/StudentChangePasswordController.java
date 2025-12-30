package org.example.controller.student;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.service.ChangePasswordOtpService;
import org.example.service.SessionContext;

public class StudentChangePasswordController {

    @FXML private Label lblInfo;
    @FXML private Label lblSentTo;
    @FXML private TextField tfOtp;
    @FXML private PasswordField pfOld;
    @FXML private PasswordField pfNew;
    @FXML private PasswordField pfConfirm;
    @FXML private Button btnSendOtp;

    private final ChangePasswordOtpService service = new ChangePasswordOtpService();

    private String usernameOrThrow() {
        String u = SessionContext.getUsername();
        if (u == null || u.isBlank()) throw new IllegalStateException("Chưa đăng nhập.");
        return u.trim();
    }

    @FXML
    private void sendOtpHandle() {
        try {
            String username = usernameOrThrow();
            String masked = service.sendOtpToUserEmail(username);
            lblSentTo.setText("Đã gửi tới: " + masked);
            info("Đã gửi OTP", "Vui lòng kiểm tra email và nhập OTP trong 5 phút.");
        } catch (Exception e) {
            error("Không gửi được OTP", e.getMessage());
        }
    }

    @FXML
    private void changePasswordHandle() {
        try {
            String old = pfOld.getText() == null ? "" : pfOld.getText();
            String otp = tfOtp.getText() == null ? "" : tfOtp.getText().trim();
            String p1 = pfNew.getText() == null ? "" : pfNew.getText();
            String p2 = pfConfirm.getText() == null ? "" : pfConfirm.getText();

            if (old.isBlank()) {
                info("Thiếu thông tin", "Vui lòng nhập mật khẩu hiện tại.");
                return;
            }

            if (!otp.matches("\\d{6}")) {
                info("OTP không hợp lệ", "OTP phải gồm đúng 6 chữ số.");
                return;
            }

            if (!p1.equals(p2)) {
                info("Chưa khớp", "Mật khẩu nhập lại không khớp.");
                return;
            }

            service.verifyOldPasswordOtpAndChangePassword(usernameOrThrow(), old, otp, p1);

            pfOld.clear(); tfOtp.clear(); pfNew.clear(); pfConfirm.clear();
            lblSentTo.setText("");
            info("Thành công", "Đã đổi mật khẩu.");
        } catch (Exception e) {
            error("Không thể đổi mật khẩu", e.getMessage());
        }
    }


    private void info(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void error(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
