package org.example.repository;

import org.example.config.DbConfig;
import org.example.domain.TimeSlot;

import java.sql.*;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

public class TimeSlotRepository {

    public List<TimeSlot> findByClassId(String classId) throws SQLException {
        String sql = """
            SELECT t.day_of_week, t.start_time, t.end_time, t.room
            FROM time_slots t
            JOIN class_section_timeslots cst ON t.timeslot_id = cst.timeslot_id
            WHERE cst.class_id = ?
        """;

        List<TimeSlot> list = new ArrayList<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, classId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int dow = rs.getInt("day_of_week"); // giả định DB: 1=Mon..7=Sun
                DayOfWeek day = DayOfWeek.of(dow);

                TimeSlot ts = new TimeSlot(
                        day,
                        rs.getTime("start_time").toLocalTime(),
                        rs.getTime("end_time").toLocalTime(),
                        rs.getString("room")
                );
                list.add(ts);
            }
        }
        return list;
    }
}
