package org.example.domain;

public class Enrollment {

    private String enrollmentID;
    private Student student;
    private ClassSection classSection;
    private double midtermScore;
    private double finalScore;
    private double otherScore;
    private double totalScore;
    private String status;

    public Enrollment() {
    }

    public Enrollment(String enrollmentID, Student student, ClassSection classSection) {
        this.enrollmentID = enrollmentID;
        this.student = student;
        this.classSection = classSection;
        this.status = "REGISTERED";
    }

    public double calculateTotal() {
        totalScore = midtermScore * 0.2 + finalScore * 0.6 + otherScore * 0.2;
        return totalScore;
    }

    // Getters & setters thêm sau nếu cần
}
