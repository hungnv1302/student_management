package org.example.dto;

public class OpenClassRow {
    private final Long classId;
    private final String subjectCode;
    private final String subjectName;
    private final Integer credits;
    private final String lecturerName;

    public OpenClassRow(Long classId, String subjectCode, String subjectName, Integer credits, String lecturerName) {
        this.classId = classId;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.credits = credits;
        this.lecturerName = lecturerName;
    }

    public Long getClassId() { return classId; }
    public String getSubjectCode() { return subjectCode; }
    public String getSubjectName() { return subjectName; }
    public Integer getCredits() { return credits; }
    public String getLecturerName() { return lecturerName; }
}
