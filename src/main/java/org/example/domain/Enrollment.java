package org.example.domain;

import org.example.domain.enums.EnrollmentStatus;

import java.util.Objects;

public class Enrollment {
    private String enrollmentID;
    private Student student;
    private ClassSection classSection;

    // ✅ Kỳ của sinh viên (enrollments.semester_no)
    private Short semesterNo;

    // ✅ dùng Double để cho phép null (kỳ đang học chưa có điểm)
    private Double midtermScore;
    private Double finalScore;
    private Double otherScore;
    private Double totalScore;

    private EnrollmentStatus status;

    public Enrollment() {}

    public Enrollment(String enrollmentID, Student student, ClassSection classSection, EnrollmentStatus status) {
        this.enrollmentID = enrollmentID;
        this.student = student;
        this.classSection = classSection;
        this.status = status;
    }

    public String getEnrollmentID() { return enrollmentID; }
    public void setEnrollmentID(String enrollmentID) { this.enrollmentID = enrollmentID; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public ClassSection getClassSection() { return classSection; }
    public void setClassSection(ClassSection classSection) { this.classSection = classSection; }

    public Short getSemesterNo() { return semesterNo; }
    public void setSemesterNo(Short semesterNo) { this.semesterNo = semesterNo; }

    public Double getMidtermScore() { return midtermScore; }
    public void setMidtermScore(Double midtermScore) { this.midtermScore = midtermScore; }

    public Double getFinalScore() { return finalScore; }
    public void setFinalScore(Double finalScore) { this.finalScore = finalScore; }

    public Double getOtherScore() { return otherScore; }
    public void setOtherScore(Double otherScore) { this.otherScore = otherScore; }

    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }

    public EnrollmentStatus getStatus() { return status; }
    public void setStatus(EnrollmentStatus status) { this.status = status; }

    // ✅ Tính tổng chỉ khi có đủ điểm
    public void calculateTotal() {
        if (midtermScore == null || finalScore == null) return;

        this.totalScore = midtermScore * 0.5 + finalScore * 0.5;

        if (this.totalScore >= 4.0) {
            this.status = EnrollmentStatus.COMPLETED;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Enrollment that)) return false;
        return Objects.equals(enrollmentID, that.enrollmentID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enrollmentID);
    }
}
