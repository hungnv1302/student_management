package org.example.domain;

import org.example.domain.enums.Gender;
import org.example.domain.enums.StudentStatus;

import java.time.LocalDate;

public class Student extends org.example.domain.Person {
    private String studentID;
    private String department;
    private String major;
    private String className;
    private int admissionYear;
    private double gpa;
    private int trainingScore;
    private StudentStatus status;

    public Student() {}

    public Student(String personID, String fullName, LocalDate dateOfBirth, Gender gender,
                   String phoneNumber, String email, String address,
                   String studentID, String department, String major, String className,
                   int admissionYear, double gpa, int trainingScore, StudentStatus status) {
        super(personID, fullName, dateOfBirth, gender, phoneNumber, email, address);
        this.studentID = studentID;
        this.department = department;
        this.major = major;
        this.className = className;
        this.admissionYear = admissionYear;
        this.gpa = gpa;
        this.trainingScore = trainingScore;
        this.status = status;
    }

    public String getStudentID() { return studentID; }
    public void setStudentID(String studentID) { this.studentID = studentID; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public int getAdmissionYear() { return admissionYear; }
    public void setAdmissionYear(int admissionYear) { this.admissionYear = admissionYear; }

    public double getGpa() { return gpa; }
    public void setGpa(double gpa) { this.gpa = gpa; }

    public int getTrainingScore() { return trainingScore; }
    public void setTrainingScore(int trainingScore) { this.trainingScore = trainingScore; }

    public StudentStatus getStatus() { return status; }
    public void setStatus(StudentStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Student{" +
                "studentID='" + getStudentID() + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", department='" + getDepartment() + '\'' +
                ", major='" + getMajor() + '\'' +
                ", className='" + getClassName() + '\'' +
                ", admissionYear=" + getAdmissionYear() +
                ", gpa=" + getGpa() +
                ", trainingScore=" + getTrainingScore() +
                ", status=" + getStatus() +
                '}';
    }

}
