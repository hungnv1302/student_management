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
                new javafx.beans.property.SimpleStringProperty(c.getValue().isFinalized() ? "Đã chốt" : "")
        );

        StringConverter<BigDecimal> converter = new StringConverter<>() {
            @Override public String toString(BigDecimal v) { return v == null ? "" : v.stripTrailingZeros().toPlainString(); }
            @Override public BigDecimal fromString(String s) {
                if (s == null) return null;
                String t = s.trim();
                if (t.isEmpty()) return null;
                return new BigDecimal(t);
            }
        };

        // chỉ edit khi NULL + chưa chốt
        setupEditable(colMidterm, converter, GradeRowDto::canEditMidterm, (r, v) -> r.setMidterm(v));
        setupEditable(colFinal, converter, GradeRowDto::canEditFinal, (r, v) -> r.setFin(v));

        // total không edit (tự tính)
        colTotal.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item == null ? "" : item.stripTrailingZeros().toPlainString()));
            }
        });
    }

    @FXML
    public void handleLoad() {
        String classId = safe(classSearchField.getText());
        String lecturerId = SessionContext.getUsername(); // phải là lecturer_id

        try {
            var rows = service.load(lecturerId, classId);
            data.setAll(rows);
            gradeTable.refresh();
        } catch (Exception e) {
            show(Alert.AlertType.ERROR, "Lỗi tải danh sách", e.getMessage());
        }
    }

    @FXML
    public void handleSave() {
        String classId = safe(classSearchField.getText());
        String lecturerId = SessionContext.getUsername();

        try {
            service.save(lecturerId, classId, data);
            // reload để cell vừa được lưu (không còn NULL) => tự khóa
            data.setAll(service.load(lecturerId, classId));
            show(Alert.AlertType.INFORMATION, "Thành công", "Đã lưu điểm.");
        } catch (Exception e) {
            show(Alert.AlertType.ERROR, "Lỗi lưu điểm", e.getMessage());
        }
    }

    @FXML
    public void handleFinalize() {
        String classId = safe(classSearchField.getText());
        String lecturerId = SessionContext.getUsername();

        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setHeaderText("Chốt điểm");
        c.setContentText("Sau khi chốt sẽ không thể sửa. Bạn chắc chắn?");
        if (c.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            service.finalizeAll(lecturerId, classId, data);
            data.setAll(service.load(lecturerId, classId));
            show(Alert.AlertType.INFORMATION, "Thành công", "Đã chốt điểm.");
        } catch (Exception e) {
            show(Alert.AlertType.ERROR, "Lỗi chốt điểm", e.getMessage());
        }
    }

    // ===== helper =====
    @FunctionalInterface private interface CanEdit { boolean test(GradeRowDto r); }
    @FunctionalInterface private interface Setter { void set(GradeRowDto r, BigDecimal v); }

    private void setupEditable(TableColumn<GradeRowDto, BigDecimal> col,
                               StringConverter<BigDecimal> converter,
                               CanEdit rule,
                               Setter setter) {

        col.setCellFactory(tc -> new TextFieldTableCell<>(converter) {
            @Override public void startEdit() {
                GradeRowDto r = getTableRow() == null ? null : (GradeRowDto) getTableRow().getItem();
                if (r == null || !rule.test(r)) return;
                super.startEdit();
            }

            @Override public void commitEdit(BigDecimal newValue) {
                super.commitEdit(newValue);
                GradeRowDto r = getTableRow().getItem();
                if (r == null) return;

                setter.set(r, newValue);

                // nếu đủ 2 điểm thì tự tính total
                BigDecimal total = service.computeTotal(r.getMidterm(), r.getFin());
                r.setTotal(total);

                gradeTable.refresh();
            }

            @Override public void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) return;

                GradeRowDto r = getTableRow() == null ? null : (GradeRowDto) getTableRow().getItem();
                boolean locked = (r == null) || !rule.test(r);
                setStyle(locked ? "-fx-background-color: #F3F4F6;" : "");
            }
        });
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }

    private void show(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}
