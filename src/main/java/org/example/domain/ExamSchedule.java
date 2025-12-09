package org.example.domain;

import java.time.LocalDateTime;

public class ExamSchedule {

    private String examID;
    private ClassSection classSection;
    private LocalDateTime examDate;
    private String startTime;
    private String endTime;
    private String room;
    private String examType;

    public ExamSchedule() {
    }

    public ExamSchedule(String examID, ClassSection classSection,
                        LocalDateTime examDate, String startTime,
                        String endTime, String room, String examType) {
        this.examID = examID;
        this.classSection = classSection;
        this.examDate = examDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.examType = examType;
    }

    public void getInfo() {
        System.out.println(this);
    }
}
