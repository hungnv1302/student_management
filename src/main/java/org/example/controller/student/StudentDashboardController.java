package org.example.controller.student;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class StudentDashboardController {

    @FXML private VBox contentArea;

    @FXML private Button profileButton;
    @FXML private Button registrationButton;
    @FXML private Button scheduleButton;
    @FXML private Button scoresButton;

    private List<Button> sidebarButtons;

    private static final String VIEW_PATH = "/app/student/";
    private static final String ACTIVE_STYLE =
            "-fx-background-color: #00796B; -fx-text-fill: WHITE; -fx-font-weight: bold; -fx-font-size: 14;";
    private static final String INACTIVE_STYLE =
            "-fx-background-color: transparent; -fx-text-fill: #E0E0E0; -fx-font-size: 14;";

    // ===== Context (sinh viên đăng nhập) =====
    private Long studentId;
    private String username;

    public void setContext(Long studentId, String username) {
        this.studentId = studentId;
        this.username = username;
    }

    @FXML
    public void initialize() {
        sidebarButtons = Arrays.asList(profileButton, registrationButton, scheduleButton, scoresButton);

        // ❌ KHÔNG load view mặc định ở đây nữa
        // vì initialize chạy trước khi Login kịp setContext()
        // loadView("StudentRegistrationView.fxml", registrationButton);
    }

    // ✅ gọi sau khi setContext() từ LoginController
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

    @FXML
    private void scheduleHandle() {
        loadView("StudentScheduleView.fxml", scheduleButton);
    }

    @FXML
    private void scoresHandle() {
        loadView("StudentScoresView.fxml", scoresButton);
    }

    private void loadView(String fxmlName, Button activeButton) {
        try {
            String fullPath = VIEW_PATH + fxmlName;

            java.net.URL url = Thread.currentThread()
                    .getContextClassLoader()
                    .getResource(fullPath.substring(1));

            if (url == null) {
                System.err.println("LỖI KHÔNG TÌM THẤY RESOURCE: " + fullPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load();

            // ✅ TRUYỀN CONTEXT XUỐNG VIEW CON NẾU NÓ CẦN
            Object childController = loader.getController();
            if (childController instanceof StudentViewContextAware ctxAware) {
                ctxAware.setContext(studentId, username);
            }

            updateSidebarStyles(activeButton);
            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            System.err.println("LỖI IO: Không thể load FXML: " + fxmlName);
            e.printStackTrace();
        }
    }

    private void updateSidebarStyles(Button activeButton) {
        for (Button button : sidebarButtons) {
            button.setStyle(button == activeButton ? ACTIVE_STYLE : INACTIVE_STYLE);
        }
    }
}
