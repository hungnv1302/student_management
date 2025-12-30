package org.example.controller.student;
//
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.service.SessionContext;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class StudentDashboardController {

    // ===== CONTENT =====
    @FXML private VBox contentArea;

    // ===== SIDEBAR (menu buttons) =====
    @FXML private Button profileButton;
    @FXML private Button registrationButton;
    @FXML private Button scheduleButton;
    @FXML private Button scheduleOpenButton;
    @FXML private Button scoresButton;
    @FXML private Button changePasswordButton;

    // ===== SIDEBAR (toggle) =====
    @FXML private VBox sidebar;            // fx:id="sidebar"
    @FXML private VBox menuBox;            // fx:id="menuBox" (menu container)
    @FXML private VBox sidebarFooter;      // fx:id="sidebarFooter"
    @FXML private Label sidebarTitle;      // fx:id="sidebarTitle"
    @FXML private Button toggleSidebarBtn; // fx:id="toggleSidebarBtn"

    private List<Button> sidebarButtons;

    private boolean sidebarCollapsed = false;
    private static final double SIDEBAR_EXPANDED = 260;
    private static final double SIDEBAR_COLLAPSED = 68;

    private static final String VIEW_PATH = "/app/student/";
    private static final String ACTIVE_STYLE =
            "-fx-background-color: #00796B; -fx-text-fill: WHITE; -fx-font-weight: bold; -fx-font-size: 14;";
    private static final String INACTIVE_STYLE =
            "-fx-background-color: transparent; -fx-text-fill: #E0E0E0; -fx-font-size: 14;";

    @FXML
    public void initialize() {
        sidebarButtons = Arrays.asList(
                profileButton,
                registrationButton,
                scheduleButton,
                scheduleOpenButton,
                scoresButton,
                changePasswordButton
        );

        // Cho các button tự co giãn theo sidebar width
        for (Button b : sidebarButtons) {
            b.setMaxWidth(Double.MAX_VALUE);
        }

        if (!SessionContext.isLoggedIn() || !SessionContext.isStudent()) {
            System.err.println("[StudentDashboard] Chưa đăng nhập hoặc không phải STUDENT!");
        }

        // Set icon nút toggle ban đầu
        if (toggleSidebarBtn != null) {
            toggleSidebarBtn.setText("☰");
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

    // ===== Toggle Sidebar (Thu gọn/Mở rộng) =====
    @FXML
    private void toggleSidebarHandle() {
        // target width
        double target = sidebarCollapsed ? SIDEBAR_EXPANDED : SIDEBAR_COLLAPSED;

        Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(220),
                        new KeyValue(sidebar.prefWidthProperty(), target, Interpolator.EASE_BOTH),
                        new KeyValue(sidebar.minWidthProperty(), target, Interpolator.EASE_BOTH),
                        new KeyValue(sidebar.maxWidthProperty(), target, Interpolator.EASE_BOTH)
                )
        );
        tl.play();

        boolean willExpand = sidebarCollapsed; // đang collapsed -> sẽ expand

        // Ẩn/hiện title (SINH VIÊN)
        if (sidebarTitle != null) {
            sidebarTitle.setVisible(willExpand);
            sidebarTitle.setManaged(willExpand);
        }

        // Nếu muốn thu gọn mà ẩn luôn menu/footer thì bật 2 block này:
        // if (menuBox != null) { menuBox.setVisible(willExpand); menuBox.setManaged(willExpand); }
        // if (sidebarFooter != null) { sidebarFooter.setVisible(willExpand); sidebarFooter.setManaged(willExpand); }

        // đổi icon nút toggle
        if (toggleSidebarBtn != null) {
            toggleSidebarBtn.setText(willExpand ? "←" : "☰");
        }

        sidebarCollapsed = !sidebarCollapsed;
    }

    // ===== Load view into content area =====
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

    // ===== Logout =====
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
