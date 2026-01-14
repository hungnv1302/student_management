package org.example.dto;

import javafx.beans.property.*;

public class AssignedClassDto {
    private final StringProperty classId = new SimpleStringProperty();
    private final StringProperty subjectName = new SimpleStringProperty();
    private final IntegerProperty studentCount = new SimpleIntegerProperty();
    private final StringProperty timeInfo = new SimpleStringProperty();

    public AssignedClassDto() {}

    public AssignedClassDto(String classId, String subjectName, int studentCount, String timeInfo) {
        this.classId.set(classId);
        this.subjectName.set(subjectName);
        this.studentCount.set(studentCount);
        this.timeInfo.set(timeInfo);
    }

    public String getClassId() { return classId.get(); }
    public void setClassId(String v) { classId.set(v); }
    public StringProperty classIdProperty() { return classId; }

    public String getSubjectName() { return subjectName.get(); }
    public void setSubjectName(String v) { subjectName.set(v); }
    public StringProperty subjectNameProperty() { return subjectName; }

    public int getStudentCount() { return studentCount.get(); }
    public void setStudentCount(int v) { studentCount.set(v); }
    public IntegerProperty studentCountProperty() { return studentCount; }

    public String getTimeInfo() { return timeInfo.get(); }
    public void setTimeInfo(String v) { timeInfo.set(v); }
    public StringProperty timeInfoProperty() { return timeInfo; }
}
