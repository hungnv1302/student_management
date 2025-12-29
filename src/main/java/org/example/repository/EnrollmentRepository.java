package org.example.repository;

import org.example.dto.MyEnrollmentRow;
import org.example.dto.OpenClassRow;
import org.example.dto.ScheduleRow;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class EnrollmentRepository {

    /**
     * ✅ Đăng ký lớp: TX + khóa row sections FOR UPDATE để tránh vượt sĩ số
     * Rule:
     * - registration_config.is_open = true
     * - lớp thuộc open term, sections.status='OPEN'
     * - chưa enroll lớp này
     * - chưa PASSED môn này (finalized + total_score >= 4)
     * - chưa enroll môn này trong open term
     * - không vượt max_credits
     * - không trùng lịch với các lớp đã enroll trong open term
     * - còn chỗ (count < capacity)
     */
    public void enrollStudent(String studentId, String classId) throws SQLException {
        Objects.requireNonNull(studentId);
        Objects.requireNonNull(classId);

        DbFn.tx(c -> {
            OpenConfig cfg = getOpenConfig(c);
            if (!cfg.isOpen) throw new SQLException("Registration is closed");

            // 1) Lock section row
            Section sec = getSectionForUpdate(c, classId);
            if (sec == null) throw new SQLException("Class not found");

            // 2) Must belong to open term
            if (sec.termYear != cfg.termYear || sec.termSem != cfg.termSem) {
                throw new SQLException("Class " + classId + " is not in open term " + cfg.termYear + "." + cfg.termSem);
            }

            // 3) Section must be OPEN
            if (!"OPEN".equalsIgnoreCase(nvl(sec.status))) {
                throw new SQLException("Class is not OPEN");
            }

            // 4) Already enrolled this class?
            if (exists(c,
                    "SELECT 1 FROM qlsv.enrollments WHERE student_id=? AND class_id=? LIMIT 1",
                    ps -> { ps.setString(1, studentId); ps.setString(2, classId); }
            )) {
                throw new SQLException("Bạn đã đăng ký lớp này rồi.");
            }

            // 5) Block if already PASSED subject
            if (exists(c, """
                    SELECT 1
                    FROM qlsv.enrollments e
                    JOIN qlsv.sections s ON s.class_id = e.class_id
                    WHERE e.student_id = ?
                      AND s.subject_id = ?
                      AND e.is_finalized = true
                      AND e.total_score IS NOT NULL
                      AND e.total_score >= 4
                    LIMIT 1
                    """,
                    ps -> { ps.setString(1, studentId); ps.setString(2, sec.subjectId); }
            )) {
                throw new SQLException("Không thể đăng ký: bạn đã qua môn " + sec.subjectId);
            }

            // 6) Block if already enrolled another class of same subject in open term
            if (exists(c, """
                    SELECT 1
                    FROM qlsv.enrollments e
                    JOIN qlsv.sections s ON s.class_id = e.class_id
                    WHERE e.student_id = ?
                      AND s.term_year = ? AND s.term_sem = ?
                      AND s.subject_id = ?
                    LIMIT 1
                    """,
                    ps -> {
                        ps.setString(1, studentId);
                        ps.setInt(2, cfg.termYear);
                        ps.setShort(3, cfg.termSem);
                        ps.setString(4, sec.subjectId);
                    }
            )) {
                throw new SQLException("Không thể đăng ký: bạn đã đăng ký môn " + sec.subjectId + " trong kỳ này rồi.");
            }

            // 7) Credit limit check
            int classCredit = queryInt(c, """
                    SELECT sub.credit
                    FROM qlsv.subjects sub
                    WHERE sub.subject_id = ?
                    """, ps -> ps.setString(1, sec.subjectId));

            int sumCredits = queryInt(c, """
                    SELECT COALESCE(SUM(sub.credit),0)
                    FROM qlsv.enrollments e
                    JOIN qlsv.sections s ON s.class_id = e.class_id
                    JOIN qlsv.subjects sub ON sub.subject_id = s.subject_id
                    WHERE e.student_id = ?
                      AND s.term_year = ? AND s.term_sem = ?
                    """, ps -> {
                ps.setString(1, studentId);
                ps.setInt(2, cfg.termYear);
                ps.setShort(3, cfg.termSem);
            });

            if (sumCredits + classCredit > cfg.maxCredits) {
                throw new SQLException("Vượt quá số tín chỉ tối đa (" + cfg.maxCredits + ").");
            }

            // 8) Schedule conflict check
            String conflictClass = queryString(c, """
                    SELECT e2.class_id
                    FROM qlsv.enrollments e2
                    JOIN qlsv.sections s2 ON s2.class_id = e2.class_id
                    JOIN qlsv.time_slots t2 ON t2.class_id = e2.class_id
                    JOIN qlsv.time_slots t1 ON t1.class_id = ?
                    WHERE e2.student_id = ?
                      AND s2.term_year = ? AND s2.term_sem = ?
                      AND t2.day_of_week = t1.day_of_week
                      AND t1.start_time < t2.end_time
                      AND t2.start_time < t1.end_time
                    LIMIT 1
                    """, ps -> {
                ps.setString(1, classId);
                ps.setString(2, studentId);
                ps.setInt(3, cfg.termYear);
                ps.setShort(4, cfg.termSem);
            });

            if (conflictClass != null && !conflictClass.isBlank()) {
                throw new SQLException("Trùng lịch với lớp " + conflictClass);
            }

            // 9) Capacity check (still inside locked section row)
            int enrolledCount = queryInt(c,
                    "SELECT COUNT(*) FROM qlsv.enrollments WHERE class_id = ?",
                    ps -> ps.setString(1, classId)
            );
            if (enrolledCount >= sec.capacity) {
                throw new SQLException("Lớp đã đủ sĩ số.");
            }

            // 10) Insert enrollment
            DbFn.execUpdate(c,
                    "INSERT INTO qlsv.enrollments(student_id, class_id) VALUES (?, ?)",
                    ps -> { ps.setString(1, studentId); ps.setString(2, classId); }
            );

            return null;
        });
    }

    public void dropEnrollment(String studentId, String classId) throws SQLException {
        Objects.requireNonNull(studentId);
        Objects.requireNonNull(classId);

        DbFn.tx(c -> {
            OpenConfig cfg = getOpenConfig(c);
            if (!cfg.isOpen) throw new SQLException("Registration is closed");

            String sql = """
                DELETE FROM qlsv.enrollments e
                USING qlsv.sections s
                WHERE e.class_id = s.class_id
                  AND e.student_id = ?
                  AND e.class_id = ?
                  AND s.term_year = ?
                  AND s.term_sem  = ?
                  AND e.is_finalized = false
            """;

            int affected = DbFn.execUpdate(c, sql, ps -> {
                ps.setString(1, studentId);
                ps.setString(2, classId);
                ps.setInt(3, cfg.termYear);
                ps.setShort(4, cfg.termSem);
            });

            if (affected == 0) {
                throw new SQLException("Không thể hủy: không tìm thấy, không thuộc kỳ đăng ký, hoặc đã chốt điểm.");
            }
            return null;
        });
    }

    /**
     * ✅ Danh sách lớp mở đăng ký: lấy từ v_open_sections + Java tính eligibility/reason
     * (vì bạn đã xóa function list_open_sections_for_student)
     */
    public List<OpenClassRow> listOpenSectionsForStudent(String studentId) throws SQLException {
        Objects.requireNonNull(studentId);

        // 1) load open config
        OpenConfig cfg = DbFn.queryOne(
                "SELECT term_year, term_sem, is_open, max_credits FROM qlsv.registration_config WHERE id=1",
                null,
                rs -> new OpenConfig(rs.getInt("term_year"), rs.getShort("term_sem"),
                        rs.getBoolean("is_open"), rs.getInt("max_credits"))
        );

        // 2) base open sections from view (status='OPEN' in view means "belongs to open term")
        String sql = """
            SELECT v.class_id, v.subject_id, v.subject_name, v.credit,
                   v.lecturer_name, v.schedule_text,
                   v.enrolled_count, v.capacity, v.remaining_seats
            FROM qlsv.v_open_sections v
            JOIN qlsv.sections s ON s.class_id = v.class_id
            WHERE v.status = 'OPEN'
              AND s.status = 'OPEN'
            ORDER BY v.subject_id, v.class_id
        """;

        List<OpenClassRow> list = DbFn.queryList(sql, null, rs -> {
            OpenClassRow r = new OpenClassRow();
            r.setClassId(rs.getString("class_id"));
            r.setSubjectId(rs.getString("subject_id"));
            r.setSubjectName(rs.getString("subject_name"));
            r.setCredit(rs.getInt("credit"));
            r.setLecturerName(rs.getString("lecturer_name"));
            r.setScheduleText(rs.getString("schedule_text"));
            r.setEnrolledCount(rs.getInt("enrolled_count"));
            r.setCapacity(rs.getInt("capacity"));
            r.setRemainingSeats(rs.getInt("remaining_seats"));
            return r;
        });

        // Nếu đóng đăng ký thì đánh dấu CLOSED luôn
        if (!cfg.isOpen) {
            for (OpenClassRow r : list) {
                r.setEligibility("CLOSED");
                r.setReason("Registration is closed");
            }
            return list;
        }

        // 3) Preload sets for eligibility
        Set<String> passedSubjects = new HashSet<>(DbFn.queryList("""
                SELECT DISTINCT s.subject_id
                FROM qlsv.enrollments e
                JOIN qlsv.sections s ON s.class_id = e.class_id
                WHERE e.student_id = ?
                  AND e.is_finalized = true
                  AND e.total_score IS NOT NULL
                  AND e.total_score >= 4
                """, ps -> ps.setString(1, studentId), rs -> rs.getString(1)));

        Set<String> enrolledOpenSubjects = new HashSet<>(DbFn.queryList("""
                SELECT DISTINCT s.subject_id
                FROM qlsv.enrollments e
                JOIN qlsv.sections s ON s.class_id = e.class_id
                WHERE e.student_id = ?
                  AND s.term_year = ? AND s.term_sem = ?
                """, ps -> {
            ps.setString(1, studentId);
            ps.setInt(2, cfg.termYear);
            ps.setShort(3, cfg.termSem);
        }, rs -> rs.getString(1)));

        Set<String> enrolledOpenClasses = new HashSet<>(DbFn.queryList("""
                SELECT e.class_id
                FROM qlsv.enrollments e
                JOIN qlsv.sections s ON s.class_id = e.class_id
                WHERE e.student_id = ?
                  AND s.term_year = ? AND s.term_sem = ?
                """, ps -> {
            ps.setString(1, studentId);
            ps.setInt(2, cfg.termYear);
            ps.setShort(3, cfg.termSem);
        }, rs -> rs.getString(1)));

        // conflict set: class_id that conflicts with existing open enrollments
        Set<String> conflictClasses = new HashSet<>(DbFn.queryList("""
                SELECT DISTINCT v.class_id
                FROM qlsv.v_open_sections v
                JOIN qlsv.time_slots t_new ON t_new.class_id = v.class_id
                JOIN qlsv.enrollments e2 ON e2.student_id = ?
                JOIN qlsv.sections s2 ON s2.class_id = e2.class_id
                JOIN qlsv.time_slots t_old ON t_old.class_id = e2.class_id
                WHERE v.status = 'OPEN'
                  AND s2.term_year = ? AND s2.term_sem = ?
                  AND t_old.day_of_week = t_new.day_of_week
                  AND t_new.start_time < t_old.end_time
                  AND t_old.start_time < t_new.end_time
                """, ps -> {
            ps.setString(1, studentId);
            ps.setInt(2, cfg.termYear);
            ps.setShort(3, cfg.termSem);
        }, rs -> rs.getString(1)));

        // 4) Apply eligibility rule
        for (OpenClassRow r : list) {
            String classId = nvl(r.getClassId());
            String subjectId = nvl(r.getSubjectId());

            if (r.getRemainingSeats() <= 0) {
                r.setEligibility("FULL");
                r.setReason("Class is full");
            } else if (enrolledOpenClasses.contains(classId)) {
                r.setEligibility("ENROLLED");
                r.setReason("Already enrolled this class");
            } else if (enrolledOpenSubjects.contains(subjectId)) {
                r.setEligibility("BLOCKED");
                r.setReason("Already enrolled another class of this subject in open term");
            } else if (conflictClasses.contains(classId)) {
                r.setEligibility("CONFLICT");
                r.setReason("Schedule conflict");
            } else if (passedSubjects.contains(subjectId)) {
                r.setEligibility("BLOCKED");
                r.setReason("Already passed this subject");
            } else {
                r.setEligibility("ELIGIBLE");
                r.setReason(null);
            }
        }

        return list;
    }

    /**
     * ✅ TKB theo kỳ (year/sem) - không dùng function.
     * room = COALESCE(time_slots.room, sections.room)
     */
    public List<ScheduleRow> getStudentSchedule(String studentId, int termYear, short termSem) throws SQLException {

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
        JOIN qlsv.sections s      ON s.class_id = e.class_id
        JOIN qlsv.subjects sub    ON sub.subject_id = s.subject_id
        JOIN qlsv.time_slots ts   ON ts.class_id = s.class_id
        LEFT JOIN qlsv.persons lp ON lp.person_id = s.lecturer_id
        WHERE e.student_id = ?
          AND s.term_year = ?
          AND s.term_sem  = ?
        ORDER BY ts.day_of_week, ts.start_time
    """;

        return DbFn.queryList(sql,
                ps -> {
                    ps.setString(1, studentId);
                    ps.setInt(2, termYear);
                    ps.setShort(3, termSem);
                },
                rs -> {
                    ScheduleRow r = new ScheduleRow();
                    r.setClassId(rs.getString("class_id"));
                    r.setSubjectName(rs.getString("subject_name"));
                    r.setDayOfWeek(rs.getInt("day_of_week"));
                    r.setStartTime(rs.getObject("start_time", java.time.LocalTime.class));
                    r.setEndTime(rs.getObject("end_time", java.time.LocalTime.class));
                    r.setRoom(rs.getString("room"));
                    r.setLecturerName(rs.getString("lecturer_name"));
                    return r;
                }
        );
    }

    public List<MyEnrollmentRow> listMyOpenEnrollments(String studentId) throws SQLException {
        String sql = """
            SELECT e.class_id,
                   s.subject_id,
                   sub.subject_name,
                   sub.credit,
                   e.status,
                   string_agg(
                     'T' || ts.day_of_week || ' ' ||
                     to_char(ts.start_time,'HH24:MI') || '-' || to_char(ts.end_time,'HH24:MI') ||
                     COALESCE(' | ' || COALESCE(ts.room, s.room),'')
                   , '; ' ORDER BY ts.day_of_week, ts.start_time
                   ) AS schedule_text
            FROM qlsv.enrollments e
            JOIN qlsv.sections s ON s.class_id = e.class_id
            JOIN qlsv.subjects sub ON sub.subject_id = s.subject_id
            JOIN qlsv.registration_config rc ON rc.id=1
            LEFT JOIN qlsv.time_slots ts ON ts.class_id = e.class_id
            WHERE e.student_id = ?
              AND s.term_year = rc.term_year AND s.term_sem = rc.term_sem
            GROUP BY e.class_id, s.subject_id, sub.subject_name, sub.credit, e.status
            ORDER BY s.subject_id, e.class_id
        """;

        return DbFn.queryList(sql, ps -> ps.setString(1, studentId), rs -> {
            MyEnrollmentRow r = new MyEnrollmentRow();
            r.setClassId(rs.getString("class_id"));
            r.setSubjectId(rs.getString("subject_id"));
            r.setSubjectName(rs.getString("subject_name"));
            r.setCredit(rs.getInt("credit"));
            r.setStatus(rs.getString("status"));
            r.setScheduleText(rs.getString("schedule_text"));
            return r;
        });
    }

    /** ✅ Tổng tín chỉ SV đã đăng ký trong open term - không dùng function */
    public int getOpenCredits(String studentId) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(sub.credit),0) AS credits
            FROM qlsv.enrollments e
            JOIN qlsv.sections s ON s.class_id = e.class_id
            JOIN qlsv.subjects sub ON sub.subject_id = s.subject_id
            JOIN qlsv.registration_config rc ON rc.id=1
            WHERE e.student_id = ?
              AND s.term_year = rc.term_year AND s.term_sem = rc.term_sem
        """;

        Integer v = DbFn.queryOne(sql,
                ps -> ps.setString(1, studentId),
                rs -> rs.getInt("credits"));
        return v == null ? 0 : v;
    }

    // =========================
    // Internal helpers
    // =========================

    private OpenConfig getOpenConfig(Connection c) throws SQLException {
        return DbFn.queryOne(c,
                "SELECT term_year, term_sem, is_open, max_credits FROM qlsv.registration_config WHERE id=1",
                null,
                rs -> new OpenConfig(
                        rs.getInt("term_year"),
                        rs.getShort("term_sem"),
                        rs.getBoolean("is_open"),
                        rs.getInt("max_credits")
                )
        );
    }

    private Section getSectionForUpdate(Connection c, String classId) throws SQLException {
        String sql = """
            SELECT class_id, subject_id, term_year, term_sem, capacity, status
            FROM qlsv.sections
            WHERE class_id = ?
            FOR UPDATE
        """;
        return DbFn.queryOne(c, sql, ps -> ps.setString(1, classId), rs ->
                new Section(
                        rs.getString("class_id"),
                        rs.getString("subject_id"),
                        rs.getInt("term_year"),
                        rs.getShort("term_sem"),
                        rs.getInt("capacity"),
                        rs.getString("status")
                )
        );
    }

    private boolean exists(Connection c, String sql, DbFn.SqlBinder binder) throws SQLException {
        Integer v = DbFn.queryScalar(c, sql, binder, rs -> 1);
        return v != null;
    }

    private int queryInt(Connection c, String sql, DbFn.SqlBinder binder) throws SQLException {
        Integer v = DbFn.queryOne(c, sql, binder, rs -> rs.getInt(1));
        return v == null ? 0 : v;
    }

    private String queryString(Connection c, String sql, DbFn.SqlBinder binder) throws SQLException {
        return DbFn.queryOne(c, sql, binder, rs -> rs.getString(1));
    }

    private static String nvl(String s) { return s == null ? "" : s; }

    private record OpenConfig(int termYear, short termSem, boolean isOpen, int maxCredits) {}
    private record Section(String classId, String subjectId, int termYear, short termSem, int capacity, String status) {}
}
