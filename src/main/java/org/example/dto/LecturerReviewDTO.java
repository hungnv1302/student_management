package org.example.dto;

import javafx.beans.property.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LecturerReviewDTO {
    private final IntegerProperty requestId = new SimpleIntegerProperty();
    private final IntegerProperty enrollId = new SimpleIntegerProperty();
    private final StringProperty studentId = new SimpleStringProperty();
    private final StringProperty studentName = new SimpleStringProperty();
    private final StringProperty classId = new SimpleStringProperty();
    private final StringProperty subjectName = new SimpleStringProperty();
    private final StringProperty reason = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> oldTotal = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> newTotal = new SimpleObjectProperty<>();
    private final StringProperty note = new SimpleStringProperty();

    // ====== Getters & Setters ======
    public int getRequestId() { return requestId.get(); }
    public IntegerProperty requestIdProperty() { return requestId; }
    public void setRequestId(int v) { requestId.set(v); }

    public int getEnrollId() { return enrollId.get(); }
    public IntegerProperty enrollIdProperty() { return enrollId; }
    public void setEnrollId(int v) { enrollId.set(v); }

    public String getStudentId() { return studentId.get(); }
    public StringProperty studentIdProperty() { return studentId; }
    public void setStudentId(String v) { studentId.set(v); }

    public String getStudentName() { return studentName.get(); }
    public StringProperty studentNameProperty() { return studentName; }
    public void setStudentName(String v) { studentName.set(v); }

    public String getClassId() { return classId.get(); }
    public StringProperty classIdProperty() { return classId; }
    public void setClassId(String v) { classId.set(v); }

    public String getSubjectName() { return subjectName.get(); }
    public StringProperty subjectNameProperty() { return subjectName; }
    public void setSubjectName(String v) { subjectName.set(v); }

    public String getReason() { return reason.get(); }
    public StringProperty reasonProperty() { return reason; }
    public void setReason(String v) { reason.set(v); }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }
    public void setStatus(String v) { status.set(v); }

    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { createdAt.set(v); }

    public BigDecimal getOldTotal() { return oldTotal.get(); }
    public ObjectProperty<BigDecimal> oldTotalProperty() { return oldTotal; }
    public void setOldTotal(BigDecimal v) { oldTotal.set(v); }

    public BigDecimal getNewTotal() { return newTotal.get(); }
    public ObjectProperty<BigDecimal> newTotalProperty() { return newTotal; }
    public void setNewTotal(BigDecimal v) { newTotal.set(v); }

    public String getNote() { return note.get(); }
    public StringProperty noteProperty() { return note; }
    public void setNote(String v) { note.set(v); }

    // ====== Helper methods ======
    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status.get());
    }

    public String getStatusText() {
        return switch (status.get().toUpperCase()) {
            case "PENDING" -> "Chờ xử lý";
            case "APPROVED" -> "Chấp nhận";
            case "REJECTED" -> "Từ chối";
            case "RESOLVED" -> "Đã xử lý";
            default -> status.get();
        };
    }
}