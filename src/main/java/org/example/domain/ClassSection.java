package org.example.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ClassSection {
    private String classID;     // sections.class_id (varchar(6))
    private short termNo;       // sections.term_no (smallint)
    private int capacity;       // sections.capacity
    private String status;      // sections.status (OPEN/CLOSED/LOCKED/CANCELED)
    private String room;        // sections.room
    private String note;        // sections.note

    private Schedule schedule;
    private Subject subject;
    private Lecturer lecturer;

    private final List<Enrollment> enrollments = new ArrayList<>();

    public ClassSection() {}

    public ClassSection(String classID, short termNo, int capacity, String status, String room, String note,
                        Schedule schedule, Subject subject, Lecturer lecturer) {
        this.classID = classID;
        this.termNo = termNo;
        this.capacity = capacity;
        this.status = status;
        this.room = room;
        this.note = note;
        this.schedule = schedule;
        this.subject = subject;
        this.lecturer = lecturer;
    }

    public String getClassID() { return classID; }
    public void setClassID(String classID) { this.classID = classID; }

    public short getTermNo() { return termNo; }
    public void setTermNo(short termNo) { this.termNo = termNo; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

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

    /** Lưu ý: enrollments list thường không load đủ khi dùng DB functions */
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
