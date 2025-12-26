package org.example.dto;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class EnrolledRow {
    private final String classId;      // varchar(6)
    private final String classCode;    // LHP + classId
    private final String subjectName;
    private final String timeText;
    private final String status;

    // ✅ checkbox
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public EnrolledRow(String classId, String subjectName, String timeText, String status) {
        this.classId = classId;
        this.classCode = "LHP" + classId;
        this.subjectName = subjectName;
        this.timeText = timeText;
        this.status = status;
    }

    public String getClassId() { return classId; }
    public String getClassCode() { return classCode; }
    public String getSubjectName() { return subjectName; }
    public String getTimeText() { return timeText; }
    public String getStatus() { return status; }

    // ✅ cho TableView bind checkbox
    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean v) { selected.set(v); }
    public BooleanProperty selectedProperty() { return selected; }
}
