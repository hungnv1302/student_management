package org.example.controller.student;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.dto.ScheduleRow;
import org.example.service.SessionContext;
import org.example.service.StudentScheduleService;

import java.time.LocalTime;
import java.util.List;

public class StudentScheduleController {

    @FXML private Label termLabel;
    @FXML private GridPane timetableGrid;

    private final StudentScheduleService service = new StudentScheduleService();

    // 4 ca/ngày theo khung giờ đại học
    private static final String[] SLOT_LABELS = {
            "Ca 1 (Tiết 1–3)\n06:45–09:10",
            "Ca 2 (Tiết 4–6)\n09:20–11:45",
            "Ca 3 (Tiết 7–9)\n12:30–14:55",
            "Ca 4 (Tiết 10–12)\n15:05–17:30"
    };

    private static final String[] DAY_LABELS = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };

    private String getStudentIdOrThrow() {
        String u = SessionContext.getUsername();
        if (u == null || u.isBlank()) throw new IllegalStateException("Chưa đăng nhập.");
        return u.trim(); // student_id là varchar
    }

    @FXML
    public void initialize() {
        try {
            buildBaseGrid();
            reload();
        } catch (Exception ex) {
            error("Lỗi tải thời khóa biểu", ex.getMessage());
        }
    }

    @FXML
    private void reloadHandle() {
        try {
            clearCells();
            reload();
        } catch (Exception ex) {
            error("Lỗi tải lại", ex.getMessage());
        }
    }

    private void reload() {
        String studentId = getStudentIdOrThrow();

        short termNo = service.getCurrentOpenTerm();
        termLabel.setText("Kỳ hiện tại: " + termNo);

        List<ScheduleRow> rows = service.getScheduleForCurrentTerm(studentId);

        for (ScheduleRow r : rows) {
            int dayCol = isoDowToCol(r.getDayOfWeek()); // 1..7 -> col 1..7
            int slotRow = timeToSlot(r.getStartTime()); // 1..4 -> row 1..4
            if (dayCol < 1 || dayCol > 7 || slotRow < 1 || slotRow > 4) continue;

            VBox cell = findCell(slotRow, dayCol);
            if (cell == null) continue;

            // Thẻ môn (2 dòng: tên + phòng)
            VBox entry = buildEntryCard(r,cell);
            cell.getChildren().add(entry);
        }
    }

    /**
     * Thẻ hiển thị trong ô:
     * - Dòng 1: Tên môn (ELLIPSIS)
     * - Dòng 2: Phòng: ... (ELLIPSIS)
     * - Tooltip: full info
     */
    private VBox buildEntryCard(ScheduleRow r, VBox cell) {
        String subjectName = nvl(r.getSubjectName());
        String room = blank(r.getRoom()) ? "" : r.getRoom();

        Label title = new Label(subjectName);
        title.getStyleClass().add("title");
        title.setWrapText(true);                 // cho xuống dòng
        title.setMaxWidth(Double.MAX_VALUE);

        Label sub = new Label(room.isBlank() ? "" : ("Phòng: " + room));
        sub.getStyleClass().add("sub");
        sub.setWrapText(true);
        sub.setMaxWidth(Double.MAX_VALUE);

        VBox box = new VBox(4, title, sub);
        box.getStyleClass().add("tt-entry");
        box.setPadding(new javafx.geometry.Insets(8, 10, 8, 10));
        box.setMaxWidth(Double.MAX_VALUE);

        // ✅ QUAN TRỌNG: thẻ môn nở theo bề ngang ô
        box.prefWidthProperty().bind(cell.widthProperty().subtract(12)); // trừ chút padding/gap

        // Tooltip full nội dung
        String tip = subjectName + (room.isBlank() ? "" : ("\nPhòng: " + room));
        javafx.scene.control.Tooltip.install(box, new javafx.scene.control.Tooltip(tip));

        return box;
    }

    // ===== Grid build =====

    private void buildBaseGrid() {
        timetableGrid.getChildren().clear();

        // (0,0) trống
        timetableGrid.add(makeHeaderLabel(""), 0, 0);

        // header day labels col 1..7
        for (int d = 1; d <= 7; d++) {
            Label header = makeHeaderLabel(DAY_LABELS[d - 1]);
            timetableGrid.add(header, d, 0);
        }

        // slot labels at col 0, row 1..4
        for (int s = 1; s <= 4; s++) {
            Label slot = makeSlotLabel(SLOT_LABELS[s - 1]);
            timetableGrid.add(slot, 0, s);
        }

        // data cells: VBox per cell
        for (int s = 1; s <= 4; s++) {
            for (int d = 1; d <= 7; d++) {
                VBox cell = makeDataCell(s);
                cell.setId(cellId(s, d));

                // quan trọng: cho cell “nở” theo bề ngang
                GridPane.setHgrow(cell, Priority.ALWAYS);
                cell.setMaxWidth(Double.MAX_VALUE);

                timetableGrid.add(cell, d, s);
            }
        }
    }

    private void clearCells() {
        for (int s = 1; s <= 4; s++) {
            for (int d = 1; d <= 7; d++) {
                VBox cell = findCell(s, d);
                if (cell != null) cell.getChildren().clear();
            }
        }
    }

    private VBox findCell(int slotRow, int dayCol) {
        String id = cellId(slotRow, dayCol);
        for (Node n : timetableGrid.getChildren()) {
            if (n instanceof VBox v && id.equals(v.getId())) return v;
        }
        return null;
    }

    private String cellId(int slotRow, int dayCol) {
        return "cell_r" + slotRow + "_c" + dayCol;
    }

    private Label makeHeaderLabel(String text) {
        Label lb = new Label(text);
        lb.getStyleClass().add("tt-header");
        lb.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(lb, Priority.ALWAYS);
        return lb;
    }

    private Label makeSlotLabel(String text) {
        Label lb = new Label(text);
        lb.getStyleClass().add("tt-slot");
        lb.setWrapText(true);
        lb.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(lb, Priority.ALWAYS);
        return lb;
    }

    private VBox makeDataCell(int slotRow) {
        VBox box = new VBox(6);
        box.getStyleClass().add("tt-cell");
        box.getStyleClass().add("slot-" + slotRow); // slot-1..slot-4

        // để thẻ môn chiếm full width của cell
        box.setFillWidth(true);
        box.setMaxWidth(Double.MAX_VALUE);

        return box;
    }

    // ISO DOW: 1=Mon..7=Sun -> cột 1..7
    private int isoDowToCol(int isoDow) {
        return isoDow;
    }

    // Chia ca theo khung giờ đại học: dựa vào start_time
    private int timeToSlot(LocalTime start) {
        if (start == null) return -1;

        LocalTime CA2_START = LocalTime.of(9, 20);
        LocalTime CA3_START = LocalTime.of(12, 30);
        LocalTime CA4_START = LocalTime.of(15, 5);

        if (start.isBefore(CA2_START)) return 1;      // Tiết 1-3
        if (start.isBefore(CA3_START)) return 2;      // Tiết 4-6
        if (start.isBefore(CA4_START)) return 3;      // Tiết 7-9
        return 4;                                     // Tiết 10-12
    }

    private boolean blank(String s) { return s == null || s.isBlank(); }
    private String nvl(String s) { return s == null ? "" : s; }

    private void error(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
