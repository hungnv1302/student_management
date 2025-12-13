package org.example.domain;

import org.example.domain.enums.Gender;
import java.time.LocalDate;

public class Admin extends Person {
    private String staffID;
    private String department;
    private String position;

    public Admin() {}

    public Admin(String personID, String fullName, LocalDate dateOfBirth, Gender gender,
                 String phoneNumber, String email, String address,
                 String staffID, String department, String position) {
        super(personID, fullName, dateOfBirth, gender, phoneNumber, email, address);
        this.staffID = staffID;
        this.department = department;
        this.position = position;
    }

    public String getStaffID() { return staffID; }
    public void setStaffID(String staffID) { this.staffID = staffID; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
}
