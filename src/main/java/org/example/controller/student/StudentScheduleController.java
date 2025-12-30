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

public class StudentScheduleController {

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

        // ✅ Kỳ đang học
        StudentScheduleService.TermKey cur = service.getCurrentTerm();
        termLabel.setText("Thời khóa biểu kỳ đang học: " + cur);

        // ✅ load TKB kỳ đang học
        List<ScheduleRow> rows = service.getScheduleCurrentTerm(studentId);

        for (ScheduleRow r : rows) {
            int dayCol = isoDowToCol(r.getDayOfWeek());
            int slotRow = timeToSlot(r.getStartTime());
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
        applySubjectColor(box, r);
        box.setPadding(new javafx.geometry.Insets(8, 10, 8, 10));
        box.setMaxWidth(Double.MAX_VALUE);
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

        box.prefWidthProperty().bind(cell.widthProperty().subtract(12));
        Tooltip.install(box, new Tooltip(subjectName + (room.isBlank() ? "" : ("\nPhòng: " + room))));

        return box;
    }

    private void buildBaseGrid() {
        timetableGrid.getChildren().clear();
        timetableGrid.add(makeHeaderLabel(""), 0, 0);

        for (int d = 1; d <= 7; d++) {
            Label header = makeHeaderLabel(DAY_LABELS[d - 1]);
            timetableGrid.add(header, d, 0);
        }

        for (int s = 1; s <= 4; s++) {
            Label slot = makeSlotLabel(SLOT_LABELS[s - 1]);
            timetableGrid.add(slot, 0, s);
        }

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
        box.getStyleClass().add("slot-" + slotRow);
        box.setFillWidth(true);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private int isoDowToCol(int isoDow) { return isoDow; }

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

    private void applySubjectColor(VBox box, ScheduleRow r) {
        // ưu tiên classId (ổn định hơn), fallback subjectName
        String key = !blank(r.getClassId()) ? r.getClassId() : nvl(r.getSubjectName());

        int h = Math.abs(key.hashCode());

        // palette dịu mắt, không bị "giúm gió"
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

        box.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + c1 + ", " + c2 + ");" +
                        "-fx-border-color: " + border + ";" +
                        "-fx-border-width: 1 1 1 6;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-radius: 14;"
        );
    }

    private String hslToHex(double h, double s, double l) {
        // convert HSL -> RGB -> HEX
        double c = (1 - Math.abs(2 * l - 1)) * s;
        double x = c * (1 - Math.abs((h / 60.0) % 2 - 1));
        double m = l - c / 2.0;

        double r1=0, g1=0, b1=0;
        if (0 <= h && h < 60) { r1=c; g1=x; b1=0; }
        else if (60 <= h && h < 120) { r1=x; g1=c; b1=0; }
        else if (120 <= h && h < 180) { r1=0; g1=c; b1=x; }
        else if (180 <= h && h < 240) { r1=0; g1=x; b1=c; }
        else if (240 <= h && h < 300) { r1=x; g1=0; b1=c; }
        else { r1=c; g1=0; b1=x; }

        int r = (int)Math.round((r1 + m) * 255);
        int g = (int)Math.round((g1 + m) * 255);
        int b = (int)Math.round((b1 + m) * 255);

        return String.format("#%02X%02X%02X", clamp255(r), clamp255(g), clamp255(b));
    }

    private int clamp255(int v) { return Math.max(0, Math.min(255, v)); }

}
