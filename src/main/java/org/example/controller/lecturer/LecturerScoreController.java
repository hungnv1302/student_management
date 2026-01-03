package org.example.controller.lecturer;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import org.example.dto.StudentScoreRow;
import org.example.service.LecturerScoreService;
import org.example.service.SessionContext;

import java.math.BigDecimal;

public class LecturerScoreController {

    @FXML private TextField classSearchField;

    @FXML private TableView<StudentScoreRow> gradeTable;
    @FXML private TableColumn<StudentScoreRow, String> colStudentId;
    @FXML private TableColumn<StudentScoreRow, String> colFullName;
    @FXML private TableColumn<StudentScoreRow, BigDecimal> colMidterm;
    @FXML private TableColumn<StudentScoreRow, BigDecimal> colFinal;
    @FXML private TableColumn<StudentScoreRow, BigDecimal> colTotal;
    @FXML private TableColumn<StudentScoreRow, String> colNote;

    private final LecturerScoreService service = new LecturerScoreService();
    private String currentClassId;

    @FXML
    public void initialize() {
        gradeTable.setEditable(true);

        colStudentId.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getStudentId()));
        colFullName.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getFullName()));
        colNote.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getStatus()));

        colMidterm.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getMidtermScore()));
        colFinal.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getFinalScore()));
        colTotal.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getTotalScore()));

        StringConverter<BigDecimal> converter = new StringConverter<>() {
            @Override public String toString(BigDecimal v) {
                return v == null ? "" : v.stripTrailingZeros().toPlainString();
            }
            @Override public BigDecimal fromString(String s) {
                if (s == null) return null;
                String t = s.trim();
                if (t.isEmpty()) return null;
                return new BigDecimal(t);
            }
        };

        setupEditableColumn(colMidterm, converter, StudentScoreRow::canEditMidterm, (r, v) -> r.setMidtermScore(v));
        setupEditableColumn(colFinal,   converter, StudentScoreRow::canEditFinal,   (r, v) -> r.setFinalScore(v));
        setupEditableColumn(colTotal,   converter, StudentScoreRow::canEditTotal,   (r, v) -> r.setTotalScore(v));
    }

    @FXML
    public void handleLoad() {
        if (!SessionContext.isLecturer()) {
            alert(Alert.AlertType.WARNING, "Không có quyền", "Role hiện tại không phải LECTURER.");
            return;
        }
        String lecturerId = SessionContext.getUserId(); // lecturer_id: 20180001
        String classId = classSearchField.getText() == null ? "" : classSearchField.getText().trim();

        if (classId.isEmpty()) {
            alert(Alert.AlertType.WARNING, "Thiếu thông tin", "Nhập Mã lớp (VD: 25A008).");
            return;
        }

        try {
            var rows = service.load(lecturerId, classId);
            currentClassId = classId;
            gradeTable.setItems(FXCollections.observableArrayList(rows));
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Lỗi tải danh sách", e.getMessage());
        }
    }

    @FXML
    public void handleSave() {
        if (currentClassId == null) {
            alert(Alert.AlertType.WARNING, "Chưa chọn lớp", "Hãy tải danh sách lớp trước.");
            return;
        }
        String lecturerId = SessionContext.getUserId();
        try {
            service.save(lecturerId, currentClassId, gradeTable.getItems());
            handleLoad(); // reload để phản ánh điểm đã lưu & khóa cell
            alert(Alert.AlertType.INFORMATION, "Thành công", "Đã lưu điểm.");
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Lỗi lưu điểm", e.getMessage());
        }
    }

    @FXML
    public void handleFinalize() {
        if (currentClassId == null) {
            alert(Alert.AlertType.WARNING, "Chưa chọn lớp", "Hãy tải danh sách lớp trước.");
            return;
        }

        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setHeaderText("Chốt điểm");
        c.setContentText("Sau khi chốt sẽ không thể sửa. Chốt lớp " + currentClassId + " ?");
        if (c.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        String lecturerId = SessionContext.getUserId();
        try {
            service.finalizeScores(lecturerId, currentClassId, gradeTable.getItems());
            handleLoad();
            alert(Alert.AlertType.INFORMATION, "Thành công", "Đã chốt điểm. Không thể sửa nữa.");
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Lỗi chốt điểm", e.getMessage());
        }
    }

    // ===== helpers =====
    @FunctionalInterface private interface CanEdit { boolean test(StudentScoreRow r); }
    @FunctionalInterface private interface Setter { void set(StudentScoreRow r, BigDecimal v); }

    private void setupEditableColumn(TableColumn<StudentScoreRow, BigDecimal> col,
                                     StringConverter<BigDecimal> converter,
                                     CanEdit rule,
                                     Setter setter) {

        col.setCellFactory(tc -> new TextFieldTableCell<>(converter) {
            @Override public void startEdit() {
                StudentScoreRow r = getTableRow() == null ? null : (StudentScoreRow) getTableRow().getItem();
                if (r == null || !rule.test(r)) return;
                super.startEdit();
            }

            @Override public void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                StudentScoreRow r = getTableRow() == null ? null : (StudentScoreRow) getTableRow().getItem();
                boolean editable = r != null && rule.test(r);
                setOpacity(editable ? 1.0 : 0.55);
            }
        });

        col.setOnEditCommit(evt -> {
            try {
                StudentScoreRow r = evt.getRowValue();
                setter.set(r, evt.getNewValue());
                gradeTable.refresh();
            } catch (Exception ex) {
                alert(Alert.AlertType.ERROR, "Giá trị không hợp lệ", ex.getMessage());
                gradeTable.refresh();
            }
        });
    }

    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}
