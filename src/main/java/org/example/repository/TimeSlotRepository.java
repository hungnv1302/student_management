package org.example.repository;

import org.example.dto.LecturerExamRow;
import org.example.dto.LecturerTimetableRow;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class TimeSlotRepository {

    // (A) Thêm ca học: SELECT qlsv.add_time_slot(?,?,?,?)
    public void addTimeSlot(String classId, int dayOfWeek, LocalTime start, LocalTime end) throws SQLException {
        String sql = "SELECT qlsv.add_time_slot(?, ?, ?, ?)";
        DbFn.exec(sql, ps -> {
            ps.setString(1, classId);
            ps.setInt(2, dayOfWeek);
            ps.setObject(3, start); // LocalTime
            ps.setObject(4, end);   // LocalTime
        });
    }

    // (B) Thời khóa biểu giảng viên
    public List<LecturerTimetableRow> lecturerTimetable(String lecturerId, short termNo) throws SQLException {
        String sql = "SELECT * FROM qlsv.lecturer_timetable(?, ?)";
        return DbFn.queryList(sql,
                ps -> {
                    ps.setString(1, lecturerId);
                    ps.setShort(2, termNo);
                },
                rs -> {
                    LecturerTimetableRow r = new LecturerTimetableRow();
                    r.setDayOfWeek(rs.getInt("day_of_week"));
                    r.setStartTime(rs.getObject("start_time", LocalTime.class));
                    r.setEndTime(rs.getObject("end_time", LocalTime.class));
                    r.setRoom(rs.getString("room"));
                    r.setClassId(rs.getString("class_id"));
                    r.setSubjectId(rs.getString("subject_id"));
                    r.setSubjectName(rs.getString("subject_name"));
                    return r;
                });
    }

    // (C) Lịch thi giảng viên
    public List<LecturerExamRow> lecturerExamSchedule(String lecturerId, short termNo) throws SQLException {
        String sql = "SELECT * FROM qlsv.lecturer_exam_schedule(?, ?)";
        return DbFn.queryList(sql,
                ps -> {
                    ps.setString(1, lecturerId);
                    ps.setShort(2, termNo);
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
