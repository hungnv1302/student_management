package org.example.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Subject {
    private String subjectID;
    private String subjectName;
    private int credit;
    private String description;
    private String department;
    private boolean required;

    private final List<Subject> prerequisites = new ArrayList<>();

    public Subject() {}

    public Subject(String subjectID, String subjectName, int credit, String description, String department, boolean required) {
        this.subjectID = subjectID;
        this.subjectName = subjectName;
        this.credit = credit;
        this.description = description;
        this.department = department;
        this.required = required;
    }

    public String getSubjectID() { return subjectID; }
    public void setSubjectID(String subjectID) { this.subjectID = subjectID; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public int getCredit() { return credit; }
    public void setCredit(int credit) { this.credit = credit; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public List<Subject> getPrerequisites() {
        return Collections.unmodifiableList(prerequisites);
    }

    public void addPrerequisite(Subject subject) {
        if (subject != null && !prerequisites.contains(subject)) prerequisites.add(subject);
    }

    public void removePrerequisite(Subject subject) {
        prerequisites.remove(subject);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subject subject)) return false;
        return Objects.equals(subjectID, subject.subjectID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subjectID);
    }

}
