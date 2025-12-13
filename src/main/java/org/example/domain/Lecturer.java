package org.example.domain;

import org.example.domain.enums.Gender;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Lecturer extends Person {
    private String lecturerID;
    private String department;
    private String degree;
    private String position;
    private LocalDate startDate;

    private final List<ClassSection> assignedClasses = new ArrayList<>();

    public Lecturer() {}

    public Lecturer(String personID, String fullName, LocalDate dateOfBirth, Gender gender,
                    String phoneNumber, String email, String address,
                    String lecturerID, String department, String degree, String position, LocalDate startDate) {
        super(personID, fullName, dateOfBirth, gender, phoneNumber, email, address);
        this.lecturerID = lecturerID;
        this.department = department;
        this.degree = degree;
        this.position = position;
        this.startDate = startDate;
    }

    public String getLecturerID() { return lecturerID; }
    public void setLecturerID(String lecturerID) { this.lecturerID = lecturerID; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public List<ClassSection> getAssignedClasses() {
        return Collections.unmodifiableList(assignedClasses);
    }

    public void addAssignedClass(ClassSection section) {
        if (section != null && !assignedClasses.contains(section)) assignedClasses.add(section);
    }

    public void removeAssignedClass(ClassSection section) {
        assignedClasses.remove(section);
    }
}
