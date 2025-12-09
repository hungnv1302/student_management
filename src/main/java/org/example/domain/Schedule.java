package org.example.domain;

import java.util.ArrayList;
import java.util.List;

public class Schedule {

    private String scheduleID;
    private List<TimeSlot> timeSlots = new ArrayList<>();

    public Schedule() {
    }

    public Schedule(String scheduleID) {
        this.scheduleID = scheduleID;
    }

    public List<TimeSlot> getTodaySchedule() {
        // tạm: trả về toàn bộ
        return new ArrayList<>(timeSlots);
    }

    public List<TimeSlot> getWeeklySchedule() {
        return new ArrayList<>(timeSlots);
    }

    public void addTimeSlot(TimeSlot slot) {
        if (slot != null) timeSlots.add(slot);
    }
}
