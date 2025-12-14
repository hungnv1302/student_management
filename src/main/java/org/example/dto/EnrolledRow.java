package org.example.dto;

public class EnrolledRow {
    private final Long classId;
    private final String classCode;     // hoặc dùng classId format thành "LHPxxx"
    private final String subjectName;
    private final String timeText;      // "T2 7:00-9:00, T4 7:00-9:00"
    private final String status;

    public EnrolledRow(Long classId, String classCode, String subjectName, String timeText, String status) {
        this.classId = classId;
        this.classCode = classCode;
        this.subjectName = subjectName;
        this.timeText = timeText;
        this.status = status;
    }

    public Long getClassId() { return classId; }
    public String getClassCode() { return classCode; }
    public String getSubjectName() { return subjectName; }
    public String getTimeText() { return timeText; }
    public String getStatus() { return status; }
}
