package org.example.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ClassSection {
    private String classID;
    private String semester;
    private int year;
    private int capacity;
    private String room;

    private Schedule schedule;
    private Subject subject;
    private Lecturer lecturer;

    private final List<Enrollment> enrollments = new ArrayList<>();

    public ClassSection() {}

    public ClassSection(String classID, String semester, int year, int capacity, String room,
                        Schedule schedule, Subject subject, Lecturer lecturer) {
        this.classID = classID;
        this.semester = semester;
        this.year = year;
        this.capacity = capacity;
        this.room = room;
        this.schedule = schedule;
        this.subject = subject;
        this.lecturer = lecturer;
    }

    public String getClassID() { return classID; }
    public void setClassID(String classID) { this.classID = classID; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public Schedule getSchedule() { return schedule; }
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public Lecturer getLecturer() { return lecturer; }
    public void setLecturer(Lecturer lecturer) { this.lecturer = lecturer; }

    public List<Enrollment> getEnrollments() {
        return Collections.unmodifiableList(enrollments);
    }

    public void addEnrollment(Enrollment enrollment) {
        if (enrollment != null && !enrollments.contains(enrollment)) enrollments.add(enrollment);
    }

    public void removeEnrollment(Enrollment enrollment) {
        enrollments.remove(enrollment);
    }

    public boolean isFull() {
        return capacity > 0 && enrollments.size() >= capacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassSection that)) return false;
        return Objects.equals(classID, that.classID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classID);
    }
}
