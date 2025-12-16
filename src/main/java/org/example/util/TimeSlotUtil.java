package org.example.util;

import org.example.domain.TimeSlot;

public class TimeSlotUtil {

    public static boolean isOverlap(TimeSlot a, TimeSlot b) {
        if (a.getDayOfWeek() != b.getDayOfWeek()) return false;

        // overlap if start < otherEnd AND otherStart < end
        return a.getStartTime().isBefore(b.getEndTime())
                && b.getStartTime().isBefore(a.getEndTime());
    }
}
