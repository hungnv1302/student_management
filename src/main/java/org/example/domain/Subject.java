package org.example.domain;

import java.util.ArrayList;
import java.util.List;

public class Subject {

    private String subjectID;
    private String subjectName;
    private int credit;
    private String description;
    private String department;
    private boolean isRequired;
    private List<Subject> prerequisites = new ArrayList<>();

    public Subject() {
    }

    public Subject(String subjectID, String subjectName, int credit) {
        this.subjectID = subjectID;
        this.subjectName = subjectName;
        this.credit = credit;
    }

    public void getInfo() {
        System.out.println(this);
    }

    // Getters & setters thêm nếu cần
}
