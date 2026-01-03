package org.example.dto;

public class LecturerScheduleSlotDto {
    private final String classId;
    private final String subjectName;
    private final int dayOfWeek; // 2..8 (8 = CN)
    private final int shiftNo;   // 1..4
    private final String room;

    public LecturerScheduleSlotDto(String classId, String subjectName, int dayOfWeek, int shiftNo, String room) {
        this.classId = classId;
        this.subjectName = subjectName;
        this.dayOfWeek = dayOfWeek;
        this.shiftNo = shiftNo;
        this.room = room;
    }

    public String getClassId() { return classId; }
    public String getSubjectName() { return subjectName; }
    public int getDayOfWeek() { return dayOfWeek; }
    public int getShiftNo() { return shiftNo; }
    public String getRoom() { return room; }
}
