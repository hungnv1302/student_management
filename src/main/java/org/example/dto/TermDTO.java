package org.example.dto;

public class TermDTO {
    private int termYear;
    private short termSem;

    public TermDTO() {}
    public TermDTO(int termYear, short termSem) {
        this.termYear = termYear;
        this.termSem = termSem;
    }

    public int getTermYear() { return termYear; }
    public void setTermYear(int termYear) { this.termYear = termYear; }
    public short getTermSem() { return termSem; }
    public void setTermSem(short termSem) { this.termSem = termSem; }

    @Override public String toString() {
        return termYear + "." + termSem;
    }
}
