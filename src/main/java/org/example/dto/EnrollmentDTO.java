package org.example.dto;

public class EnrollmentDTO {
    private String enrollmentID;
    private String studentID;
    private String classID;

    private String subjectName;
    private Integer credit;

    private Double midtermScore;
    private Double finalScore;
    private Double otherScore;
    private Double totalScore;
    private String status;

    public EnrollmentDTO() {}

    public String getEnrollmentID() { return enrollmentID; }
    public void setEnrollmentID(String enrollmentID) { this.enrollmentID = enrollmentID; }

    public String getStudentID() { return studentID; }
    public void setStudentID(String studentID) { this.studentID = studentID; }

    public String getClassID() { return classID; }
    public void setClassID(String classID) { this.classID = classID; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public Integer getCredit() { return credit; }
    public void setCredit(Integer credit) { this.credit = credit; }

    public Double getMidtermScore() { return midtermScore; }
    public void setMidtermScore(Double midtermScore) { this.midtermScore = midtermScore; }

    public Double getFinalScore() { return finalScore; }
    public void setFinalScore(Double finalScore) { this.finalScore = finalScore; }

    public Double getOtherScore() { return otherScore; }
    public void setOtherScore(Double otherScore) { this.otherScore = otherScore; }

    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
