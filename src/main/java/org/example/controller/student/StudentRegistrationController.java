package org.example.controller.student;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.example.dto.EnrolledRow;
import org.example.dto.OpenClassRow;
import org.example.service.StudentRegistrationService;

import java.util.List;

public class StudentRegistrationController implements StudentViewContextAware {

    // ===== Search =====
    @FXML private TextField searchField;
    @FXML private Button searchButton;

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

    private final ObservableList<OpenClassRow> openData = FXCollections.observableArrayList();
    private final ObservableList<EnrolledRow> enrolledData = FXCollections.observableArrayList();

    private Long studentId;
    private String username;

    private final StudentRegistrationService service = new StudentRegistrationService();

    @FXML
    public void initialize() {
        bindTables();

        openClassesTable.setItems(openData);
        enrolledTable.setItems(enrolledData);

        // nút đỏ có onAction trong FXML rồi thì không cần set lại (tuỳ cháu giữ/bỏ)
        cancelRegistrationButton.setDisable(true);
        enrolledTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) ->
                cancelRegistrationButton.setDisable(newV == null)
        );

        // Enter trong ô search cũng chạy tìm kiếm cho tiện
        searchField.setOnAction(this::searchHandle);
    }

    @Override
    public void setContext(Long studentId, String username) {
        this.studentId = studentId;
        this.username = username;
        reload(); // load lần đầu
    }

    // ✅ HÀM NÀY BẮT BUỘC PHẢI CÓ vì FXML đang gọi onAction="#searchHandle"
    @FXML
    private void searchHandle(ActionEvent e) {
        if (studentId == null) {
            error("Thiếu studentId", "Chưa có thông tin sinh viên đăng nhập.");
            return;
        }

        String keyword = (searchField.getText() == null) ? "" : searchField.getText().trim();

        try {
            // Nếu rỗng thì load tất cả
            List<OpenClassRow> open = keyword.isEmpty()
                    ? service.getOpenClasses(studentId)
                    : service.searchOpenClasses(studentId, keyword);

            openData.setAll(open);
        } catch (RuntimeException ex) {
            error("Lỗi tìm kiếm", ex.getMessage());
        }
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
            error("Không thể đăng ký", ex.getMessage());
        }
    }

    private void reload() {
        if (studentId == null) return;

        List<OpenClassRow> open = service.getOpenClasses(studentId);
        List<EnrolledRow> enrolled = service.getEnrolledClasses(studentId);

        openData.setAll(open);
        enrolledData.setAll(enrolled);
    }

    private void bindTables() {
        colOpenSubjectCode.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getSubjectCode()));
        colOpenSubjectName.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getSubjectName()));
        colOpenCredits.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getCredits()));
        colOpenLecturer.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getLecturerName()));
        colOpenAction.setCellFactory(makeRegisterButtonCell());

        colEnrolledClassCode.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getClassCode()));
        colEnrolledSubject.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getSubjectName()));
        colEnrolledTime.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getTimeText()));
        colEnrolledStatus.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getStatus()));
        colEnrolledAction.setCellFactory(makeDropButtonCell());
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
            @Override protected void updateItem(Void item, boolean empty) {
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
            @Override protected void updateItem(Void item, boolean empty) {
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
