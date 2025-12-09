package org.example.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Lecturer extends Person {

    private String lecturerID;
    private String department;
    private String degree;
    private String position;
    private LocalDate startDate;

    public Lecturer() {
    }

    public Lecturer(String lecturerID, String department, String degree,
                    String position, LocalDate startDate) {
        this.lecturerID = lecturerID;
        this.department = department;
        this.degree = degree;
        this.position = position;
        this.startDate = startDate;
    }

    public Schedule getTeachingSchedule() {
        return new Schedule();
    }

    public List<ClassSection> getAssignedClasses() {
        return new ArrayList<>();
    }

    public List<Student> getStudentsInClass(String classID) {
        return new ArrayList<>();
    }

    public void inputScore(String enrollmentID, String type, double value) {
        System.out.printf("Input %s score %.2f for enrollment %s%n", type, value, enrollmentID);
    }

    public void updateScore(String enrollmentID, String type, double value) {
        System.out.printf("Update %s score %.2f for enrollment %s%n", type, value, enrollmentID);
    }

    public void submitScores(String classID) {
        System.out.println("Submit scores for class " + classID);
    }

    public void exportScoreList(String classID) {
        System.out.println("Export score list for class " + classID);
    }

    // Getters & setters
    // (bạn thêm nếu cần)
}
