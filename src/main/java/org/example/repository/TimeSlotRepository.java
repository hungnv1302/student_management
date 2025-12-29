package org.example.repository;

import org.example.dto.LecturerExamRow;
import org.example.dto.LecturerTimetableRow;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class TimeSlotRepository {

    public void addTimeSlot(String classId, int dayOfWeek, LocalTime start, LocalTime end) throws SQLException {
        String sql = "SELECT qlsv.add_time_slot(?, ?, ?, ?)";
        DbFn.exec(sql, ps -> {
            ps.setString(1, classId);
            ps.setInt(2, dayOfWeek);
            ps.setObject(3, start);
            ps.setObject(4, end);
        });
    }

    // ✅ Lịch dạy (schedule) giảng viên theo kỳ (year/sem) - dùng đúng function DB hiện có
    public List<LecturerTimetableRow> lecturerSchedule(String lecturerId, int termYear, short termSem) throws SQLException {
        String sql = "SELECT * FROM qlsv.get_lecturer_schedule(?, ?, ?::smallint)";
        return DbFn.queryList(sql,
                ps -> {
                    ps.setString(1, lecturerId);
                    ps.setInt(2, termYear);
                    ps.setShort(3, termSem);
                },
                rs -> {
                    LecturerTimetableRow r = new LecturerTimetableRow();
                    r.setDayOfWeek(rs.getInt("day_of_week"));
                    r.setStartTime(rs.getObject("start_time", LocalTime.class));
                    r.setEndTime(rs.getObject("end_time", LocalTime.class));
                    r.setRoom(rs.getString("room"));
                    r.setClassId(rs.getString("class_id"));
                    r.setSubjectName(rs.getString("subject_name"));

                    // DB function không trả subject_id -> để null (hoặc bỏ field subjectId)
                    r.setSubjectId(null);
                    return r;
                });
    }

    // ✅ Lịch thi giảng viên theo kỳ (year/sem)
    public List<LecturerExamRow> lecturerExamSchedule(String lecturerId, int termYear, short termSem) throws SQLException {
        String sql = "SELECT * FROM qlsv.lecturer_exam_schedule(?, ?, ?::smallint)";
        return DbFn.queryList(sql,
                ps -> {
                    ps.setString(1, lecturerId);
                    ps.setInt(2, termYear);
                    ps.setShort(3, termSem);
                },
                rs -> {
                    LecturerExamRow r = new LecturerExamRow();
                    r.setExamDate(rs.getObject("exam_date", LocalDate.class));
                    r.setStartTime(rs.getObject("start_time", LocalTime.class));
                    r.setEndTime(rs.getObject("end_time", LocalTime.class));
                    r.setRoom(rs.getString("room"));
                    r.setExamType(rs.getString("exam_type"));
                    r.setClassId(rs.getString("class_id"));
                    r.setSubjectId(rs.getString("subject_id"));
                    r.setSubjectName(rs.getString("subject_name"));
                    return r;
                });
    }
}
