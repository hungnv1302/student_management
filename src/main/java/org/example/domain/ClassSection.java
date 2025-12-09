package org.example.domain;

import java.util.ArrayList;
import java.util.List;

public class ClassSection {

    private String classID;
    private String semester;
    private int year;
    private int capacity;
    private String room;
    private Schedule schedule;
    private Subject subject;
    private Lecturer lecturer;

    // (quan hệ tới Enrollment được quản lý ở nơi khác)

    public ClassSection() {
    }

    public ClassSection(String classID, String semester, int year, int capacity) {
        this.classID = classID;
        this.semester = semester;
        this.year = year;
        this.capacity = capacity;
    }

    public List<Student> getEnrolledStudents() {
        return new ArrayList<>();
    }

    public boolean isFull() {
        // chưa có số SV hiện tại nên tạm luôn false
        return false;
    }

    // Getters & setters thêm sau nếu cần
}
