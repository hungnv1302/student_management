package org.example.dto;

public class OpenClassRow {
    private String classId;
    private String subjectId;
    private String subjectName;
    private int credit;

    private int capacity;
    private int enrolledCount;
    private int remainingSeats;

    private String lecturerName;
    private String scheduleText;

    // ELIGIBLE / FULL / ENROLLED / BLOCKED / CONFLICT / CLOSED
    private String eligibility;
    private String reason;

    // ===== getters/setters =====

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public int getCredit() { return credit; }
    public void setCredit(int credit) { this.credit = credit; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getEnrolledCount() { return enrolledCount; }
    public void setEnrolledCount(int enrolledCount) { this.enrolledCount = enrolledCount; }

    public int getRemainingSeats() { return remainingSeats; }
    public void setRemainingSeats(int remainingSeats) { this.remainingSeats = remainingSeats; }

    public String getLecturerName() { return lecturerName; }
    public void setLecturerName(String lecturerName) { this.lecturerName = lecturerName; }

    public String getScheduleText() { return scheduleText; }
    public void setScheduleText(String scheduleText) { this.scheduleText = scheduleText; }

    public String getEligibility() { return eligibility; }
    public void setEligibility(String eligibility) { this.eligibility = eligibility; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
