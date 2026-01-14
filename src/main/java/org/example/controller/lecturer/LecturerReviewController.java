package org.example.controller.lecturer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.example.dto.LecturerReviewDTO;
import org.example.repository.LecturerReviewRepository;
import org.example.service.LecturerReviewService;
import org.example.service.SessionContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class LecturerReviewController {

    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<LecturerReviewDTO> reviewTable;
    @FXML private Label statusLabel;

    @FXML private TableColumn<LecturerReviewDTO, Integer> colRequestId;
    @FXML private TableColumn<LecturerReviewDTO, String> colStudentId;
    @FXML private TableColumn<LecturerReviewDTO, String> colStudentName;
    @FXML private TableColumn<LecturerReviewDTO, String> colClassId;
    @FXML private TableColumn<LecturerReviewDTO, String> colSubjectName;
    @FXML private TableColumn<LecturerReviewDTO, BigDecimal> colOldTotal;
    @FXML private TableColumn<LecturerReviewDTO, String> colStatus;
    @FXML private TableColumn<LecturerReviewDTO, LocalDateTime> colCreatedAt;

    private final LecturerReviewService service = new LecturerReviewService();
    private final ObservableList<LecturerReviewDTO> data = FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        // Setup status filter
        statusFilter.setItems(FXCollections.observableArrayList(
                "Tất cả", "Chờ xử lý", "Chấp nhận", "Từ chối"
        ));
        statusFilter.setValue("Chờ xử lý");

        // Setup table
        reviewTable.setItems(data);

        colRequestId.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        colStudentId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colClassId.setCellValueFactory(new PropertyValueFactory<>("classId"));
        colSubjectName.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        colOldTotal.setCellValueFactory(new PropertyValueFactory<>("oldTotal"));
        colStatus.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatusText())
        );
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Format date column
        colCreatedAt.setCellFactory(col -> new TableCell<LecturerReviewDTO, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DATE_FORMATTER));
                }
            }
        });

        // Format status column with colors
        colStatus.setCellFactory(col -> new TableCell<LecturerReviewDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String style = switch (item) {
                        case "Chờ xử lý" -> "-fx-text-fill: #F57C00; -fx-font-weight: bold;";
                        case "Chấp nhận" -> "-fx-text-fill: #388E3C; -fx-font-weight: bold;";
                        case "Từ chối" -> "-fx-text-fill: #D32F2F; -fx-font-weight: bold;";
                        default -> "";
                    };
                    setStyle(style);
                }
            }
        });

        // Format score column
        colOldTotal.setCellFactory(col -> new TableCell<LecturerReviewDTO, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.stripTrailingZeros().toPlainString());
                }
            }
        });

        // Double click to view detail
        reviewTable.setRowFactory(tv -> {
            TableRow<LecturerReviewDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleViewDetail();
                }
            });
            return row;
        });

        // Auto load
        handleLoad();
    }

    @FXML
    public void handleLoad() {
        String lecturerId = SessionContext.getUserId();

        if (lecturerId == null || lecturerId.isBlank()) {
            show(Alert.AlertType.ERROR, "Lỗi phiên làm việc",
                    "Không xác định được giảng viên. Vui lòng đăng nhập lại.");
            return;
        }

        try {
            String filterValue = mapFilterToStatus(statusFilter.getValue());
            var requests = service.getReviewRequests(lecturerId, filterValue);
            data.setAll(requests);

            long pending = requests.stream()
                    .filter(LecturerReviewDTO::isPending)
                    .count();

            statusLabel.setText(String.format(
                    "Tổng: %d yêu cầu | Chờ xử lý: %d",
                    requests.size(), pending
            ));
            statusLabel.setStyle("-fx-text-fill: #2E7D32;");

        } catch (Exception e) {
            statusLabel.setText("Lỗi tải dữ liệu");
            statusLabel.setStyle("-fx-text-fill: #C62828;");
            show(Alert.AlertType.ERROR, "Lỗi", e.getMessage());
        }
    }

    @FXML
    public void handleViewDetail() {
        LecturerReviewDTO selected = reviewTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            show(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một yêu cầu phúc tra");
            return;
        }

        showDetailDialog(selected);
    }

    @FXML
    public void handleApprove() {
        LecturerReviewDTO selected = reviewTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            show(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một yêu cầu phúc tra");
            return;
        }

        if (!selected.isPending()) {
            show(Alert.AlertType.WARNING, "Không thể xử lý",
                    "Yêu cầu này đã được xử lý trước đó");
            return;
        }

        showApproveDialog(selected);
    }

    @FXML
    public void handleReject() {
        LecturerReviewDTO selected = reviewTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            show(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một yêu cầu phúc tra");
            return;
        }

        if (!selected.isPending()) {
            show(Alert.AlertType.WARNING, "Không thể xử lý",
                    "Yêu cầu này đã được xử lý trước đó");
            return;
        }

        showRejectDialog(selected);
    }

    // ===== Helper Methods =====

    private void showDetailDialog(LecturerReviewDTO dto) {
        try {
            // Lấy điểm chi tiết
            var score = service.getEnrollmentScore(dto.getEnrollId());

            StringBuilder detail = new StringBuilder();
            detail.append("MÃ YÊU CẦU: ").append(dto.getRequestId()).append("\n");
            detail.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

            detail.append("THÔNG TIN SINH VIÊN:\n");
            detail.append("  • Họ tên: ").append(dto.getStudentName()).append("\n");
            detail.append("  • Mã SV: ").append(dto.getStudentId()).append("\n");
            detail.append("  • Lớp: ").append(dto.getClassId()).append("\n");
            detail.append("  • Môn học: ").append(dto.getSubjectName()).append("\n\n");

            detail.append("THÔNG TIN ĐIỂM:\n");
            if (score != null) {
                detail.append("  • Điểm giữa kỳ: ").append(formatScore(score.midterm)).append("\n");
                detail.append("  • Điểm cuối kỳ: ").append(formatScore(score.finalScore)).append("\n");
            }
            detail.append("  • Điểm tổng kết hiện tại: ").append(formatScore(dto.getOldTotal())).append("\n\n");

            detail.append("LÝ DO PHÚC TRA:\n");
            detail.append(dto.getReason()).append("\n\n");

            detail.append("THÔNG TIN XỬ LÝ:\n");
            detail.append("  • Ngày gửi: ").append(dto.getCreatedAt().format(DATE_FORMATTER)).append("\n");
            detail.append("  • Trạng thái: ").append(dto.getStatusText()).append("\n");

            if (dto.getNewTotal() != null) {
                detail.append("  • Điểm sau phúc tra: ").append(formatScore(dto.getNewTotal())).append("\n");
            }

            if (dto.getNote() != null && !dto.getNote().isBlank()) {
                detail.append("  • Ghi chú của GV: ").append(dto.getNote()).append("\n");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Chi tiết yêu cầu phúc tra");
            alert.setHeaderText("Yêu cầu #" + dto.getRequestId() + " - " + dto.getStudentName());

            TextArea textArea = new TextArea(detail.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefRowCount(20);
            textArea.setPrefColumnCount(50);

            alert.getDialogPane().setContent(textArea);
            alert.getDialogPane().setMinWidth(600);
            alert.getDialogPane().setMinHeight(500);

            alert.showAndWait();

        } catch (Exception e) {
            show(Alert.AlertType.ERROR, "Lỗi", "Không thể tải chi tiết: " + e.getMessage());
        }
    }

    private void showApproveDialog(LecturerReviewDTO dto) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Chấp nhận yêu cầu phúc tra");
        dialog.setHeaderText("Cập nhật điểm cho sinh viên: " + dto.getStudentName() +
                " (" + dto.getStudentId() + ")");

        // Buttons
        ButtonType confirmButtonType = new ButtonType("Chấp nhận", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        // Content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Label infoLabel = new Label("Môn học: " + dto.getSubjectName());
        infoLabel.setStyle("-fx-font-weight: bold;");

        TextField newScoreField = new TextField();
        newScoreField.setPromptText("Nhập điểm từ 0.00 đến 10.00");

        TextArea noteArea = new TextArea();
        noteArea.setPromptText("Nhập ghi chú cho sinh viên (tùy chọn)");
        noteArea.setPrefRowCount(3);
        noteArea.setWrapText(true);

        int row = 0;
        grid.add(infoLabel, 0, row++, 2, 1);
        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(new Label("Điểm hiện tại:"), 0, row);
        grid.add(new Label(formatScore(dto.getOldTotal())), 1, row++);
        grid.add(new Label("Điểm mới: *"), 0, row);
        grid.add(newScoreField, 1, row++);
        grid.add(new Label("Ghi chú:"), 0, row);
        grid.add(noteArea, 1, row++);

        dialog.getDialogPane().setContent(grid);

        // Validate and process
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == confirmButtonType) {
            try {
                String scoreText = newScoreField.getText().trim();
                if (scoreText.isEmpty()) {
                    show(Alert.AlertType.WARNING, "Lỗi nhập liệu", "Vui lòng nhập điểm mới");
                    showApproveDialog(dto); // Hiển thị lại dialog
                    return;
                }

                BigDecimal newScore = new BigDecimal(scoreText);

                // Validate range
                if (newScore.compareTo(BigDecimal.ZERO) < 0 ||
                        newScore.compareTo(BigDecimal.TEN) > 0) {
                    show(Alert.AlertType.WARNING, "Lỗi nhập liệu",
                            "Điểm phải trong khoảng 0-10");
                    showApproveDialog(dto); // Hiển thị lại dialog
                    return;
                }

                String lecturerId = SessionContext.getUserId();
                String note = noteArea.getText().trim();

                // Confirm
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setHeaderText("Xác nhận chấp nhận");
                confirm.setContentText(
                        "Bạn chắc chắn muốn chấp nhận yêu cầu này?\n\n" +
                                "Điểm cũ: " + formatScore(dto.getOldTotal()) + "\n" +
                                "Điểm mới: " + formatScore(newScore) + "\n\n" +
                                "Thao tác này không thể hoàn tác!"
                );

                Optional<ButtonType> confirmResult = confirm.showAndWait();
                if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                    service.approveRequest(dto.getRequestId(), lecturerId, newScore,
                            note.isEmpty() ? null : note);

                    show(Alert.AlertType.INFORMATION, "Thành công",
                            "Đã chấp nhận yêu cầu phúc tra và cập nhật điểm mới:\n" +
                                    "Điểm cũ: " + formatScore(dto.getOldTotal()) + " → " +
                                    "Điểm mới: " + formatScore(newScore));
                    handleLoad();
                }

            } catch (NumberFormatException e) {
                show(Alert.AlertType.WARNING, "Lỗi định dạng",
                        "Điểm không hợp lệ. Vui lòng nhập số thập phân (VD: 8.5)");
                showApproveDialog(dto); // Hiển thị lại dialog
            } catch (Exception e) {
                show(Alert.AlertType.ERROR, "Lỗi", e.getMessage());
            }
        }
    }

    private void showRejectDialog(LecturerReviewDTO dto) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Từ chối yêu cầu phúc tra");
        dialog.setHeaderText("Từ chối yêu cầu của sinh viên: " + dto.getStudentName() +
                " (" + dto.getStudentId() + ")");

        // Buttons
        ButtonType confirmButtonType = new ButtonType("Từ chối", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        // Content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Label infoLabel = new Label("Môn học: " + dto.getSubjectName());
        infoLabel.setStyle("-fx-font-weight: bold;");

        Label reasonLabel = new Label("Lý do của sinh viên:");
        TextArea studentReasonArea = new TextArea(dto.getReason());
        studentReasonArea.setEditable(false);
        studentReasonArea.setPrefRowCount(3);
        studentReasonArea.setWrapText(true);
        studentReasonArea.setStyle("-fx-background-color: #F5F5F5;");

        Label rejectLabel = new Label("Lý do từ chối: *");
        TextArea rejectReasonArea = new TextArea();
        rejectReasonArea.setPromptText("Nhập lý do từ chối yêu cầu phúc tra này...");
        rejectReasonArea.setPrefRowCount(4);
        rejectReasonArea.setWrapText(true);

        int row = 0;
        grid.add(infoLabel, 0, row++, 2, 1);
        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(reasonLabel, 0, row++, 2, 1);
        grid.add(studentReasonArea, 0, row++, 2, 1);
        grid.add(new Label(), 0, row++); // Spacer
        grid.add(rejectLabel, 0, row++, 2, 1);
        grid.add(rejectReasonArea, 0, row++, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setMinWidth(500);

        // Process result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return rejectReasonArea.getText().trim();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(note -> {
            if (note.isEmpty()) {
                show(Alert.AlertType.WARNING, "Lỗi nhập liệu",
                        "Vui lòng nhập lý do từ chối");
                showRejectDialog(dto); // Hiển thị lại dialog
                return;
            }

            // Confirm
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setHeaderText("Xác nhận từ chối");
            confirm.setContentText(
                    "Bạn chắc chắn muốn từ chối yêu cầu này?\n\n" +
                            "Sinh viên: " + dto.getStudentName() + "\n" +
                            "Môn học: " + dto.getSubjectName() + "\n\n" +
                            "Lý do từ chối sẽ được gửi đến sinh viên."
            );

            Optional<ButtonType> confirmResult = confirm.showAndWait();
            if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                try {
                    String lecturerId = SessionContext.getUserId();
                    service.rejectRequest(dto.getRequestId(), lecturerId, note);

                    show(Alert.AlertType.INFORMATION, "Thành công",
                            "Đã từ chối yêu cầu phúc tra");
                    handleLoad();

                } catch (Exception e) {
                    show(Alert.AlertType.ERROR, "Lỗi", e.getMessage());
                }
            }
        });
    }

    private String mapFilterToStatus(String filterText) {
        return switch (filterText) {
            case "Chờ xử lý" -> "PENDING";
            case "Chấp nhận" -> "APPROVED";
            case "Từ chối" -> "REJECTED";
            default -> "ALL";
        };
    }

    private String formatScore(BigDecimal score) {
        return score == null ? "N/A" : score.stripTrailingZeros().toPlainString();
    }

    private void show(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}