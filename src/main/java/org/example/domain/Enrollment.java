package org.example.domain;

import org.example.domain.enums.EnrollmentStatus;

import java.util.Objects;

public class Enrollment {
    private String enrollmentID;
    private Student student;
    private ClassSection classSection;

    private double midtermScore;
    private double finalScore;
    private double otherScore;
    private double totalScore;

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

    public double getMidtermScore() { return midtermScore; }
    public void setMidtermScore(double midtermScore) { this.midtermScore = midtermScore; }

    public double getFinalScore() { return finalScore; }
    public void setFinalScore(double finalScore) { this.finalScore = finalScore; }

    public double getOtherScore() { return otherScore; }
    public void setOtherScore(double otherScore) { this.otherScore = otherScore; }

    public double getTotalScore() { return totalScore; }
    public void setTotalScore(double totalScore) { this.totalScore = totalScore; }

    public EnrollmentStatus getStatus() { return status; }
    public void setStatus(EnrollmentStatus status) { this.status = status; }

    public void calculateTotal() {
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
