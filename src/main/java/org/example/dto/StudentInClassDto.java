package org.example.dto;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class StudentInClassDto {
    private final StringProperty studentId = new SimpleStringProperty();
    private final StringProperty fullName = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();

    public StudentInClassDto() {}

    public StudentInClassDto(String studentId, String fullName, String email) {
        this.studentId.set(studentId);
        this.fullName.set(fullName);
        this.email.set(email);
    }

    public String getStudentId() { return studentId.get(); }
    public void setStudentId(String v) { studentId.set(v); }
    public StringProperty studentIdProperty() { return studentId; }

    public String getFullName() { return fullName.get(); }
    public void setFullName(String v) { fullName.set(v); }
    public StringProperty fullNameProperty() { return fullName; }

    public String getEmail() { return email.get(); }
    public void setEmail(String v) { email.set(v); }
    public StringProperty emailProperty() { return email; }
}
