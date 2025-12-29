package org.example.controller.student;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.dto.ScheduleRow;
import org.example.service.SessionContext;
import org.example.service.StudentScheduleService;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentScheduleOpenController {

    @FXML private Label termLabel;
    @FXML private GridPane timetableGrid;

    private final StudentScheduleService scheduleService = new StudentScheduleService();
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final Map<String, StackPane> cellMap = new HashMap<>();

    @FXML
    public void initialize() {
        // ✅ bỏ phụ thuộc CSS file: set style inline
        buildEmptyGrid();

        String studentId = SessionContext.getUsername();
        var open = scheduleService.getOpenTerm();
        termLabel.setText("Kỳ tiếp theo: " + open);

        List<ScheduleRow> rows = scheduleService.getScheduleOpenTerm(studentId);
        fill(rows);
    }

    private void buildEmptyGrid() {
        timetableGrid.getChildren().clear();
        cellMap.clear();

        // style nền chung cho grid
        timetableGrid.setHgap(10);
        timetableGrid.setVgap(10);
        timetableGrid.setStyle(
                "-fx-padding: 14;" +
                        "-fx-background-color: #FFFFFF;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #E5E7EB;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;"
        );

        // Header row
        String[] days = {"", "T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        for (int c = 0; c < days.length; c++) {
            StackPane p = new StackPane();

            if (c == 0) {
                // ô góc trái
                p.setStyle(
                        "-fx-background-color: #F3F4F6;" +
                                "-fx-background-radius: 10;" +
                                "-fx-border-color: #E5E7EB;" +
                                "-fx-border-radius: 10;" +
                                "-fx-border-width: 1;" +
                                "-fx-min-height: 44;"
                );
            } else {
                p.setStyle(
                        "-fx-background-color: #E6F4F1;" +
                                "-fx-background-radius: 10;" +
                                "-fx-border-color: #BFE6DD;" +
                                "-fx-border-radius: 10;" +
                                "-fx-border-width: 1;" +
                                "-fx-min-height: 44;"
                );
                Label lb = new Label(days[c]);
                lb.setStyle("-fx-font-weight: 700; -fx-text-fill: #0F766E;");
                p.getChildren().add(lb);
            }

            timetableGrid.add(p, c, 0);
        }

        // Slot labels + cells
        String[] slots = {
                "Ca 1 (Tiết 1–3)\n06:45–09:10",
                "Ca 2 (Tiết 4–6)\n09:20–11:45",
                "Ca 3 (Tiết 7–9)\n12:30–14:55",
                "Ca 4 (Tiết 10–12)\n15:05–17:30"
        };

        for (int r = 1; r <= 4; r++) {
            // slot label col 0
            StackPane slotPane = new StackPane();
            slotPane.setStyle(
                    "-fx-background-color: #F8FAFC;" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-color: #E5E7EB;" +
                            "-fx-border-radius: 12;" +
                            "-fx-border-width: 1;" +
                            "-fx-padding: 10;"
            );

            Label lb = new Label(slots[r - 1]);
            lb.setWrapText(true);
            lb.setStyle("-fx-font-weight: 600; -fx-text-fill: #111827;");
            slotPane.getChildren().add(lb);

            timetableGrid.add(slotPane, 0, r);

            // cells col 1..7
            for (int c = 1; c <= 7; c++) {
                StackPane cell = new StackPane();
                cell.setStyle(
                        "-fx-background-color: #FFFFFF;" +
                                "-fx-background-radius: 14;" +
                                "-fx-border-color: #E5E7EB;" +
                                "-fx-border-radius: 14;" +
                                "-fx-border-width: 1;" +
                                "-fx-padding: 10;"
                );

                timetableGrid.add(cell, c, r);
                cellMap.put(key(r, c), cell);
            }
        }
    }

    private void fill(List<ScheduleRow> rows) {
        for (ScheduleRow r : rows) {
            int col = mapDayToCol(r.getDayOfWeek());
            int row = mapTimeToSlot(r);

            StackPane cell = cellMap.get(key(row, col));
            if (cell == null) continue;

            VBox item = new VBox(6);

            // ✅ card style (inline)
            item.setStyle(
                    "-fx-background-color: #ECFDF5;" +
                            "-fx-border-color: #A7F3D0;" +
                            "-fx-border-width: 1;" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-radius: 12;" +
                            "-fx-padding: 10;" +
                            "-fx-cursor: hand;"
            );

            // hover nhẹ
            item.setOnMouseEntered(e ->
                    item.setStyle(
                            "-fx-background-color: #D1FAE5;" +
                                    "-fx-border-color: #6EE7B7;" +
                                    "-fx-border-width: 1;" +
                                    "-fx-background-radius: 12;" +
                                    "-fx-border-radius: 12;" +
                                    "-fx-padding: 10;" +
                                    "-fx-cursor: hand;"
                    )
            );
            item.setOnMouseExited(e ->
                    item.setStyle(
                            "-fx-background-color: #ECFDF5;" +
                                    "-fx-border-color: #A7F3D0;" +
                                    "-fx-border-width: 1;" +
                                    "-fx-background-radius: 12;" +
                                    "-fx-border-radius: 12;" +
                                    "-fx-padding: 10;" +
                                    "-fx-cursor: hand;"
                    )
            );

            Label subject = new Label(shorten(r.getSubjectName(), 22));
            subject.setStyle("-fx-font-weight: 800; -fx-text-fill: #065F46;");

            Label room = new Label("Phòng: " + nullToDots(r.getRoom()));
            room.setStyle("-fx-text-fill: #064E3B;");

            Label clazz = new Label("Lớp: " + nullToDots(r.getClassId()));
            clazz.setStyle("-fx-text-fill: #064E3B;");

            item.getChildren().addAll(subject, room, clazz);

            item.setOnMouseClicked(e -> showDetail(r));

            cell.getChildren().add(item);
        }
    }

    private void showDetail(ScheduleRow r) {
        String time = (r.getStartTime() == null || r.getEndTime() == null)
                ? "(không rõ)"
                : r.getStartTime().format(TIME_FMT) + " - " + r.getEndTime().format(TIME_FMT);

        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Chi tiết lịch học");
        a.setHeaderText(r.getSubjectName());
        a.setContentText(
                "Mã lớp: " + nullToDots(r.getClassId()) + "\n" +
                        "Thứ: " + dayLabel(r.getDayOfWeek()) + "\n" +
                        "Giờ: " + time + "\n" +
                        "Phòng: " + nullToDots(r.getRoom())
        );
        a.showAndWait();
    }

    // ===== helpers =====

    // map day -> col (1..7)
    private int mapDayToCol(int dayOfWeek) {
        // nếu DB bạn dùng 2..8 (T2..CN) thì:
        // 2->1, 3->2, ... 7->6, 8->7
        if (dayOfWeek == 8) return 7;
        if (dayOfWeek >= 2 && dayOfWeek <= 7) return dayOfWeek - 1;

        // nếu lỡ DB dùng ISO 1..7 (Mon..Sun)
        if (dayOfWeek == 7) return 7; // Sun
        if (dayOfWeek >= 1 && dayOfWeek <= 6) return dayOfWeek;

        return 1;
    }

    private int mapTimeToSlot(ScheduleRow r) {
        if (r.getStartTime() == null) return 1;
        int h = r.getStartTime().getHour();
        if (h < 9) return 1;
        if (h < 12) return 2;
        if (h < 15) return 3;
        return 4;
    }

    private String key(int row, int col) { return row + ":" + col; }

    private String shorten(String s, int n) {
        if (s == null) return "";
        String t = s.trim();
        if (t.length() <= n) return t;
        return t.substring(0, n - 1) + "…";
    }

    private String nullToDots(String s) { return (s == null || s.isBlank()) ? "..." : s; }

    private String dayLabel(int d) {
        return switch (d) {
            case 2 -> "T2";
            case 3 -> "T3";
            case 4 -> "T4";
            case 5 -> "T5";
            case 6 -> "T6";
            case 7 -> "T7";
            case 8 -> "CN";
            // fallback nếu DB ISO 1..7
            case 1 -> "T2";
            default -> String.valueOf(d);
        };
    }
}
