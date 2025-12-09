package org.example.domain;

import java.util.ArrayList;
import java.util.List;

public class Student extends Person {

    private String studentID;
    private String department;
    private String major;
    private String className;
    private int admissionYear;
    private double gpa;
    private int trainingScore;
    private String status;

    public Student() {
    }

    public Student(String studentID, String department, String major,
                   String className, int admissionYear) {
        this.studentID = studentID;
        this.department = department;
        this.major = major;
        this.className = className;
        this.admissionYear = admissionYear;
        this.status = "STUDYING";
    }

    public boolean registerSubject(String classID) {
        System.out.println("Student " + studentID + " registers class " + classID);
        return true;
    }

    public boolean dropSubject(String classID) {
        System.out.println("Student " + studentID + " drops class " + classID);
        return true;
    }

    public List<Enrollment> getEnrollments() {
        return new ArrayList<>();
    }

    public Schedule viewSchedule() {
        return new Schedule();
    }

    public List<Enrollment> viewScores() {
        return new ArrayList<>();
    }

    public ClassSection viewClassInfo(String classID) {
        return null;
    }

    // Getters & setters thêm sau nếu cần
}
