package org.example.controller.lecturer;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.example.dto.LecturerScheduleSlotDto;
import org.example.repository.LecturerTimetableRepository;
import org.example.service.LecturerTimetableService;
import org.example.service.SessionContext;

import java.util.*;

public class LecturerTimetableController {

    @FXML private Label termPill; // đúng fx:id trong FXML
    @FXML private GridPane grid;  // đúng fx:id trong FXML

    private final LecturerTimetableService service = new LecturerTimetableService();

    // DB: 2..8 (8=CN)
    private static final int[] DAYS = {2,3,4,5,6,7,8};
    private static final String[] DAY_LABELS = {"T2","T3","T4","T5","T6","T7","CN"};

    @FXML
    public void initialize() {
        reload();
    }

    @FXML
    public void reload() {
        if (!SessionContext.isLecturer()) {
            alert(Alert.AlertType.WARNING, "Không có quyền", "Role hiện tại không phải LECTURER.");
            return;
        }

        String lecturerId = SessionContext.getUsername(); // phải là "20180001"...
        if (lecturerId == null || lecturerId.isBlank()) {
            alert(Alert.AlertType.ERROR, "Session lỗi", "SessionContext.username đang null/blank.");
            return;
        }

        LecturerTimetableRepository.Term term = service.getCurrentTermOrThrow();
        termPill.setText("Kỳ: " + term.year() + "." + term.sem());

        LinkedHashMap<Integer, String> shiftRanges = service.getShiftRanges();  // ca -> giờ
        List<LecturerScheduleSlotDto> slots = service.getSlots(lecturerId, term.year(), term.sem());

        buildGrid(shiftRanges, slots);

        if (slots.isEmpty()) {
            alert(Alert.AlertType.INFORMATION, "Không có dữ liệu",
                    "Không có lịch dạy cho GV " + lecturerId + " trong kỳ " + term.year() + "." + term.sem()
                            + "\n\nKiểm tra nhanh:"
                            + "\n1) Bạn có login đúng lecturer_id (vd 20180001) không?"
                            + "\n2) registration_config(id=1) có đúng kỳ có lớp không?");
        }
    }

    private void buildGrid(LinkedHashMap<Integer, String> shiftRanges, List<LecturerScheduleSlotDto> slots) {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();

        // column constraints
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(190);
        c0.setPrefWidth(220);
        c0.setHgrow(Priority.NEVER);
        grid.getColumnConstraints().add(c0);

        for (int i = 0; i < 7; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setMinWidth(150);
            c.setPrefWidth(180);
            c.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(c);
        }

        // header row
        grid.add(headerCell(""), 0, 0);
        for (int i = 0; i < 7; i++) {
            grid.add(headerCell(DAY_LABELS[i]), i + 1, 0);
        }

        // index slot by "day:shift"
        Map<String, List<LecturerScheduleSlotDto>> idx = new HashMap<>();
        for (var s : slots) {
            String key = s.getDayOfWeek() + ":" + s.getShiftNo();
            idx.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
        }

        int row = 1;
        for (var e : shiftRanges.entrySet()) {
            int shiftNo = e.getKey();
            String range = e.getValue();

            grid.add(shiftCell(shiftNo, range), 0, row);

            for (int col = 0; col < 7; col++) {
                int day = DAYS[col];
                String key = day + ":" + shiftNo;
                List<LecturerScheduleSlotDto> cellSlots = idx.getOrDefault(key, List.of());

                StackPane cell = emptyCell();

                VBox stack = new VBox(8);
                stack.setFillWidth(true);
                for (var s : cellSlots) {
                    stack.getChildren().add(classCard(s));
                }

                cell.getChildren().add(stack);
                grid.add(cell, col + 1, row);
            }
            row++;
        }
    }

    private StackPane headerCell(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 14; -fx-font-weight: 800; -fx-text-fill: #0E6B5A;");
        StackPane p = new StackPane(l);
        p.setStyle("-fx-background-color: #DFF7F0; -fx-background-radius: 14; -fx-padding: 10 12;");
        return p;
    }

    private StackPane shiftCell(int shiftNo, String range) {
        Label a = new Label("Ca " + shiftNo);
        a.setStyle("-fx-font-size: 14; -fx-font-weight: 800; -fx-text-fill: #0E6B5A;");

        Label b = new Label(range);
        b.setStyle("-fx-font-size: 13; -fx-font-weight: 700; -fx-text-fill: #111827;");

        VBox box = new VBox(4, a, b);
        StackPane p = new StackPane(box);
        p.setStyle("-fx-background-color: #E8FFF7; -fx-background-radius: 14; -fx-padding: 12;");
        return p;
    }

    private StackPane emptyCell() {
        StackPane p = new StackPane();
        p.setMinHeight(95);
        p.setStyle("""
            -fx-background-color: #FBFEFD;
            -fx-border-color: #DDE7E4;
            -fx-border-radius: 14;
            -fx-background-radius: 14;
            -fx-padding: 10;
        """);
        return p;
    }

    private StackPane classCard(LecturerScheduleSlotDto s) {
        Label title = new Label(s.getSubjectName());
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 14; -fx-font-weight: 800; -fx-text-fill: #0B2B28;");

        Label room = new Label("Phòng: " + (s.getRoom() == null ? "--" : s.getRoom()));
        room.setStyle("-fx-font-size: 12; -fx-font-weight: 700; -fx-text-fill: #6B7280;");

        VBox box = new VBox(6, title, room);

        StackPane card = new StackPane(box);
        card.setStyle("""
            -fx-background-color: #E9FBFF;
            -fx-background-radius: 14;
            -fx-padding: 12;
            -fx-border-color: #2CA6C6;
            -fx-border-width: 1;
            -fx-border-radius: 14;
        """);

        card.setBorder(new Border(new BorderStroke(
                Color.web("#2F6AF6"),
                BorderStrokeStyle.SOLID,
                new CornerRadii(14),
                new BorderWidths(0,0,0,4)
        )));
        return card;
    }

    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}
