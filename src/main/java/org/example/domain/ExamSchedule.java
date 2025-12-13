package org.example.domain;

import org.example.domain.enums.ExamType;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public class ExamSchedule {
    private String examID;
    private ClassSection classSection;

    private LocalDateTime examDate; // ngày thi (có thể chỉ cần LocalDate)
    private LocalTime startTime;
    private LocalTime endTime;

    private String room;
    private ExamType examType;

    public ExamSchedule() {}

    public ExamSchedule(String examID, ClassSection classSection, LocalDateTime examDate,
                        LocalTime startTime, LocalTime endTime, String room, ExamType examType) {
        this.examID = examID;
        this.classSection = classSection;
        this.examDate = examDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.examType = examType;
    }

    public String getExamID() { return examID; }
    public void setExamID(String examID) { this.examID = examID; }

    public ClassSection getClassSection() { return classSection; }
    public void setClassSection(ClassSection classSection) { this.classSection = classSection; }

    public LocalDateTime getExamDate() { return examDate; }
    public void setExamDate(LocalDateTime examDate) { this.examDate = examDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public ExamType getExamType() { return examType; }
    public void setExamType(ExamType examType) { this.examType = examType; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExamSchedule that)) return false;
        return Objects.equals(examID, that.examID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(examID);
    }
}
