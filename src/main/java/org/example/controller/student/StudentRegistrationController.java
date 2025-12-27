package org.example.controller.student;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.util.Callback;
import org.example.dto.EnrolledRow;
import org.example.dto.OpenClassRow;
import org.example.service.SessionContext;
import org.example.service.StudentRegistrationService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class StudentRegistrationController {

    // ===== Search =====
    @FXML private TextField searchField;
    @FXML private Button searchButton;

    // ===== Table 1: open classes =====
    @FXML private TableView<OpenClassRow> openClassesTable;
    @FXML private TableColumn<OpenClassRow, String>  colOpenSubjectCode;
    @FXML private TableColumn<OpenClassRow, String>  colOpenSubjectName;
    @FXML private TableColumn<OpenClassRow, String>  colOpenClassCode;
    @FXML private TableColumn<OpenClassRow, Integer> colOpenCredits;
    @FXML private TableColumn<OpenClassRow, String>  colOpenLecturer;
    @FXML private TableColumn<OpenClassRow, Void>    colOpenAction;

    // ===== Table 2: enrolled =====
    @FXML private TableView<EnrolledRow> enrolledTable;
    @FXML private TableColumn<EnrolledRow, Boolean> colEnrolledSelect; // ✅ mới
    @FXML private TableColumn<EnrolledRow, String> colEnrolledClassCode;
    @FXML private TableColumn<EnrolledRow, String> colEnrolledSubject;
    @FXML private TableColumn<EnrolledRow, String> colEnrolledTime;
    @FXML private TableColumn<EnrolledRow, String> colEnrolledStatus;

    // ===== Bottom UI =====
    @FXML private Label totalCreditsLabel;
    @FXML private Button cancelRegistrationButton;

    private final ObservableList<OpenClassRow> openData = FXCollections.observableArrayList();
    private final ObservableList<EnrolledRow> enrolledData = FXCollections.observableArrayList();

    private final StudentRegistrationService service = new StudentRegistrationService();

    private String getStudentIdOrThrow() {
        String u = SessionContext.getUsername();
        if (u == null || u.isBlank()) throw new IllegalStateException("Chưa đăng nhập (SessionContext.username null).");
        return u.trim();
    }

    @FXML
    public void initialize() {
        bindTables();

        openClassesTable.setItems(openData);
        enrolledTable.setItems(enrolledData);

        searchField.setOnAction(this::searchHandle);

        try {
            reload();
        } catch (Exception ex) {
            error("Lỗi tải dữ liệu", ex.getMessage());
        }
    }

    @FXML
    private void searchHandle(ActionEvent e) {
        String keyword = (searchField.getText() == null) ? "" : searchField.getText().trim();

        try {
            String studentId = getStudentIdOrThrow();
            List<OpenClassRow> open = keyword.isEmpty()
                    ? service.getOpenClasses(studentId)
                    : service.searchOpenClasses(studentId, keyword);
            openData.setAll(open);
        } catch (RuntimeException ex) {
            error("Lỗi tìm kiếm", ex.getMessage());
        } catch (Exception ex) {
            error("Lỗi hệ thống", ex.getMessage());
        }
    }

    private void onRegisterClicked(OpenClassRow row) {
        if (row == null) return;

        try {
            String studentId = getStudentIdOrThrow();
            service.register(studentId, row.getClassId());

            // reload để bảng dưới + tổng tín chỉ cập nhật đúng
            reload();
            info("Thành công", "Đã đăng ký lớp: " + safe(row.getSubjectName()));

        } catch (RuntimeException ex) {
            error("Không thể đăng ký", ex.getMessage());
        } catch (Exception ex) {
            error("Lỗi hệ thống", ex.getMessage());
        }
    }

    /** ✅ Hủy nhiều môn theo checkbox + xác nhận */
    @FXML
    private void cancelRegistrationHandle() {
        List<EnrolledRow> selected = enrolledData.stream()
                .filter(EnrolledRow::isSelected)
                .toList();

        if (selected.isEmpty()) {
            info("Chưa chọn lớp", "Cháu hãy tích chọn ít nhất 1 lớp để hủy.");
            return;
        }

        String classes = selected.stream()
                .map(EnrolledRow::getClassCode)
                .collect(Collectors.joining(", "));

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận hủy");
        confirm.setHeaderText("Bạn có chắc muốn hủy " + selected.size() + " lớp không?");
        confirm.setContentText(classes);

        ButtonType ok = new ButtonType("Xác nhận", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Không", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(ok, cancel);

        if (confirm.showAndWait().orElse(cancel) != ok) return;

        try {
            String studentId = getStudentIdOrThrow();

            for (EnrolledRow r : selected) {
                service.drop(studentId, r.getClassId());
            }

            reload();
            info("Thành công", "Đã hủy " + selected.size() + " lớp.");

        } catch (RuntimeException ex) {
            error("Không thể hủy đăng ký", ex.getMessage());
        } catch (Exception ex) {
            error("Lỗi hệ thống", ex.getMessage());
        }
    }

    private void reload() {
        String studentId = getStudentIdOrThrow();

        List<OpenClassRow> open = service.getOpenClasses(studentId);
        List<EnrolledRow> enrolled = service.getEnrolledClasses(studentId);

        enrolled.forEach(r -> r.setSelected(false)); // giữ checkbox nếu có

        openData.setAll(open);
        enrolledData.setAll(enrolled);

        int total = service.getCurrentCredits(studentId);
        totalCreditsLabel.setText("Tổng số tín chỉ: " + total + "/24");
    }


    private void bindTables() {
        // Open classes
        colOpenClassCode.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getClassCode()));
        colOpenSubjectCode.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getSubjectCode()));
        colOpenSubjectName.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getSubjectName()));
        colOpenCredits.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getCredits()));
        colOpenLecturer.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getLecturerName()));
        colOpenAction.setCellFactory(makeRegisterButtonCell());

        // ✅ Enrolled checkbox column
        colEnrolledSelect.setCellValueFactory(cell -> cell.getValue().selectedProperty());
        colEnrolledSelect.setCellFactory(CheckBoxTableCell.forTableColumn(colEnrolledSelect));
        colEnrolledSelect.setEditable(true);

        enrolledTable.setEditable(true);

        // Enrolled other columns
        colEnrolledClassCode.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getClassCode()));
        colEnrolledSubject.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getSubjectName()));
        colEnrolledTime.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getTimeText()));
        colEnrolledStatus.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getStatus()));
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

    private String safe(String s) { return (s == null) ? "" : s; }

    private void info(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(Objects.toString(title, ""));
        a.setHeaderText(null);
        a.setContentText(Objects.toString(msg, ""));
        a.showAndWait();
    }

    private void error(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(Objects.toString(title, ""));
        a.setHeaderText(null);
        a.setContentText(Objects.toString(msg, ""));
        a.showAndWait();
    }
}
