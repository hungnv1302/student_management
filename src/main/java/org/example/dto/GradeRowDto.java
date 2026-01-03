package org.example.dto;

import javafx.beans.property.*;

import java.math.BigDecimal;

public class GradeRowDto {

    private final IntegerProperty enrollId = new SimpleIntegerProperty();
    private final StringProperty studentId = new SimpleStringProperty();
    private final StringProperty fullName = new SimpleStringProperty();

    private final ObjectProperty<BigDecimal> midterm = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> finalScore = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> total = new SimpleObjectProperty<>();

    private final StringProperty note = new SimpleStringProperty();
    private final BooleanProperty finalized = new SimpleBooleanProperty(false);

    // chỉ cho edit nếu ban đầu null
    private final boolean midtermWasNull;
    private final boolean finalWasNull;
    private final boolean totalWasNull;

    public GradeRowDto(int enrollId, String studentId, String fullName,
                       BigDecimal midterm, BigDecimal finalScore, BigDecimal total,
                       String note, boolean finalized) {
        this.enrollId.set(enrollId);
        this.studentId.set(studentId);
        this.fullName.set(fullName);

        this.midterm.set(midterm);
        this.finalScore.set(finalScore);
        this.total.set(total);

        this.note.set(note == null ? "" : note);
        this.finalized.set(finalized);

        this.midtermWasNull = (midterm == null);
        this.finalWasNull = (finalScore == null);
        this.totalWasNull = (total == null);
    }

    public int getEnrollId() { return enrollId.get(); }

    public StringProperty studentIdProperty() { return studentId; }
    public StringProperty fullNameProperty() { return fullName; }

    public ObjectProperty<BigDecimal> midtermProperty() { return midterm; }
    public ObjectProperty<BigDecimal> finalScoreProperty() { return finalScore; }
    public ObjectProperty<BigDecimal> totalProperty() { return total; }

    public StringProperty noteProperty() { return note; }

    public boolean isFinalized() { return finalized.get(); }
    public BooleanProperty finalizedProperty() { return finalized; }

    public boolean canEditMidterm() { return !isFinalized() && midtermWasNull; }
    public boolean canEditFinal()   { return !isFinalized() && finalWasNull; }
    public boolean canEditTotal()   { return !isFinalized() && totalWasNull; }

    public boolean hasAnyNewInput() {
        return (midtermWasNull && midterm.get() != null)
                || (finalWasNull && finalScore.get() != null)
                || (totalWasNull && total.get() != null);
    }
}
