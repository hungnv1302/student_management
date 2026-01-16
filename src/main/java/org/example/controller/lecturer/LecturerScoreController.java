package org.example.controller.lecturer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import org.example.dto.GradeRowDto;
import org.example.service.LecturerScoreService;
import org.example.service.SessionContext;

import java.math.BigDecimal;

public class LecturerScoreController {

    @FXML private TextField classSearchField;
    @FXML private TableView<GradeRowDto> gradeTable;
    @FXML private Label statusLabel;

    @FXML private TableColumn<GradeRowDto, String> colStudentId;
    @FXML private TableColumn<GradeRowDto, String> colFullName;
    @FXML private TableColumn<GradeRowDto, BigDecimal> colMidterm;
    @FXML private TableColumn<GradeRowDto, BigDecimal> colFinal;
    @FXML private TableColumn<GradeRowDto, BigDecimal> colTotal;
    @FXML private TableColumn<GradeRowDto, String> colNote;

    private final LecturerScoreService service = new LecturerScoreService();
    private final ObservableList<GradeRowDto> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        gradeTable.setItems(data);
        gradeTable.setEditable(true);

        colStudentId.setCellValueFactory(c -> c.getValue().studentIdProperty());
        colFullName.setCellValueFactory(c -> c.getValue().fullNameProperty());
        colMidterm.setCellValueFactory(c -> c.getValue().midtermProperty());
        colFinal.setCellValueFactory(c -> c.getValue().finProperty());
        colTotal.setCellValueFactory(c -> c.getValue().totalProperty());

        colNote.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().isFinalized() ? "Đã tính" : ""
                )
        );

        StringConverter<BigDecimal> converter = new StringConverter<>() {
            @Override
            public String toString(BigDecimal v) {
                return v == null ? "" : v.stripTrailingZeros().toPlainString();
            }

            @Override
            public BigDecimal fromString(String s) {
                if (s == null) return null;
                String t = s.trim();
                if (t.isEmpty()) return null;
                try {
                    BigDecimal value = new BigDecimal(t);
                    // Kiểm tra ngay khi nhập
                    if (value.compareTo(BigDecimal.ZERO) < 0 ||
                            value.compareTo(BigDecimal.TEN) > 0) {
                        show(Alert.AlertType.WARNING, "Điểm không hợp lệ",
                                "Điểm phải trong khoảng 0-10");
                        return null;
                    }
                    return value;
                } catch (NumberFormatException e) {
                    show(Alert.AlertType.WARNING, "Lỗi định dạng",
                            "Vui lòng nhập số hợp lệ");
                    return null;
                }
            }
        };

        // Chỉ edit khi chưa chốt điểm
        setupEditable(colMidterm, converter, GradeRowDto::canEditMidterm,
                (r, v) -> r.setMidterm(v));
        setupEditable(colFinal, converter, GradeRowDto::canEditFinal,
                (r, v) -> r.setFin(v));

        // Cột total không cho edit (tự động tính bởi database)
        colTotal.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item == null ? "" : item.stripTrailingZeros().toPlainString());

                    GradeRowDto r = getTableRow() == null ? null : getTableRow().getItem();
                    if (r != null && r.isFinalized()) {
                        setStyle("-fx-background-color: #E8F5E9; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }

    @FXML
    public void handleLoad() {
        String classId = safe(classSearchField.getText());
        String lecturerId = SessionContext.getUserId();

        if (lecturerId == null || lecturerId.isBlank()) {
            show(Alert.AlertType.ERROR, "Lỗi phiên làm việc",
                    "Không xác định được giảng viên. Vui lòng đăng nhập lại.");
            return;
        }

        // Kiểm tra classId rỗng
        if (classId.isBlank()) {
            data.clear();
            gradeTable.refresh();
            statusLabel.setText("Vui lòng nhập mã lớp");
            statusLabel.setStyle("-fx-text-fill: #FF9800;");
            return;
        }

        try {
            var rows = service.load(lecturerId, classId);
            data.setAll(rows);
            gradeTable.refresh();

            // Cập nhật status
            long finalized = rows.stream().filter(GradeRowDto::isFinalized).count();
            long canCalculate = rows.stream()
                    .filter(r -> !r.isFinalized() && r.canCalculateTotal())
                    .count();

            statusLabel.setText(String.format(
                    "Tổng: %d SV | Đã tính: %d | Chưa chốt: %d | Có thể tính điểm: %d",
                    rows.size(), finalized, rows.size() - finalized, canCalculate
            ));
            statusLabel.setStyle("-fx-text-fill: #2E7D32;");

        } catch (Exception e) {
            // Xóa dữ liệu cũ khi có lỗi
            data.clear();
            gradeTable.refresh();

            statusLabel.setText("Lỗi tải dữ liệu");
            statusLabel.setStyle("-fx-text-fill: #C62828;");
            show(Alert.AlertType.ERROR, "Lỗi tải danh sách", e.getMessage());
        }
    }

    @FXML
    public void handleSave() {
        String classId = safe(classSearchField.getText());
        String lecturerId = SessionContext.getUserId();

        if (lecturerId == null || lecturerId.isBlank()) {
            show(Alert.AlertType.ERROR, "Lỗi phiên làm việc",
                    "Không xác định được giảng viên. Vui lòng đăng nhập lại.");
            return;
        }

        // Kiểm tra classId rỗng
        if (classId.isBlank()) {
            statusLabel.setText("Vui lòng nhập mã lớp");
            statusLabel.setStyle("-fx-text-fill: #FF9800;");
            return;
        }

        // Kiểm tra có dữ liệu để lưu không
        if (data.isEmpty()) {
            show(Alert.AlertType.WARNING, "Không có dữ liệu",
                    "Vui lòng tải danh sách sinh viên trước khi lưu.");
            return;
        }

        try {
            service.save(lecturerId, classId, data);

            // Reload để đồng bộ dữ liệu
            data.setAll(service.load(lecturerId, classId));
            gradeTable.refresh();

            statusLabel.setText("✓ Đã lưu điểm thành công lúc " +
                    java.time.LocalTime.now().format(
                            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
                    ));
            statusLabel.setStyle("-fx-text-fill: #2E7D32;");

            show(Alert.AlertType.INFORMATION, "Thành công",
                    "Đã lưu điểm");

        } catch (Exception e) {
            // Xóa data khi lưu thất bại (có thể do mất quyền)
            data.clear();
            gradeTable.refresh();

            statusLabel.setText("✗ Lỗi lưu điểm");
            statusLabel.setStyle("-fx-text-fill: #C62828;");
            show(Alert.AlertType.ERROR, "Lỗi lưu điểm", e.getMessage());
        }
    }

    @FXML
    public void handleCalculateFinalGrades() {
        String classId = safe(classSearchField.getText());
        String lecturerId = SessionContext.getUserId();

        if (lecturerId == null || lecturerId.isBlank()) {
            show(Alert.AlertType.ERROR, "Lỗi phiên làm việc",
                    "Không xác định được giảng viên. Vui lòng đăng nhập lại.");
            return;
        }

        // Kiểm tra classId rỗng
        if (classId.isBlank()) {
            statusLabel.setText("Vui lòng nhập mã lớp");
            statusLabel.setStyle("-fx-text-fill: #FF9800;");
            return;
        }

        // Kiểm tra có dữ liệu không
        if (data.isEmpty()) {
            show(Alert.AlertType.WARNING, "Không có dữ liệu",
                    "Vui lòng tải danh sách sinh viên trước.");
            return;
        }

        // Kiểm tra có sinh viên nào đủ điều kiện không
        long canCalculate = data.stream()
                .filter(r -> !r.isFinalized() && r.canCalculateTotal())
                .count();

        if (canCalculate == 0) {
            show(Alert.AlertType.WARNING, "Không thể tính điểm",
                    "Không có sinh viên nào đủ điều kiện tính điểm.\n" +
                            "Sinh viên phải có đủ điểm GK và CK, và chưa được chốt điểm.");
            return;
        }

        // Xác nhận
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Xác nhận tính điểm học phần");
        confirm.setContentText(
                String.format("Hệ thống sẽ tính điểm học phần cho %d sinh viên.\n\n" +
                        "Công thức: Điểm TK = Điểm GK × 0.5 + Điểm CK × 0.5\n\n" +
                        "Bạn có chắc chắn muốn tiếp tục?", canCalculate)
        );

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            // Lưu điểm trước khi tính
            service.save(lecturerId, classId, data);

            // Tính điểm học phần
            int updated = service.calculateAllFinalGrades(lecturerId, classId);

            // Reload để hiển thị kết quả
            data.setAll(service.load(lecturerId, classId));
            gradeTable.refresh();

            statusLabel.setText(String.format(
                    "✓ Đã tính điểm thành công cho %d sinh viên", updated
            ));
            statusLabel.setStyle("-fx-text-fill: #2E7D32;");

            show(Alert.AlertType.INFORMATION, "Thành công",
                    String.format("Đã tính điểm cho %d sinh viên.", updated));

        } catch (Exception e) {
            // Xóa data khi tính điểm thất bại
            data.clear();
            gradeTable.refresh();

            statusLabel.setText("✗ Lỗi tính điểm");
            statusLabel.setStyle("-fx-text-fill: #C62828;");
            show(Alert.AlertType.ERROR, "Lỗi tính điểm", e.getMessage());
        }
    }

    // ===== Helper methods =====
    @FunctionalInterface
    private interface CanEdit { boolean test(GradeRowDto r); }

    @FunctionalInterface
    private interface Setter { void set(GradeRowDto r, BigDecimal v); }

    private void setupEditable(TableColumn<GradeRowDto, BigDecimal> col,
                               StringConverter<BigDecimal> converter,
                               CanEdit rule,
                               Setter setter) {

        col.setCellFactory(tc -> new TextFieldTableCell<>(converter) {
            @Override
            public void startEdit() {
                GradeRowDto r = getTableRow() == null ? null : getTableRow().getItem();
                if (r == null || !rule.test(r)) return;
                super.startEdit();
            }

            @Override
            public void commitEdit(BigDecimal newValue) {
                super.commitEdit(newValue);
                GradeRowDto r = getTableRow().getItem();
                if (r == null) return;

                setter.set(r, newValue);
                gradeTable.refresh();
            }

            @Override
            public void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setStyle("");
                    return;
                }

                GradeRowDto r = getTableRow() == null ? null : getTableRow().getItem();
                boolean locked = (r == null) || !rule.test(r);

                if (locked) {
                    setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #9E9E9E;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private void show(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}