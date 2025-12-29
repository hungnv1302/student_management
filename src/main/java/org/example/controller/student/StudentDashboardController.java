package org.example.controller.student;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.service.SessionContext;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class StudentDashboardController {

    @FXML private VBox contentArea;

    @FXML private Button profileButton;
    @FXML private Button registrationButton;
    @FXML private Button scheduleButton;
    @FXML private Button scheduleOpenButton; // ✅ thêm nút “TKB tạm thời”
    @FXML private Button scoresButton;
    @FXML private Button changePasswordButton;

    private List<Button> sidebarButtons;

    private static final String VIEW_PATH = "/app/student/";
    private static final String ACTIVE_STYLE =
            "-fx-background-color: #00796B; -fx-text-fill: WHITE; -fx-font-weight: bold; -fx-font-size: 14;";
    private static final String INACTIVE_STYLE =
            "-fx-background-color: transparent; -fx-text-fill: #E0E0E0; -fx-font-size: 14;";

    @FXML
    public void initialize() {
        sidebarButtons = Arrays.asList(profileButton, registrationButton, scheduleButton, scheduleOpenButton, scoresButton);

        if (!SessionContext.isLoggedIn() || !SessionContext.isStudent()) {
            System.err.println("[StudentDashboard] Chưa đăng nhập hoặc không phải STUDENT!");
        }
    }

    public void openDefaultView() {
        loadView("StudentRegistrationView.fxml", registrationButton);
    }

    @FXML
    private void profileHandle() {
        loadView("StudentProfileView.fxml", profileButton);
    }

    @FXML
    private void registrationHandle() {
        loadView("StudentRegistrationView.fxml", registrationButton);
    }

    /** ✅ TKB kỳ đang học */
    @FXML
    private void scheduleHandle() {
        loadView("StudentScheduleView.fxml", scheduleButton);
    }

    /** ✅ TKB tạm thời (kỳ đang mở đăng ký) */
    @FXML
    private void scheduleOpenHandle() {
        loadView("StudentScheduleOpenView.fxml", scheduleOpenButton);
    }

    @FXML
    private void scoresHandle() {
        loadView("StudentScoresView.fxml", scoresButton);
    }

    @FXML
    private void changePasswordHandle() {
        loadView("StudentChangePasswordView.fxml", changePasswordButton);
    }

    private void loadView(String fxmlName, Button activeButton) {
        String fullPath = VIEW_PATH + fxmlName;

        try {
            URL url = getClass().getResource(fullPath);
            if (url == null) {
                showError("Không tìm thấy FXML", "Không tìm thấy resource:\n" + fullPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load();

            updateSidebarStyles(activeButton);
            contentArea.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Không thể mở màn hình",
                    "Lỗi khi load: " + fullPath + "\n\n" + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private void updateSidebarStyles(Button activeButton) {
        for (Button button : sidebarButtons) {
            button.setStyle(button == activeButton ? ACTIVE_STYLE : INACTIVE_STYLE);
        }
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    @FXML
    private void logoutHandle(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận đăng xuất");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn đăng xuất hay không?");

        ButtonType yesButton = new ButtonType("Có", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Không", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != yesButton) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/auth/LoginScene.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Đăng nhập");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Lỗi đăng xuất", e.getMessage());
        }
    }
}
