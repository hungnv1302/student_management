package org.example.repository;

import org.example.dto.MyEnrollmentRow;
import org.example.dto.OpenClassRow;
import org.example.dto.ScheduleRow;
import org.example.dto.TodayScheduleRow;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;

public class EnrollmentRepository {

    // 1) enroll_student(p_student_id, p_class_id) RETURNS void
    public void enrollStudent(String studentId, String classId) throws SQLException {
        String sql = "SELECT qlsv.enroll_student(?, ?)";
        DbFn.exec(sql, ps -> {
            ps.setString(1, studentId);
            ps.setString(2, classId);
        });
    }

    // 2) drop_enrollment(p_student_id, p_class_id) RETURNS void
    public void dropEnrollment(String studentId, String classId) throws SQLException {
        String sql = "SELECT qlsv.drop_enrollment(?, ?)";
        DbFn.exec(sql, ps -> {
            ps.setString(1, studentId);
            ps.setString(2, classId);
        });
    }

    // 3) list_available_sections(p_student_id) RETURNS TABLE(...)
    public List<OpenClassRow> listAvailableSections(String studentId) throws SQLException {
        String sql = "SELECT * FROM qlsv.list_available_sections(?)";
        return DbFn.queryList(sql,
                ps -> ps.setString(1, studentId),
                rs -> {
                    OpenClassRow r = new OpenClassRow();
                    r.setClassId(rs.getString("class_id"));
                    r.setSubjectId(rs.getString("subject_id"));
                    r.setSubjectName(rs.getString("subject_name"));
                    r.setCredit(rs.getInt("credit"));
                    r.setCapacity(rs.getInt("capacity"));
                    r.setEnrolledCount(rs.getLong("enrolled_count"));
                    r.setStatus(rs.getString("status"));
                    r.setTermNo(rs.getShort("term_no"));
                    return r;
                });
    }

    // 4) list_my_enrollments(p_student_id) RETURNS TABLE(...)
    public List<MyEnrollmentRow> listMyEnrollments(String studentId) throws SQLException {
        String sql = "SELECT * FROM qlsv.list_my_enrollments(?)";
        return DbFn.queryList(sql,
                ps -> ps.setString(1, studentId),
                rs -> {
                    MyEnrollmentRow r = new MyEnrollmentRow();
                    r.setClassId(rs.getString("class_id"));
                    r.setSubjectId(rs.getString("subject_id"));
                    r.setSubjectName(rs.getString("subject_name"));
                    r.setCredit(rs.getInt("credit"));
                    r.setTermNo(rs.getShort("term_no"));
                    r.setStatus(rs.getString("status"));
                    return r;
                });
    }

    // 5) get_student_schedule(p_student_id, p_term_no) RETURNS TABLE(...)
    public List<ScheduleRow> getStudentSchedule(String studentId, short termNo) throws SQLException {
        String sql = "SELECT * FROM qlsv.get_student_schedule(?, ?)";
        return DbFn.queryList(sql,
                ps -> {
                    ps.setString(1, studentId);
                    ps.setShort(2, termNo);
                },
                rs -> {
                    ScheduleRow r = new ScheduleRow();
                    r.setDayOfWeek(rs.getInt("day_of_week"));

                    // time without time zone -> LocalTime
                    LocalTime st = rs.getObject("start_time", LocalTime.class);
                    LocalTime et = rs.getObject("end_time", LocalTime.class);
                    r.setStartTime(st);
                    r.setEndTime(et);

                    r.setRoom(rs.getString("room"));
                    r.setClassId(rs.getString("class_id"));
                    r.setSubjectName(rs.getString("subject_name"));
                    return r;
                });
    }
    public java.util.List<TodayScheduleRow> getStudentScheduleToday(String studentId, short termNo) throws java.sql.SQLException {
        String sql = "SELECT * FROM qlsv.get_student_schedule_today(?, ?)";
        return DbFn.queryList(sql,
                ps -> {
                    ps.setString(1, studentId);
                    ps.setShort(2, termNo);
                },
                rs -> {
                    TodayScheduleRow r = new TodayScheduleRow();
                    r.setStartTime(rs.getObject("start_time", LocalTime.class));
                    r.setEndTime(rs.getObject("end_time", LocalTime.class));
                    r.setRoom(rs.getString("room"));
                    r.setClassId(rs.getString("class_id"));
                    r.setSubjectName(rs.getString("subject_name"));
                    return r;
                });
    }

    public int getCurrentCredits(String studentId) throws SQLException {
        String sql = "SELECT qlsv.get_student_current_credits(?) AS credits";
        Integer v = DbFn.queryOne(sql,
                ps -> ps.setString(1, studentId),
                rs -> rs.getInt("credits"));
        return v == null ? 0 : v;
    }

    // 0) get_open_term() RETURNS smallint
    public short getOpenTerm() throws SQLException {
        String sql = "SELECT qlsv.get_open_term() AS term_no";
        Short term = DbFn.queryOne(sql, ps -> {}, rs -> rs.getShort("term_no"));
        if (term == null) throw new RuntimeException("Chưa có kỳ nào đang mở đăng ký (registration_config.is_open=true).");
        return term;
    }

}
