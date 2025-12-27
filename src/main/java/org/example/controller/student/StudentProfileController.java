package org.example.controller.student;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.example.dto.StudentProfileDTO;
import org.example.service.SessionContext;
import org.example.service.StudentProfileService;

public class StudentProfileController {

    // ===== Fixed fields (read-only) =====
    @FXML private Label lblStudentId;
    @FXML private Label lblFullName;
    @FXML private Label lblDob;
    @FXML private Label lblGender;

    @FXML private Label lblDepartment;
    @FXML private Label lblMajor;
    @FXML private Label lblClassName;
    @FXML private Label lblAdmissionYear;
    @FXML private Label lblTrainingScore;
    @FXML private Label lblStatus;

    // ===== Editable contact fields =====
    @FXML private TextField tfPhone;
    @FXML private TextField tfEmail;
    @FXML private TextArea taAddress;

    @FXML private Button btnSave;

    private final StudentProfileService service = new StudentProfileService();

    private String getStudentIdOrThrow() {
        String u = SessionContext.getUsername();
        if (u == null || u.isBlank()) throw new IllegalStateException("Chưa đăng nhập (SessionContext.username null).");
        return u.trim();
    }

    @FXML
    public void initialize() {
        reload();
    }

    @FXML
    private void saveHandle() {
        try {
            String studentId = getStudentIdOrThrow();

            service.updateContact(
                    studentId,
                    tfPhone.getText(),
                    tfEmail.getText(),
                    taAddress.getText()
            );

            info("Thành công", "Đã cập nhật thông tin liên hệ.");
            reload(); // load lại từ DB
        } catch (RuntimeException ex) {
            error("Không thể cập nhật", ex.getMessage());
        } catch (Exception ex) {
            error("Lỗi hệ thống", ex.getMessage());
        }
    }

    private void reload() {
        try {
            String studentId = getStudentIdOrThrow();
            StudentProfileDTO d = service.loadProfile(studentId);

            lblStudentId.setText(nvl(d.getStudentId()));
            lblFullName.setText(nvl(d.getFullName()));
            lblDob.setText(d.getDob() == null ? "" : d.getDob().toString());
            lblGender.setText(nvl(d.getGender()));

            lblDepartment.setText(nvl(d.getDepartment()));
            lblMajor.setText(nvl(d.getMajor()));
            lblClassName.setText(nvl(d.getClassName()));
            lblAdmissionYear.setText(d.getAdmissionYear() == null ? "" : String.valueOf(d.getAdmissionYear()));
            lblTrainingScore.setText(d.getTrainingScore() == null ? "" : String.valueOf(d.getTrainingScore()));
            lblStatus.setText(nvl(d.getStatus()));

            tfPhone.setText(nvl(d.getPhoneNumber()));
            tfEmail.setText(nvl(d.getEmail()));
            taAddress.setText(nvl(d.getAddress()));

        } catch (RuntimeException ex) {
            error("Lỗi tải hồ sơ", ex.getMessage());
        } catch (Exception ex) {
            error("Lỗi hệ thống", ex.getMessage());
        }
    }

    private static String nvl(String s) { return s == null ? "" : s; }

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
