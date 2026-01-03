package org.example.repository;

import org.example.config.DbConfig;
import org.example.dto.LecturerScheduleSlotDto;

import java.sql.*;
import java.util.*;

public class LecturerTimetableRepository {

    public record Term(int year, int sem) {}

    public Optional<Term> getCurrentTerm() throws SQLException {
        String sql = """
            SELECT term_year, term_sem
            FROM qlsv.registration_config
            WHERE id = 1
        """;
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) return Optional.empty();
            Integer y = (Integer) rs.getObject("term_year");
            Integer s = (Integer) rs.getObject("term_sem");
            if (y == null || s == null) return Optional.empty();
            return Optional.of(new Term(y, s));
        }
    }

    /** shift_no -> "06:45-09:10" */
    public LinkedHashMap<Integer, String> loadShiftRanges() throws SQLException {
        String sql = """
            SELECT shift_no,
                   TO_CHAR(start_time,'HH24:MI') || '-' || TO_CHAR(end_time,'HH24:MI') AS range
            FROM qlsv.shift_times
            ORDER BY shift_no
        """;
        LinkedHashMap<Integer, String> map = new LinkedHashMap<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getInt("shift_no"), rs.getString("range"));
            }
        }
        return map;
    }

    /** Slots theo giảng viên + kỳ (teaching_assignments + sections + subjects + time_slots) */
    public List<LecturerScheduleSlotDto> findSlots(String lecturerId, int year, int sem) throws SQLException {
        String sql = """
            SELECT
                ts.class_id,
                sub.subject_name,
                ts.day_of_week,
                ts.shift_no,
                COALESCE(ts.room, sec.room) AS room
            FROM qlsv.teaching_assignments ta
            JOIN qlsv.sections sec ON sec.class_id = ta.class_id
            JOIN qlsv.subjects sub ON sub.subject_id = sec.subject_id
            JOIN qlsv.time_slots ts ON ts.class_id = sec.class_id
            WHERE ta.lecturer_id = ?
              AND (ta.role IS NULL OR ta.role = 'MAIN')
              AND sec.term_year = ?
              AND sec.term_sem  = ?
            ORDER BY ts.day_of_week, ts.shift_no, ts.class_id
        """;

        List<LecturerScheduleSlotDto> out = new ArrayList<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, lecturerId);
            ps.setInt(2, year);
            ps.setInt(3, sem);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new LecturerScheduleSlotDto(
                            rs.getString("class_id"),
                            rs.getString("subject_name"),
                            rs.getInt("day_of_week"),
                            rs.getInt("shift_no"),
                            rs.getString("room")
                    ));
                }
            }
        }
        return out;
    }
}
