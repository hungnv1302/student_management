package org.example.domain;

public class TimeSlot {

    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String room;

    public TimeSlot() {
    }

    public TimeSlot(String dayOfWeek, String startTime, String endTime, String room) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
    }

    public String getInfo() {
        return dayOfWeek + " " + startTime + "-" + endTime + " @ " + room;
    }

    // Getters & setters nếu cần
}
