package org.example.service;

import org.example.dto.ScheduleRow;
import org.example.repository.DbFn;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

public class StudentScheduleService {

    public record TermKey(int year, short sem) {
        @Override public String toString() { return year + "." + sem; }
    }

    /** open term lấy từ registration_config */
    public TermKey getOpenTerm() {
        try {
            return DbFn.queryOne(
                    "SELECT term_year, term_sem FROM qlsv.registration_config WHERE id=1",
                    null,
                    rs -> new TermKey(rs.getInt("term_year"), rs.getShort("term_sem"))
            );
        } catch (SQLException e) {
            throw new RuntimeException("DB error getOpenTerm: " + e.getMessage(), e);
        }
    }

    /** current term = kỳ trước của open term */
    public TermKey getCurrentTerm() {
        TermKey open = getOpenTerm();
        int y = open.year();
        short s = open.sem();
        if (s == 1) return new TermKey(y - 1, (short) 2);
        return new TermKey(y, (short) 1);
    }

    /** Query TKB theo kỳ bất kỳ (SQL thường, KHÔNG function) */
    public List<ScheduleRow> getSchedule(String studentId, int year, short sem) {
        Objects.requireNonNull(studentId, "studentId is null");

        // ✅ BỔ SUNG lecturer_name
        String sql = """
            SELECT
                s.class_id,
                sub.subject_name,
                ts.day_of_week,
                ts.start_time,
                ts.end_time,
                COALESCE(ts.room, s.room) AS room,
                lp.full_name AS lecturer_name
            FROM qlsv.enrollments e
            JOIN qlsv.sections s       ON s.class_id = e.class_id
            JOIN qlsv.subjects sub     ON sub.subject_id = s.subject_id
            JOIN qlsv.time_slots ts    ON ts.class_id = s.class_id
            LEFT JOIN qlsv.persons lp  ON lp.person_id = s.lecturer_id
            WHERE e.student_id = ?
              AND s.term_year = ?
              AND s.term_sem  = ?
            ORDER BY ts.day_of_week, ts.start_time
        """;

        try {
            return DbFn.queryList(sql,
                    ps -> {
                        ps.setString(1, studentId);
                        ps.setInt(2, year);
                        ps.setShort(3, sem);
                    },
                    rs -> {
                        ScheduleRow r = new ScheduleRow();
                        r.setClassId(rs.getString("class_id"));
                        r.setSubjectName(rs.getString("subject_name"));
                        r.setDayOfWeek(rs.getInt("day_of_week"));
                        r.setStartTime(rs.getObject("start_time", LocalTime.class));
                        r.setEndTime(rs.getObject("end_time", LocalTime.class));
                        r.setRoom(rs.getString("room"));
                        r.setLecturerName(rs.getString("lecturer_name")); // ✅ thêm dòng này
                        return r;
                    });
        } catch (SQLException e) {
            throw new RuntimeException("DB error getSchedule: " + e.getMessage(), e);
        }
    }

    public List<ScheduleRow> getScheduleCurrentTerm(String studentId) {
        TermKey t = getCurrentTerm();
        return getSchedule(studentId, t.year(), t.sem());
    }

    public List<ScheduleRow> getScheduleOpenTerm(String studentId) {
        TermKey t = getOpenTerm();
        return getSchedule(studentId, t.year(), t.sem());
    }
}
