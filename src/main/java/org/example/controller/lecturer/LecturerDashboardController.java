package org.example.controller.lecturer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LecturerDashboardController {

    // Ánh xạ khu vực nội dung chính
    @FXML
    private VBox contentArea;

    // Ánh xạ các nút Sidebar
    @FXML
    private Button profileButton;
    @FXML
    private Button teachingScheduleButton;
    @FXML
    private Button classManagementButton;
    @FXML
    private Button scoreManagementButton; // Nút Active mặc định

    // Danh sách tất cả các nút Sidebar
    private List<Button> sidebarButtons;

    // Đường dẫn gốc tới các FXML con (View). CẦN CHỈNH SỬA CHO ĐÚNG THƯ MỤC CỦA BẠN!
    private static final String VIEW_PATH = "/app/lecturer/";
    private static final String ACTIVE_STYLE = "-fx-background-color: #00796B; -fx-text-fill: WHITE; -fx-font-weight: bold; -fx-font-size: 14;";
    private static final String INACTIVE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #E0E0E0; -fx-font-size: 14;";


    @FXML
    public void initialize() {
        // Khởi tạo danh sách các nút
        sidebarButtons = Arrays.asList(profileButton, teachingScheduleButton, classManagementButton, scoreManagementButton);

        // Tải View mặc định khi Dashboard được mở (Quản lý Điểm số)
        loadView("LecturerScoreView.fxml", scoreManagementButton);
    }

    // --- Các phương thức xử lý sự kiện (được gọi từ FXML) ---

    @FXML
    private void profileHandle() {
        loadView("LecturerProfileView.fxml", profileButton);
    }

    @FXML
    private void teachingScheduleHandle() {
        loadView("LecturerScheduleView.fxml", teachingScheduleButton);
    }

    @FXML
    private void classManagementHandle() {
        loadView("LecturerClassView.fxml", classManagementButton);
    }

    @FXML
    private void scoreManagementHandle() {
        loadView("LecturerScoreView.fxml", scoreManagementButton);
    }

    // ********** CƠ CHẾ TẢI VÀ CHUYỂN ĐỔI VIEW **********

    /**
     * Phương thức dùng chung để tải FXML con và thay thế nội dung, đồng thời cập nhật trạng thái Menu
     */
    private void loadView(String fxmlName, Button activeButton) {
        try {
            // 1. Cập nhật trạng thái Menu (Active State)
            updateSidebarStyles(activeButton);

            // 2. Lấy URL và kiểm tra Class Loader (Sử dụng cách đã sửa lỗi trước đó)
            String fullPath = VIEW_PATH + fxmlName;
            java.net.URL url = getClass().getResource(fullPath);

            if (url == null) {
                System.err.println("LỖI: Không tìm thấy Resource tại đường dẫn: " + fullPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load();

            // 3. Xóa nội dung cũ và thêm nội dung mới
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

        } catch (IOException e) {
            System.err.println("LỖI TẢI VIEW: Không thể load FXML: " + fxmlName);
            e.printStackTrace();
        }
    }

    /**
     * Cập nhật màu sắc cho các nút Sidebar
     */
    private void updateSidebarStyles(Button activeButton) {
        for (Button button : sidebarButtons) {
            button.setStyle(button == activeButton ? ACTIVE_STYLE : INACTIVE_STYLE);
        }
    }
}