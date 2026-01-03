package org.example.dto;

import java.math.BigDecimal;

public class StudentScoreRow {
    private int enrollId;
    private String studentId;
    private String fullName;

    private BigDecimal midtermScore; // midterm_score
    private BigDecimal finalScore;   // final_score
    private BigDecimal totalScore;   // total_score

    private String status;           // status
    private boolean finalized;       // is_finalized

    // ===== getters/setters =====
    public int getEnrollId() { return enrollId; }
    public void setEnrollId(int enrollId) { this.enrollId = enrollId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public BigDecimal getMidtermScore() { return midtermScore; }
    public void setMidtermScore(BigDecimal midtermScore) { this.midtermScore = midtermScore; }

    public BigDecimal getFinalScore() { return finalScore; }
    public void setFinalScore(BigDecimal finalScore) { this.finalScore = finalScore; }

    public BigDecimal getTotalScore() { return totalScore; }
    public void setTotalScore(BigDecimal totalScore) { this.totalScore = totalScore; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isFinalized() { return finalized; }
    public void setFinalized(boolean finalized) { this.finalized = finalized; }

    // ===== rules: chỉ sửa nếu NULL và chưa chốt =====
    public boolean canEditMidterm() { return !finalized && midtermScore == null; }
    public boolean canEditFinal()   { return !finalized && finalScore == null; }
    public boolean canEditTotal()   { return !finalized && totalScore == null; }
}
