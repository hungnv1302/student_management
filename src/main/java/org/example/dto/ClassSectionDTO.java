package org.example.dto;

public class ClassSectionDTO {
    private String classID;
    private String subjectID;
    private String subjectName;
    private String semester;
    private Integer year;
    private Integer capacity;
    private String room;

    private String lecturerID;
    private String lecturerName;

    public ClassSectionDTO() {}

    public String getClassID() { return classID; }
    public void setClassID(String classID) { this.classID = classID; }

    public String getSubjectID() { return subjectID; }
    public void setSubjectID(String subjectID) { this.subjectID = subjectID; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getLecturerID() { return lecturerID; }
    public void setLecturerID(String lecturerID) { this.lecturerID = lecturerID; }

    public String getLecturerName() { return lecturerName; }
    public void setLecturerName(String lecturerName) { this.lecturerName = lecturerName; }
}
