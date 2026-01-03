package org.example.controller.lecturer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.dto.LecturerProfileDTO;
import org.example.service.LecturerProfileService;
import org.example.service.SessionContext;

public class LecturerProfileController {
    @FXML private Label lblLecturerId, lblFullName, lblDob, lblGender, lblDepartment, lblDegree, lblPosition;
    @FXML private TextField tfPhone, tfEmail;
    @FXML private TextArea taAddress;

    private final LecturerProfileService service = new LecturerProfileService();

    @FXML
    public void initialize() {
        reload();
    }

    @FXML
    private void saveHandle() {
        try {
            String id = SessionContext.getUsername();
            if (id == null) throw new IllegalStateException("Chưa đăng nhập.");
            service.updateContact(id, tfPhone.getText(), tfEmail.getText(), taAddress.getText());
            new Alert(Alert.AlertType.INFORMATION, "Đã cập nhật thông tin thành công!").showAndWait();
            reload();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Lỗi: " + e.getMessage()).showAndWait();
        }
    }

    private void reload() {
        String id = SessionContext.getUsername();
        if (id == null) return; // QUAN TRỌNG: Không báo lỗi popup nếu Session chưa sẵn sàng khi initialize

        try {
            LecturerProfileDTO d = service.loadProfile(id);
            if (d == null) return;

            lblLecturerId.setText(nvl(d.getLecturerId()));
            lblFullName.setText(nvl(d.getFullName()));
            lblDob.setText(d.getDob() == null ? "" : d.getDob().toString());
            lblGender.setText(nvl(d.getGender()));
            lblDepartment.setText(nvl(d.getDepartment()));
            lblDegree.setText(nvl(d.getDegree()));
            lblPosition.setText(nvl(d.getPosition()));

            tfPhone.setText(nvl(d.getPhoneNumber()));
            tfEmail.setText(nvl(d.getEmail()));
            taAddress.setText(nvl(d.getAddress()));
        } catch (Exception e) {
            System.err.println("Lỗi nạp dữ liệu: " + e.getMessage());
        }
    }

    private String nvl(String s) { return s == null ? "" : s; }
}