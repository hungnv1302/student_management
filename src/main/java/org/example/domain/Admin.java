package org.example.domain;

public class Admin extends Person {

    private String staffID;
    private String department;
    private String position;

    public Admin() {
    }

    public Admin(String staffID, String department, String position) {
        this.staffID = staffID;
        this.department = department;
        this.position = position;
    }

    public Subject createSubject(Subject info) {
        // trong domain chỉ trả lại đối tượng, service sẽ lo lưu DB
        return info;
    }

    public boolean updateSubject(String subjectID, Subject info) {
        return true;
    }

    public ClassSection createClassSection(ClassSection info) {
        return info;
    }

    public boolean assignLecturer(String classID, String lecturerID) {
        return true;
    }

    public boolean setClassSchedule(String classID, Schedule schedule) {
        return true;
    }

    public ExamSchedule createExamSchedule(String classID, ExamSchedule examInfo) {
        return examInfo;
    }

    public void openRegistration(String semester) {
        System.out.println("Open registration for semester " + semester);
    }

    public void closeRegistration(String semester) {
        System.out.println("Close registration for semester " + semester);
    }

    // Getters & setters
    public String getStaffID() { return staffID; }
    public void setStaffID(String staffID) { this.staffID = staffID; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
}
