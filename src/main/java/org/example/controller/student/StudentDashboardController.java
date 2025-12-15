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

    // Ánh xạ khu vực nội dung chính
    @FXML
    private VBox contentArea;

    // Ánh xạ các nút Sidebar
    @FXML
    private Button profileButton;
    @FXML
    private Button registrationButton;
    @FXML
    private Button scheduleButton;
    @FXML
    private Button scoresButton;

    // Danh sách tất cả các nút Sidebar để quản lý trạng thái Active
    private List<Button> sidebarButtons;

    // Đường dẫn gốc tới các FXML con (View)
    private static final String VIEW_PATH = "/app/student/";
    private static final String ACTIVE_STYLE = "-fx-background-color: #00796B; -fx-text-fill: WHITE; -fx-font-weight: bold; -fx-font-size: 14;";
    private static final String INACTIVE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #E0E0E0; -fx-font-size: 14;";


    @FXML
    public void initialize() {
        // Khởi tạo danh sách các nút
        sidebarButtons = Arrays.asList(profileButton, registrationButton, scheduleButton, scoresButton);

        // Tải View mặc định khi Dashboard được mở (Đã thiết lập là Đăng ký Học phần)
        loadView("StudentRegistrationView.fxml", registrationButton);
    }

    // --- Các phương thức xử lý sự kiện (được gọi từ FXML) ---

    @FXML
    private void profileHandle() {
        // Tên file FXML con cho Hồ sơ
        loadView("StudentProfileView.fxml", profileButton);
    }

    @FXML
    private void registrationHandle() {
        // Tên file FXML con cho Đăng ký Học phần (View mặc định)
        loadView("StudentRegistrationView.fxml", registrationButton);
    }

    @FXML
    private void scheduleHandle() {
        // Tên file FXML con cho Thời khóa biểu
        loadView("StudentScheduleView.fxml", scheduleButton);
    }

    @FXML
    private void scoresHandle() {
        // Tên file FXML con cho Kết quả Học tập
        loadView("StudentScoresView.fxml", scoresButton);
    }

    // ********** CƠ CHẾ TẢI VÀ CHUYỂN ĐỔI VIEW CHÍNH **********

    /**
     * Phương thức dùng chung để tải FXML con và thay thế nội dung, đồng thời cập nhật trạng thái Menu
     * @param fxmlName Tên file FXML con (ví dụ: StudentProfileView.fxml)
     * @param activeButton Nút vừa được nhấn (để tô màu Active)
     */
    private void loadView(String fxmlName, Button activeButton) {
        try {
            String fullPath = VIEW_PATH + fxmlName;

            // *** PHẦN THAY ĐỔI CẦN THIẾT ***
            // Sử dụng Context Class Loader để tìm resource, tránh lỗi 'Location is not set'
            java.net.URL url = Thread.currentThread().getContextClassLoader().getResource(fullPath.substring(1));

            // Hoặc thử cách này nếu cách trên không được (thêm dấu '/' ở đầu nếu cần)
            // java.net.URL url = getClass().getResource(fullPath);
            // ********************************

            if (url == null) {
                System.err.println("LỖI KHÔNG TÌM THẤY RESOURCE");
                System.err.println("Đường dẫn thử: " + fullPath);
                // Bạn có thể hiển thị một label báo lỗi lên contentArea tại đây
                return;
            }

            // 1. Tải View
            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load();

            // 2. Cập nhật Sidebar và View
            updateSidebarStyles(activeButton);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

        } catch (IOException e) {
            System.err.println("LỖI IO: Không thể load FXML: " + fxmlName);
            e.printStackTrace();
        }
    }

    /**
     * Cập nhật màu sắc cho các nút Sidebar: Đặt lại tất cả về inactive, sau đó tô sáng nút active
     * @param activeButton Nút cần được tô sáng
     */
    private void updateSidebarStyles(Button activeButton) {
        for (Button button : sidebarButtons) {
            button.setStyle(button == activeButton ? ACTIVE_STYLE : INACTIVE_STYLE);
        }
    }

    // Có thể thêm logic xử lý cho nút Hủy Đăng ký (cancelRegistrationButton) tại đây
    // @FXML
    // private void cancelRegistrationHandle() {
    //     // Logic xử lý hủy đăng ký học phần
    // }
}