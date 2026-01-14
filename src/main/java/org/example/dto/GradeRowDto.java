package org.example.dto;

import javafx.beans.property.*;

import java.math.BigDecimal;

public class GradeRowDto {
    private final IntegerProperty enrollId = new SimpleIntegerProperty();
    private final StringProperty studentId = new SimpleStringProperty();
    private final StringProperty fullName = new SimpleStringProperty();

    private final ObjectProperty<BigDecimal> midterm = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> fin = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> total = new SimpleObjectProperty<>();

    private final BooleanProperty finalized = new SimpleBooleanProperty(false);

    // ====== properties ======
    public int getEnrollId() { return enrollId.get(); }
    public IntegerProperty enrollIdProperty() { return enrollId; }
    public void setEnrollId(int v) { enrollId.set(v); }

    public StringProperty studentIdProperty() { return studentId; }
    public void setStudentId(String v) { studentId.set(v); }

    public StringProperty fullNameProperty() { return fullName; }
    public void setFullName(String v) { fullName.set(v); }

    public ObjectProperty<BigDecimal> midtermProperty() { return midterm; }
    public BigDecimal getMidterm() { return midterm.get(); }
    public void setMidterm(BigDecimal v) { midterm.set(v); }

    public ObjectProperty<BigDecimal> finProperty() { return fin; }
    public BigDecimal getFin() { return fin.get(); }
    public void setFin(BigDecimal v) { fin.set(v); }

    public ObjectProperty<BigDecimal> totalProperty() { return total; }
    public BigDecimal getTotal() { return total.get(); }
    public void setTotal(BigDecimal v) { total.set(v); }

    public boolean isFinalized() { return finalized.get(); }
    public BooleanProperty finalizedProperty() { return finalized; }
    public void setFinalized(boolean v) { finalized.set(v); }

    // ===== rules: chỉ chấm được ô NULL + chưa chốt =====
    // OLD: chỉ edit khi NULL + chưa chốt
// public boolean canEditMidterm() { return !isFinalized() && getMidterm() == null; }
// public boolean canEditFinal()   { return !isFinalized() && getFin() == null; }

    // NEW: miễn chưa chốt là edit được
    public boolean canEditMidterm() { return !isFinalized(); }
    public boolean canEditFinal()   { return !isFinalized(); }

}
