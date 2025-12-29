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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StudentRegistrationController {

    @FXML private TextField searchField;
    @FXML private Button searchButton;

    // ===== Table 1: open classes =====
    @FXML private TableView<OpenClassRow> openClassesTable;
    @FXML private TableColumn<OpenClassRow, String>  colOpenSubjectCode;
    @FXML private TableColumn<OpenClassRow, String>  colOpenSubjectName;
    @FXML private TableColumn<OpenClassRow, String>  colOpenClassCode;
    @FXML private TableColumn<OpenClassRow, Integer> colOpenCredits;
    @FXML private TableColumn<OpenClassRow, String>  colOpenLecturer;

    // ✅ NEW columns
    @FXML private TableColumn<OpenClassRow, String>  colOpenDay;
    @FXML private TableColumn<OpenClassRow, String>  colOpenSlot;
    @FXML private TableColumn<OpenClassRow, String>  colOpenRoom;

    @FXML private TableColumn<OpenClassRow, Void>    colOpenAction;

    // ===== Table 2: enrolled =====
    @FXML private TableView<EnrolledRow> enrolledTable;
    @FXML private TableColumn<EnrolledRow, Boolean> colEnrolledSelect;
    @FXML private TableColumn<EnrolledRow, String> colEnrolledClassCode;
    @FXML private TableColumn<EnrolledRow, String> colEnrolledSubject;
    @FXML private TableColumn<EnrolledRow, String> colEnrolledTime;
    @FXML private TableColumn<EnrolledRow, String> colEnrolledStatus;

    @FXML private Label totalCreditsLabel;
    @FXML private Button cancelRegistrationButton;

    private final ObservableList<OpenClassRow> openData = FXCollections.observableArrayList();
    private final ObservableList<EnrolledRow> enrolledData = FXCollections.observableArrayList();

    private final StudentRegistrationService service = new StudentRegistrationService();

    private static final Pattern P_TIME = Pattern.compile("(\\d{1,2}:\\d{2})\\s*-\\s*(\\d{1,2}:\\d{2})");
    private static final Pattern P_ROOM = Pattern.compile("\\|\\s*([A-Za-z0-9_-]+)");

    private String getStudentIdOrThrow() {
        String u = SessionContext.getUsername();
        if (u == null || u.isBlank())
            throw new IllegalStateException("Chưa đăng nhập (SessionContext.username null).");
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

        // ✅ chỉ cho đăng ký khi eligibility = ELIGIBLE
        if (!"eligible".equalsIgnoreCase(sl(row.getEligibility()))) {
            String reason = s(row.getReason());
            info("Không thể đăng ký",
                    reason.isBlank() ? "Lớp này không đủ điều kiện đăng ký." : reason);
            return;
        }

        try {
            String studentId = getStudentIdOrThrow();
            service.register(studentId, row.getClassId());

            reload();
            info("Thành công", "Đã đăng ký lớp: " + s(row.getSubjectName()));

        } catch (RuntimeException ex) {
            error("Không thể đăng ký", ex.getMessage());
        } catch (Exception ex) {
            error("Lỗi hệ thống", ex.getMessage());
        }
    }

    @FXML
    private void cancelRegistrationHandle() {
        List<EnrolledRow> selected = enrolledData.stream()
                .filter(EnrolledRow::isSelected)
                .toList();

        if (selected.isEmpty()) {
            info("Chưa chọn lớp", "Hãy tích chọn ít nhất 1 lớp để hủy.");
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

        enrolled.forEach(r -> r.setSelected(false));

        openData.setAll(open);
        enrolledData.setAll(enrolled);

        int total = service.getOpenCredits(studentId);
        totalCreditsLabel.setText("Tổng số tín chỉ: " + total + "/24");
    }

    private void bindTables() {
        // Open classes
        colOpenClassCode.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(s(c.getValue().getClassId())));
        colOpenSubjectCode.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(s(c.getValue().getSubjectId())));
        colOpenSubjectName.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(s(c.getValue().getSubjectName())));
        colOpenCredits.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getCredit()));
        colOpenLecturer.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(s(c.getValue().getLecturerName())));

        // ✅ NEW: parse từ scheduleText (ví dụ "T3 12:30-14:55 @A103")
        colOpenDay.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(extractDay(c.getValue().getScheduleText())));
        colOpenSlot.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(extractSlot(c.getValue().getScheduleText())));
        colOpenRoom.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(extractRoom(c.getValue().getScheduleText())));

        colOpenAction.setCellFactory(makeRegisterButtonCell());

        // Enrolled checkbox column
        colEnrolledSelect.setCellValueFactory(cell -> cell.getValue().selectedProperty());
        colEnrolledSelect.setCellFactory(CheckBoxTableCell.forTableColumn(colEnrolledSelect));
        colEnrolledSelect.setEditable(true);

        enrolledTable.setEditable(true);

        colEnrolledClassCode.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(s(c.getValue().getClassCode())));
        colEnrolledSubject.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(s(c.getValue().getSubjectName())));
        colEnrolledTime.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(s(c.getValue().getTimeText())));
        colEnrolledStatus.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(s(c.getValue().getStatus())));
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
                if (empty) {
                    setGraphic(null);
                    return;
                }

                OpenClassRow row = getTableView().getItems().get(getIndex());
                boolean ok = "eligible".equalsIgnoreCase(sl(row.getEligibility()));
                btn.setDisable(!ok);
                setGraphic(btn);
            }
        };
    }

    // ===== Helpers: schedule parsing =====
    // Nếu scheduleText có nhiều dòng/buổi, lấy buổi đầu để hiển thị gọn.
    private String firstSession(String scheduleText) {
        if (scheduleText == null) return "";
        String t = scheduleText.replace("\r", "").trim();
        if (t.isBlank()) return "";
        // ưu tiên theo ';' vì SQL string_agg đang dùng '; '
        String[] parts = t.split(";");
        return parts.length > 0 ? parts[0].trim() : t;
    }

    private String extractDay(String scheduleText) {
        String first = firstSession(scheduleText);
        if (first.isBlank()) return "";
        String[] tokens = first.split("\\s+");
        return tokens.length > 0 ? tokens[0].trim() : "";
    }

    private String extractSlot(String scheduleText) {
        String first = firstSession(scheduleText);
        if (first.isBlank()) return "";
        Matcher m = P_TIME.matcher(first);
        if (m.find()) return m.group(1) + "-" + m.group(2);
        return "";
    }

    private String extractRoom(String scheduleText) {
        String first = firstSession(scheduleText);
        if (first.isBlank()) return "";
        Matcher m = P_ROOM.matcher(first);
        if (m.find()) return m.group(1);
        return "";
    }

    private String s(String v) { return v == null ? "" : v; }
    private String sl(String v) { return v == null ? "" : v.toLowerCase(); }

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
