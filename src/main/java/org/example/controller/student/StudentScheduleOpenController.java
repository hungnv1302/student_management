package org.example.controller.student;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.dto.ScheduleRow;
import org.example.service.SessionContext;
import org.example.service.StudentScheduleService;

import java.time.LocalTime;
import java.util.List;

public class StudentScheduleOpenController {

    @FXML private Label termLabel;
    @FXML private GridPane timetableGrid;

    private final StudentScheduleService service = new StudentScheduleService();

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
        return u.trim();
    }

    @FXML
    public void initialize() {
        try {
            buildBaseGrid();
            reload();
        } catch (Exception ex) {
            error("Lỗi tải TKB tạm thời", ex.getMessage());
        }
    }

    private void reload() {
        String studentId = getStudentIdOrThrow();

        // ✅ Kỳ đăng ký tiếp theo
        var open = service.getOpenTerm();
        termLabel.setText("Thời khóa biểu tạm thời (kỳ đăng ký): " + open);

        // ✅ Load lịch tạm thời
        List<ScheduleRow> rows = service.getScheduleOpenTerm(studentId);

        for (ScheduleRow r : rows) {
            int dayCol = mapDayToCol(r.getDayOfWeek());      // 1..7
            int slotRow = timeToSlot(r.getStartTime());      // 1..4
            if (dayCol < 1 || dayCol > 7 || slotRow < 1 || slotRow > 4) continue;

            VBox cell = findCell(slotRow, dayCol);
            if (cell == null) continue;

            VBox entry = buildEntryCard(r, cell);
            cell.getChildren().add(entry);
        }
    }

    private VBox buildEntryCard(ScheduleRow r, VBox cell) {
        String subjectName = nvl(r.getSubjectName());
        String room = blank(r.getRoom()) ? "" : r.getRoom();

        Label title = new Label(subjectName);
        title.getStyleClass().add("title");
        title.setWrapText(true);
        title.setMaxWidth(Double.MAX_VALUE);

        Label sub = new Label(room.isBlank() ? "" : ("Phòng: " + room));
        sub.getStyleClass().add("sub");
        sub.setWrapText(true);
        sub.setMaxWidth(Double.MAX_VALUE);

        VBox box = new VBox(4, title, sub);
        box.getStyleClass().add("tt-entry");

        // ✅ full-width trong cell, nhìn “đầy”
        box.setMaxWidth(Double.MAX_VALUE);
        box.prefWidthProperty().bind(cell.widthProperty().subtract(12));

        // ✅ màu theo môn/lớp (KHÔNG dùng subjectCode)
        applySubjectColor(box, r);

        Tooltip.install(box, new Tooltip(subjectName + (room.isBlank() ? "" : ("\nPhòng: " + room))));

        box.setOnMouseClicked(e -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Chi tiết lịch học");
            a.setHeaderText(subjectName);
            a.setContentText(
                    "Mã lớp: " + nvl(r.getClassId()) + "\n" +
                            "Giảng viên: " + nvl(r.getLecturerName()) + "\n" +
                            "Phòng: " + (room.isBlank() ? "..." : room) + "\n" +
                            "Giờ: " + nvl(String.valueOf(r.getStartTime())) + " - " + nvl(String.valueOf(r.getEndTime()))
            );
            a.showAndWait();
        });

        return box;
    }

    // ✅ tạo màu ổn định theo classId (ưu tiên) hoặc subjectName
    private void applySubjectColor(VBox box, ScheduleRow r) {
        String key = !blank(r.getClassId()) ? r.getClassId() : nvl(r.getSubjectName());
        int h = Math.abs(key.hashCode());

        // palette dịu mắt, nhìn rõ (không "giúm gió")
        String[][] palettes = {
                {"#E6FFFA", "#CCFBF1", "#0B7D6E"},
                {"#EEF2FF", "#E0E7FF", "#4338CA"},
                {"#ECFEFF", "#CFFAFE", "#0E7490"},
                {"#FDF4FF", "#FAE8FF", "#86198F"},
                {"#FFF7ED", "#FFEDD5", "#C2410C"},
                {"#F0FDF4", "#DCFCE7", "#166534"}
        };

        String[] p = palettes[h % palettes.length];
        String c1 = p[0], c2 = p[1], border = p[2];

        // style nền + viền trái nổi bật (giống schedule)
        box.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + c1 + ", " + c2 + ");" +
                        "-fx-border-color: " + border + ";" +
                        "-fx-border-width: 1 1 1 6;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-radius: 14;"
        );
    }

    private void buildBaseGrid() {
        timetableGrid.getChildren().clear();

        // (0,0) corner
        timetableGrid.add(makeHeaderLabel(""), 0, 0);

        // Day headers row 0
        for (int d = 1; d <= 7; d++) {
            Label header = makeHeaderLabel(DAY_LABELS[d - 1]);
            timetableGrid.add(header, d, 0);
        }

        // Slot labels col 0
        for (int s = 1; s <= 4; s++) {
            Label slot = makeSlotLabel(SLOT_LABELS[s - 1]);
            timetableGrid.add(slot, 0, s);
        }

        // Data cells
        for (int s = 1; s <= 4; s++) {
            for (int d = 1; d <= 7; d++) {
                VBox cell = makeDataCell(s);
                cell.setId(cellId(s, d));
                GridPane.setHgrow(cell, Priority.ALWAYS);
                cell.setMaxWidth(Double.MAX_VALUE);
                timetableGrid.add(cell, d, s);
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
        box.getStyleClass().add("slot-" + slotRow);
        box.setFillWidth(true);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    // ===== mapping: hỗ trợ cả DB dùng 1..7 (Mon..Sun) và 2..8 (T2..CN) =====
    private int mapDayToCol(int dayOfWeek) {
        // case DB 2..8 (T2..CN)
        if (dayOfWeek == 8) return 7;
        if (dayOfWeek >= 2 && dayOfWeek <= 7) return dayOfWeek - 1;

        // case ISO 1..7 (Mon..Sun)
        if (dayOfWeek == 7) return 7; // Sunday
        if (dayOfWeek >= 1 && dayOfWeek <= 6) return dayOfWeek;

        return 1;
    }

    private int timeToSlot(LocalTime start) {
        if (start == null) return -1;
        LocalTime CA2_START = LocalTime.of(9, 20);
        LocalTime CA3_START = LocalTime.of(12, 30);
        LocalTime CA4_START = LocalTime.of(15, 5);

        if (start.isBefore(CA2_START)) return 1;
        if (start.isBefore(CA3_START)) return 2;
        if (start.isBefore(CA4_START)) return 3;
        return 4;
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
