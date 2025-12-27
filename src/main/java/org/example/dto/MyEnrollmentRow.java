package org.example.dto;

public class MyEnrollmentRow {
    private String classId;
    private String subjectId;
    private String subjectName;
    private int credit;
    private short termNo;
    private String status;

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public int getCredit() { return credit; }
    public void setCredit(int credit) { this.credit = credit; }
    public short getTermNo() { return termNo; }
    public void setTermNo(short termNo) { this.termNo = termNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
