package org.example.domain;

import java.time.LocalDate;

public class Person {

    private String personID;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String phoneNumber;
    private String email;
    private String address;

    public Person() {
    }

    public Person(String personID, String fullName, LocalDate dateOfBirth,
                  String gender, String phoneNumber, String email, String address) {
        this.personID = personID;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
    }

    public void viewProfile() {
        System.out.println(this);
    }

    public boolean updateProfile(Person info) {
        if (info == null) return false;
        this.fullName = info.fullName;
        this.dateOfBirth = info.dateOfBirth;
        this.gender = info.gender;
        this.phoneNumber = info.phoneNumber;
        this.email = info.email;
        this.address = info.address;
        return true;
    }

    // Getters & setters
    public String getPersonID() { return personID; }
    public void setPersonID(String personID) { this.personID = personID; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
