package org.example.domain;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Schedule {
    private String scheduleID;
    private final List<TimeSlot> timeSlots = new ArrayList<>();

    public Schedule() {}

    public Schedule(String scheduleID) {
        this.scheduleID = scheduleID;
    }

    public String getScheduleID() { return scheduleID; }
    public void setScheduleID(String scheduleID) { this.scheduleID = scheduleID; }

    public List<TimeSlot> getTimeSlots() {
        return Collections.unmodifiableList(timeSlots);
    }

    public void addTimeSlot(TimeSlot slot) {
        if (slot != null) timeSlots.add(slot);
    }

    public void removeTimeSlot(TimeSlot slot) {
        timeSlots.remove(slot);
    }

    // Hàm lọc dữ liệu nhẹ (OK trong domain)
    public List<TimeSlot> getScheduleByDay(DayOfWeek day) {
        if (day == null) return List.of();
        return timeSlots.stream()
                .filter(t -> day.equals(t.getDayOfWeek()))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Schedule schedule)) return false;
        return Objects.equals(scheduleID, schedule.scheduleID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduleID);
    }
}
