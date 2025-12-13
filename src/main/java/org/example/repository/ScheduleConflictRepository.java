package org.example.repository;

import java.sql.*;

public class ScheduleConflictRepository {

    // TRUE nếu classA và classB bị trùng (cùng day, time overlap)
    public boolean isConflict(String classA, String classB, Connection c) throws SQLException {
        String sql = """
            SELECT 1
            FROM public.class_section_timeslots a
            JOIN public.time_slots ta ON ta.timeslot_id = a.timeslot_id
            JOIN public.class_section_timeslots b ON b.class_id = ?
            JOIN public.time_slots tb ON tb.timeslot_id = b.timeslot_id
            WHERE a.class_id = ?
              AND ta.day_of_week = tb.day_of_week
              AND ta.start_time < tb.end_time
              AND tb.start_time < ta.end_time
            LIMIT 1
            """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, classB);
            ps.setString(2, classA);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
