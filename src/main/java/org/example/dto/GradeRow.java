package org.example.dto;

import java.math.BigDecimal;

public class GradeRow {

    private final int termYear;
    private final short termSem;

    private final String classId;
    private final String subjectId;
    private final String subjectName;
    private final Integer credit;

    private final BigDecimal midtermScore;
    private final BigDecimal finalScore;
    private final BigDecimal totalScore;
    private Integer enrollId;

    private final String letter;
    private final BigDecimal point4;
    private final Boolean finalized;

    public GradeRow(
            int termYear,
            short termSem,
            String classId,
            String subjectId,
            String subjectName,
            Integer credit,
            BigDecimal midtermScore,
            BigDecimal finalScore,
            BigDecimal totalScore,
            String letter,
            BigDecimal point4,
            Boolean finalized
    ) {
        this.termYear = termYear;
        this.termSem = termSem;
        this.classId = classId;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.credit = credit;
        this.midtermScore = midtermScore;
        this.finalScore = finalScore;
        this.totalScore = totalScore;
        this.letter = letter;
        this.point4 = point4;
        this.finalized = finalized;
    }

    // ===== getters =====
    public int getTermYear() { return termYear; }
    public short getTermSem() { return termSem; }

    public String getClassId() { return classId; }
    public String getSubjectId() { return subjectId; }
    public String getSubjectName() { return subjectName; }
    public Integer getCredit() { return credit; }

    public BigDecimal getMidtermScore() { return midtermScore; }
    public BigDecimal getFinalScore() { return finalScore; }
    public BigDecimal getTotalScore() { return totalScore; }

    public String getLetter() { return letter; }
    public BigDecimal getPoint4() { return point4; }
    public Boolean getFinalized() { return finalized; }

    // ===== enrollId (getter/setter) =====
    public Integer getEnrollId() {
        return enrollId;
    }

    public void setEnrollId(Integer enrollId) {
        this.enrollId = enrollId;
    }

    // (optional) tiện debug
    @Override
    public String toString() {
        return "GradeRow{" +
                "term=" + termYear + "." + termSem +
                ", classId='" + classId + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", enrollId=" + enrollId +
                ", midterm=" + midtermScore +
                ", final=" + finalScore +
                ", total=" + totalScore +
                ", finalized=" + finalized +
                '}';
    }
}
