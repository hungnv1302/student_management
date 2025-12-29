package org.example.controller.student;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.example.dto.GradeRow;
import org.example.repository.DbFn;
import org.example.service.ScoreReviewRequestService;
import org.example.service.SessionContext;
import org.example.service.StudentGradeService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class StudentScoresController {

    @FXML private Label subTitleLabel;
    @FXML private Accordion termAccordion;

    @FXML private VBox reviewFormArea;
    @FXML private Label reviewInfoLabel;
    @FXML private TextArea reasonTextArea;
    @FXML private Button submitReviewButton;
    @FXML private Button cancelReviewButton;

    @FXML private Label cpaValueLabel;
    @FXML private ComboBox<String> termComboBox;
    @FXML private Label gpaValueLabel;

    private final StudentGradeService gradeService = new StudentGradeService();
    private final ScoreReviewRequestService reviewService = new ScoreReviewRequestService();

    private Map<String, List<GradeRow>> byTerm = new LinkedHashMap<>();
    private List<GradeRow> allShown = new ArrayList<>();
    private List<GradeRow> finalizedAll = new ArrayList<>();

    private TermKey currentTerm;
    private GradeRow selectedForReview;

    private record TermKey(int year, short sem) {}
    private static int cmpTerm(int y1, short s1, int y2, short s2) {
        if (y1 != y2) return Integer.compare(y1, y2);
        return Short.compare(s1, s2);
    }

    @FXML
    private void submitReviewHandle() {
        submitReview();
    }

    @FXML
    private void cancelReviewHandle() {
        hideReviewForm();
    }

    @FXML
    public void initialize() {
        hideReviewForm();

        cancelReviewButton.setOnAction(e -> hideReviewForm());
        submitReviewButton.setOnAction(e -> submitReview());

        termComboBox.setOnAction(e -> updateGpaBySelectedTerm());

        loadGradesAndBuildUI();
    }

    private void loadGradesAndBuildUI() {
        String studentId = SessionContext.getUsername();
        if (studentId == null || studentId.isBlank()) {
            renderEmpty("Không xác định sinh viên (chưa đăng nhập).");
            return;
        }

        currentTerm = getCurrentTermFromDb(); // <= 2024.2 (nếu open=2025.1)

        List<GradeRow> all;
        try {
            all = gradeService.getStudentGrades(studentId);
        } catch (Exception ex) {
            renderEmpty("Không tải được dữ liệu điểm.");
            showError("Lỗi tải dữ liệu", ex.getMessage());
            return;
        }

        // 1) Chỉ hiển thị đến KỲ HIỆN TẠI (không show 2025.1)
        allShown = all.stream()
                .filter(r -> cmpTerm(r.getTermYear(), r.getTermSem(), currentTerm.year, currentTerm.sem) <= 0)
                .collect(Collectors.toList());

        if (allShown.isEmpty()) {
            renderEmpty("Chưa có dữ liệu điểm.");
            return;
        }

        // 2) CPA chỉ tính môn đã chốt
        finalizedAll = allShown.stream()
                .filter(r -> Boolean.TRUE.equals(r.getFinalized()))
                .collect(Collectors.toList());

        // 3) group theo kỳ (kể cả kỳ hiện tại chưa đủ điểm)
        byTerm = allShown.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getTermYear() + "." + r.getTermSem(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // ===== Accordion =====
        termAccordion.getPanes().clear();

        for (Map.Entry<String, List<GradeRow>> entry : byTerm.entrySet()) {
            String termKey = entry.getKey();
            List<GradeRow> rows = entry.getValue();

            TableView<GradeRow> table = createGradesTable();
            table.setItems(FXCollections.observableArrayList(rows));

            TitledPane pane = new TitledPane("Kỳ " + termKey, table);
            pane.setExpanded(false);
            termAccordion.getPanes().add(pane);
        }

        // ===== CPA =====
        cpaValueLabel.setText(calcGpaFromRows(finalizedAll));

        // ===== GPA theo kỳ (tính trên các môn có point4) =====
        List<String> terms = new ArrayList<>(byTerm.keySet());
        termComboBox.setItems(FXCollections.observableArrayList(terms));

        String latest = terms.get(terms.size() - 1); // term mới nhất <= current
        termComboBox.getSelectionModel().select(latest);
        updateGpaBySelectedTerm();

        subTitleLabel.setText("Điểm theo từng kỳ.");
    }

    private void updateGpaBySelectedTerm() {
        String term = termComboBox.getSelectionModel().getSelectedItem();
        if (term == null) {
            gpaValueLabel.setText("--");
            return;
        }
        List<GradeRow> rows = byTerm.getOrDefault(term, Collections.emptyList());
        gpaValueLabel.setText(calcGpaFromRows(rows));
    }

    private String calcGpaFromRows(List<GradeRow> rows) {
        BigDecimal sum = BigDecimal.ZERO;
        int credits = 0;

        for (GradeRow r : rows) {
            if (r.getPoint4() == null || r.getCredit() == null) continue;
            sum = sum.add(r.getPoint4().multiply(BigDecimal.valueOf(r.getCredit())));
            credits += r.getCredit();
        }

        if (credits == 0) return "--";
        return sum.divide(BigDecimal.valueOf(credits), 2, RoundingMode.HALF_UP).toPlainString();
    }

    private TableView<GradeRow> createGradesTable() {
        TableView<GradeRow> table = new TableView<>();
        table.setPrefHeight(280);

        table.getColumns().addAll(
                col("Mã lớp", "classId", 110),
                col("Mã môn", "subjectId", 90),
                col("Tên môn", "subjectName", 260),
                col("TC", "credit", 45),
                col("Giữa kỳ", "midtermScore", 85),
                col("Cuối kỳ", "finalScore", 85),
                col("Tổng kết", "totalScore", 85),
                col("Chữ", "letter", 60),
                col("Thang 4", "point4", 70)
        );

        // ===== Cột PHÚC TRA =====
        TableColumn<GradeRow, Void> reviewCol = new TableColumn<>("Phúc tra");
        reviewCol.setPrefWidth(90);

        reviewCol.setCellFactory(new Callback<>() {
            @Override public TableCell<GradeRow, Void> call(TableColumn<GradeRow, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Phúc tra");
                    {
                        btn.setStyle("-fx-background-color: #00796B; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                        btn.setOnAction(e -> {
                            GradeRow r = getTableView().getItems().get(getIndex());
                            openReviewForm(r);
                        });
                    }

                    @Override protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) { setGraphic(null); return; }

                        GradeRow r = getTableView().getItems().get(getIndex());

                        boolean isCurrent = (r.getTermYear() == currentTerm.year && r.getTermSem() == currentTerm.sem);
                        boolean has2Scores = (r.getMidtermScore() != null && r.getFinalScore() != null);

                        // chỉ cho phúc tra ở kỳ hiện tại và khi đã có đủ 2 điểm
                        if (isCurrent && has2Scores && r.getEnrollId() != null) {
                            setGraphic(btn);
                        } else {
                            setGraphic(null);
                        }
                    }
                };
            }
        });

        table.getColumns().add(reviewCol);
        return table;
    }

    private <T> TableColumn<GradeRow, T> col(String title, String prop, double w) {
        TableColumn<GradeRow, T> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(w);
        return c;
    }

    private void openReviewForm(GradeRow r) {
        selectedForReview = r;
        reasonTextArea.clear();

        reviewInfoLabel.setText(
                "MSSV: " + SessionContext.getUsername()
                        + " | Lớp: " + r.getClassId()
                        + " | Môn: " + r.getSubjectName()
                        + " | EnrollID: " + r.getEnrollId()
        );

        reviewFormArea.setManaged(true);
        reviewFormArea.setVisible(true);
    }

    private void hideReviewForm() {
        selectedForReview = null;
        reviewFormArea.setVisible(false);
        reviewFormArea.setManaged(false);
        reasonTextArea.clear();
        reviewInfoLabel.setText("(Thông tin lớp/môn sẽ hiện ở đây)");
    }

    private void submitReview() {
        String studentId = SessionContext.getUsername();
        if (selectedForReview == null || selectedForReview.getEnrollId() == null) {
            showError("Phúc tra", "Không xác định được lớp để phúc tra.");
            return;
        }

        String reason = reasonTextArea.getText();
        try {
            int requestId = reviewService.createScoreReviewRequest(
                    studentId,
                    selectedForReview.getEnrollId(),
                    reason
            );
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Phúc tra");
            a.setHeaderText(null);
            a.setContentText("Đã gửi yêu cầu phúc tra (request_id=" + requestId + ").");
            a.showAndWait();
            hideReviewForm();
        } catch (Exception ex) {
            showError("Phúc tra thất bại", ex.getMessage());
        }
    }

    private TermKey getCurrentTermFromDb() {
        try {
            // open term = kỳ đăng ký (vd 2025.1)
            TermKey open = DbFn.queryOne(
                    "SELECT term_year, term_sem FROM qlsv.registration_config WHERE id=1",
                    null,
                    rs -> new TermKey(rs.getInt("term_year"), rs.getShort("term_sem"))
            );

            if (open == null) return new TermKey(0, (short) 0);

            int y = open.year;
            short s = open.sem;
            // current term = kỳ trước của open term
            if (s == 1) return new TermKey(y - 1, (short) 2);
            return new TermKey(y, (short) 1);

        } catch (SQLException e) {
            throw new RuntimeException("DB error getCurrentTerm: " + e.getMessage(), e);
        }
    }

    private void renderEmpty(String msg) {
        subTitleLabel.setText(msg);
        termAccordion.getPanes().clear();
        cpaValueLabel.setText("--");
        gpaValueLabel.setText("--");
        termComboBox.setItems(FXCollections.observableArrayList());
        hideReviewForm();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg == null ? "(không rõ lỗi)" : msg);
        a.showAndWait();
    }
}
