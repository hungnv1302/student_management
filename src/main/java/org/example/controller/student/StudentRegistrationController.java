package org.example.controller.student;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.example.dto.EnrolledRow;
import org.example.dto.OpenClassRow;
import org.example.service.StudentRegistrationService;

import java.util.List;

public class StudentRegistrationController implements StudentViewContextAware {

    // ===== Table 1: open classes =====
    @FXML private TableView<OpenClassRow> openClassesTable;
    @FXML private TableColumn<OpenClassRow, String>  colOpenSubjectCode;
    @FXML private TableColumn<OpenClassRow, String>  colOpenSubjectName;
    @FXML private TableColumn<OpenClassRow, Integer> colOpenCredits;
    @FXML private TableColumn<OpenClassRow, String>  colOpenLecturer;
    @FXML private TableColumn<OpenClassRow, Void>    colOpenAction;

    // ===== Table 2: enrolled =====
    @FXML private TableView<EnrolledRow> enrolledTable;
    @FXML private TableColumn<EnrolledRow, String> colEnrolledClassCode;
    @FXML private TableColumn<EnrolledRow, String> colEnrolledSubject;
    @FXML private TableColumn<EnrolledRow, String> colEnrolledTime;
    @FXML private TableColumn<EnrolledRow, String> colEnrolledStatus;
    @FXML private TableColumn<EnrolledRow, Void>   colEnrolledAction;

    @FXML private Button cancelRegistrationButton;

    // data
    private final ObservableList<OpenClassRow> openData = FXCollections.observableArrayList();
    private final ObservableList<EnrolledRow> enrolledData = FXCollections.observableArrayList();

    // context
    private Long studentId;
    private String username;

    // service (tạm tạo stub, lát cháu thay bằng service thật)
    private final StudentRegistrationService service = new StudentRegistrationService();

    @FXML
    public void initialize() {
        bindTables();
        wireButtons();

        openClassesTable.setItems(openData);
        enrolledTable.setItems(enrolledData);

        // Lưu ý: studentId chưa có ở đây nếu cháu truyền context sau khi load view.
        // Khi setContext được gọi, mình sẽ reload.
    }

    @Override
    public void setContext(Long studentId, String username) {
        this.studentId = studentId;
        this.username = username;
        reload();
    }

    private void bindTables() {
        // Table 1 bindings
        colOpenSubjectCode.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getSubjectCode()));
        colOpenSubjectName.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getSubjectName()));
        colOpenCredits.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getCredits()));
        colOpenLecturer.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getLecturerName()));
        colOpenAction.setCellFactory(makeRegisterButtonCell());

        // Table 2 bindings
        colEnrolledClassCode.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getClassCode()));
        colEnrolledSubject.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getSubjectName()));
        colEnrolledTime.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getTimeText()));
        colEnrolledStatus.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getStatus()));
        colEnrolledAction.setCellFactory(makeDropButtonCell());

        // UX: click chọn dòng sẽ enable nút "Hủy đăng ký"
        cancelRegistrationButton.setDisable(true);
        enrolledTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) ->
                cancelRegistrationButton.setDisable(newV == null)
        );
    }

    private void wireButtons() {
        // Nút đỏ "Hủy Đăng ký" (theo dòng được chọn)
        cancelRegistrationButton.setOnAction(e -> cancelRegistrationHandle());
    }

    @FXML
    private void cancelRegistrationHandle() {
        EnrolledRow selected = enrolledTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            info("Chưa chọn lớp", "Cháu hãy chọn 1 lớp ở bảng 'Lớp đã đăng ký' để hủy.");
            return;
        }
        if (studentId == null) {
            error("Thiếu studentId", "Chưa có thông tin sinh viên đăng nhập.");
            return;
        }

        try {
            service.drop(studentId, selected.getClassId());
            reload();
            info("Thành công", "Đã hủy đăng ký lớp " + selected.getClassCode());
        } catch (RuntimeException ex) {
            error("Không thể hủy đăng ký", ex.getMessage());
        }
    }

    private void onRegisterClicked(OpenClassRow row) {
        if (row == null) return;
        if (studentId == null) {
            error("Thiếu studentId", "Chưa có thông tin sinh viên đăng nhập.");
            return;
        }

        try {
            service.register(studentId, row.getClassId());
            reload();
            info("Thành công", "Đã đăng ký: " + row.getSubjectName());
        } catch (RuntimeException ex) {
            // chỗ này sau này cháu ném BusinessException: trùng lịch/đủ sĩ số/đã đăng ký...
            error("Không thể đăng ký", ex.getMessage());
        }
    }

    private void reload() {
        if (studentId == null) return;

        // gọi service lấy list (sau này service nối DB thật)
        List<OpenClassRow> open = service.getOpenClasses(studentId);
        List<EnrolledRow> enrolled = service.getEnrolledClasses(studentId);

        openData.setAll(open);
        enrolledData.setAll(enrolled);
    }

    private Callback<TableColumn<OpenClassRow, Void>, TableCell<OpenClassRow, Void>> makeRegisterButtonCell() {
        return col -> new TableCell<>() {
            private final Button btn = new Button("ĐK");

            {
                btn.setStyle("-fx-background-color: #00796B; -fx-text-fill: white; -fx-background-radius: 5;");
                btn.setOnAction(e -> {
                    OpenClassRow row = getTableView().getItems().get(getIndex());
                    onRegisterClicked(row);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        };
    }

    private Callback<TableColumn<EnrolledRow, Void>, TableCell<EnrolledRow, Void>> makeDropButtonCell() {
        return col -> new TableCell<>() {
            private final Button btn = new Button("Hủy");

            {
                btn.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-background-radius: 5;");
                btn.setOnAction(e -> {
                    EnrolledRow row = getTableView().getItems().get(getIndex());
                    if (row == null) return;
                    try {
                        service.drop(studentId, row.getClassId());
                        reload();
                    } catch (RuntimeException ex) {
                        error("Không thể hủy", ex.getMessage());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        };
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
