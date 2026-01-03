package org.example.controller.lecturer;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.dto.AssignedClassDto;
import org.example.dto.StudentInClassDto;
import org.example.repository.TeachingScheduleRepository;
import org.example.service.SessionContext;
import org.example.service.TeachingScheduleService;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class LecturerTeachingScheduleController {

    @FXML private Label lecturerIdLabel;

    @FXML private ComboBox<Integer> yearCombo;
    @FXML private ComboBox<Integer> semCombo;

    @FXML private TableView<AssignedClassDto> assignedTable;
    @FXML private TableColumn<AssignedClassDto, String> colClassId;
    @FXML private TableColumn<AssignedClassDto, String> colSubjectName;
    @FXML private TableColumn<AssignedClassDto, Number> colStudentCount;
    @FXML private TableColumn<AssignedClassDto, String> colTimeInfo;
    @FXML private TableColumn<AssignedClassDto, Void> colAction;

    @FXML private TableView<StudentInClassDto> studentsTable;
    @FXML private TableColumn<StudentInClassDto, String> colStudentId;
    @FXML private TableColumn<StudentInClassDto, String> colFullName;
    @FXML private TableColumn<StudentInClassDto, String> colEmail;

    private final TeachingScheduleService service = new TeachingScheduleService();
    private String lecturerId; // resolved numeric id

    @FXML
    public void initialize() {
        colClassId.setCellValueFactory(d -> d.getValue().classIdProperty());
        colSubjectName.setCellValueFactory(d -> d.getValue().subjectNameProperty());
        colStudentCount.setCellValueFactory(d -> d.getValue().studentCountProperty());
        colTimeInfo.setCellValueFactory(d -> d.getValue().timeInfoProperty());

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Xem SV");
            {
                btn.setOnAction(evt -> {
                    AssignedClassDto row = getTableView().getItems().get(getIndex());
                    assignedTable.getSelectionModel().select(row);
                    loadStudents(row.getClassId());
                });
                btn.setMaxWidth(Double.MAX_VALUE);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        assignedTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) loadStudents(newV.getClassId());
        });

        colStudentId.setCellValueFactory(d -> d.getValue().studentIdProperty());
        colFullName.setCellValueFactory(d -> d.getValue().fullNameProperty());
        colEmail.setCellValueFactory(d -> d.getValue().emailProperty());

        if (semCombo != null) semCombo.setItems(FXCollections.observableArrayList(1, 2));

        bootstrap();
    }

    private void bootstrap() {
        studentsTable.setItems(FXCollections.observableArrayList());
        assignedTable.setItems(FXCollections.observableArrayList());

        if (!SessionContext.isLecturer()) {
            showAlert(Alert.AlertType.WARNING, "Không có quyền", "Bạn không có quyền truy cập màn hình giảng viên.");
            return;
        }

        String loginValue = SessionContext.getUsername();
        try {
            lecturerId = service.resolveLecturerIdOrThrow(loginValue);
            if (lecturerIdLabel != null) lecturerIdLabel.setText("Mã GV: " + lecturerId);

            // Load term list of this lecturer
            List<TeachingScheduleRepository.Term> terms = service.getTermsOfLecturer(lecturerId);

            Set<Integer> years = new LinkedHashSet<>();
            for (var t : terms) years.add(t.termYear());

            if (yearCombo != null) yearCombo.setItems(FXCollections.observableArrayList(years));

            // default: newest term of lecturer
            var data = service.getAssignedClassesDefault(lecturerId);
            assignedTable.setItems(FXCollections.observableArrayList(data));

            // set combos to newest term
            if (!terms.isEmpty()) {
                var newest = terms.get(0);
                if (yearCombo != null) yearCombo.getSelectionModel().select(Integer.valueOf(newest.termYear()));
                if (semCombo != null) semCombo.getSelectionModel().select(Integer.valueOf(newest.termSem()));
            }

            if (data.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Không có dữ liệu",
                        "Giảng viên chưa được phân công lớp nào.");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", e.getMessage());
        }
    }

    @FXML
    public void filterByTerm() {
        if (lecturerId == null) return;

        Integer y = yearCombo == null ? null : yearCombo.getValue();
        Integer s = semCombo == null ? null : semCombo.getValue();

        if (y == null || s == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu chọn", "Bạn cần chọn Năm và Kỳ.");
            return;
        }

        var data = service.getAssignedClassesInTerm(lecturerId, y, s);
        assignedTable.setItems(FXCollections.observableArrayList(data));
        studentsTable.setItems(FXCollections.observableArrayList());
    }

    @FXML
    public void showAllTerms() {
        if (lecturerId == null) return;
        var data = service.getAssignedClassesAllTerms(lecturerId);
        assignedTable.setItems(FXCollections.observableArrayList(data));
        studentsTable.setItems(FXCollections.observableArrayList());
    }

    @FXML
    public void refresh() {
        bootstrap();
    }

    private void loadStudents(String classId) {
        try {
            var data = service.getStudentsInClass(classId);
            studentsTable.setItems(FXCollections.observableArrayList(data));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi tải sinh viên", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}
