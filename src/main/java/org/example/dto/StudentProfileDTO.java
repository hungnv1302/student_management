package org.example.dto;

import java.time.LocalDate;

public class StudentProfileDTO {
    private String studentId;

    // persons
    private String fullName;
    private LocalDate dob;
    private String gender;
    private String phoneNumber;
    private String email;
    private String address;

    // students
    private String department;
    private String major;
    private String className;
    private Integer admissionYear;
    private Integer trainingScore;
    private String status;

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public Integer getAdmissionYear() { return admissionYear; }
    public void setAdmissionYear(Integer admissionYear) { this.admissionYear = admissionYear; }

    public Integer getTrainingScore() { return trainingScore; }
    public void setTrainingScore(Integer trainingScore) { this.trainingScore = trainingScore; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
