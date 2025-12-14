package org.example.service;

import org.example.config.DbConfig; // nếu package khác thì đổi
import org.example.dto.EnrolledRow;
import org.example.dto.OpenClassRow;

import java.sql.*;
import java.time.LocalTime;
import java.util.*;

public class StudentRegistrationService {

    // ====== PUBLIC API ======

    /** Lớp mở đăng ký (theo registration_config đang is_open=true) */
    public List<OpenClassRow> getOpenClasses(Long studentId) {
        Objects.requireNonNull(studentId, "studentId is null");

        String sql =
                "SELECT cs.class_id, cs.subject_id, s.subject_name, s.credit, " +
                        "       p.full_name AS lecturer_name " +
                        "FROM class_sections cs " +
                        "JOIN subjects s ON s.subject_id = cs.subject_id " +
                        "JOIN lecturers l ON l.lecturer_id = cs.lecturer_id " +
                        "JOIN persons p ON p.person_id = l.person_id " +
                        "JOIN registration_config rc ON rc.semester = cs.semester AND rc.year = cs.year " +
                        "WHERE rc.is_open = TRUE " +
                        "  AND NOW() BETWEEN rc.open_at AND rc.close_at " +
                        // không show lớp đã đăng ký rồi
                        "  AND NOT EXISTS (SELECT 1 FROM enrollments e " +
                        "                  WHERE e.student_id = ? AND e.class_id = cs.class_id AND e.status = 'ENROLLED') " +
                        "ORDER BY cs.class_id";

        List<OpenClassRow> list = new ArrayList<>();
        try (Connection conn = DbConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long classId = rs.getLong("class_id");
                    long subjectId = rs.getLong("subject_id");
                    String subjectName = rs.getString("subject_name");
                    int credit = rs.getInt("credit");
                    String lecturerName = rs.getString("lecturer_name");

                    // “Mã môn” cháu chưa có cột riêng, nên tạm dùng format SUB + subjectId
                    String subjectCode = "SUB" + subjectId;

                    list.add(new OpenClassRow(classId, subjectCode, subjectName, credit, lecturerName));
                }
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("DB error getOpenClasses: " + e.getMessage(), e);
        }
    }

    /** Lớp đã đăng ký của sinh viên */
    public List<EnrolledRow> getEnrolledClasses(Long studentId) {
        Objects.requireNonNull(studentId, "studentId is null");

        // Lấy enroll + subject + timeslots
        String sql =
                "SELECT e.class_id, e.status, s.subject_name, " +
                        "       ts.day_of_week, ts.start_time, ts.end_time " +
                        "FROM enrollments e " +
                        "JOIN class_sections cs ON cs.class_id = e.class_id " +
                        "JOIN subjects s ON s.subject_id = cs.subject_id " +
                        "LEFT JOIN class_section_timeslots cst ON cst.class_id = cs.class_id " +
                        "LEFT JOIN time_slots ts ON ts.timeslot_id = cst.timeslot_id " +
                        "WHERE e.student_id = ? AND e.status = 'ENROLLED' " +
                        "ORDER BY e.class_id, ts.day_of_week, ts.start_time";

        // Gom nhiều timeslot về 1 dòng theo class_id
        Map<Long, EnrolledAgg> map = new LinkedHashMap<>();

        try (Connection conn = DbConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long classId = rs.getLong("class_id");
                    String status = rs.getString("status");
                    String subjectName = rs.getString("subject_name");

                    Integer day = (Integer) rs.getObject("day_of_week"); // có thể null
                    LocalTime start = (LocalTime) rs.getObject("start_time");
                    LocalTime end = (LocalTime) rs.getObject("end_time");

                    map.putIfAbsent(classId, new EnrolledAgg(classId, subjectName, status));

                    if (day != null && start != null && end != null) {
                        map.get(classId).addSlot(day, start, end);
                    }
                }
            }

            List<EnrolledRow> out = new ArrayList<>();
            for (EnrolledAgg agg : map.values()) {
                String classCode = "LHP" + agg.classId; // tạm format
                out.add(new EnrolledRow(agg.classId, classCode, agg.subjectName, agg.buildTimeText(), agg.status));
            }
            return out;

        } catch (SQLException e) {
            throw new RuntimeException("DB error getEnrolledClasses: " + e.getMessage(), e);
        }
    }

    /** Đăng ký lớp: check mở đăng ký + check đã đăng ký + check capacity + check trùng lịch */
    public void register(Long studentId, Long classId) {
        Objects.requireNonNull(studentId, "studentId is null");
        Objects.requireNonNull(classId, "classId is null");

        try (Connection conn = DbConfig.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            // 1) check kỳ mở đăng ký
            if (!isRegistrationOpenForClass(conn, classId)) {
                throw new RuntimeException("Hiện tại chưa mở đăng ký cho lớp này.");
            }

            // 2) đã đăng ký?
            if (existsEnrollment(conn, studentId, classId)) {
                throw new RuntimeException("Bạn đã đăng ký lớp này rồi.");
            }

            // 3) capacity
            int capacity = getClassCapacity(conn, classId);
            int enrolledCount = countEnrolled(conn, classId);
            if (enrolledCount >= capacity) {
                throw new RuntimeException("Lớp đã đủ sĩ số.");
            }

            // 4) trùng lịch
            if (hasScheduleConflict(conn, studentId, classId)) {
                throw new RuntimeException("Trùng lịch học với lớp đã đăng ký.");
            }

            // 5) insert enrollment
            insertEnrollment(conn, studentId, classId);

            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("DB error register: " + e.getMessage(), e);
        }
    }

    /** Hủy đăng ký: update status */
    public void drop(Long studentId, Long classId) {
        Objects.requireNonNull(studentId, "studentId is null");
        Objects.requireNonNull(classId, "classId is null");

        String sql = "UPDATE enrollments SET status = 'DROPPED' WHERE student_id = ? AND class_id = ? AND status = 'ENROLLED'";
        try (Connection conn = DbConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, studentId);
            ps.setLong(2, classId);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new RuntimeException("Không tìm thấy đăng ký đang hoạt động để hủy.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error drop: " + e.getMessage(), e);
        }
    }

    // ====== PRIVATE HELPERS ======

    private boolean isRegistrationOpenForClass(Connection conn, Long classId) throws SQLException {
        String sql =
                "SELECT 1 " +
                        "FROM class_sections cs " +
                        "JOIN registration_config rc ON rc.semester = cs.semester AND rc.year = cs.year " +
                        "WHERE cs.class_id = ? AND rc.is_open = TRUE AND NOW() BETWEEN rc.open_at AND rc.close_at " +
                        "LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean existsEnrollment(Connection conn, Long studentId, Long classId) throws SQLException {
        String sql = "SELECT 1 FROM enrollments WHERE student_id = ? AND class_id = ? AND status = 'ENROLLED' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            ps.setLong(2, classId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private int getClassCapacity(Connection conn, Long classId) throws SQLException {
        String sql = "SELECT capacity FROM class_sections WHERE class_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new RuntimeException("Không tồn tại lớp học phần.");
                return rs.getInt("capacity");
            }
        }
    }

    private int countEnrolled(Connection conn, Long classId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE class_id = ? AND status = 'ENROLLED'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Check overlap: cùng day_of_week và start < other_end && end > other_start */
    private boolean hasScheduleConflict(Connection conn, Long studentId, Long newClassId) throws SQLException {
        String sql =
                "SELECT 1 " +
                        "FROM enrollments e " +
                        "JOIN class_section_timeslots cst ON cst.class_id = e.class_id " +
                        "JOIN time_slots ts ON ts.timeslot_id = cst.timeslot_id " +
                        "JOIN class_section_timeslots cst2 ON cst2.class_id = ? " +
                        "JOIN time_slots ts2 ON ts2.timeslot_id = cst2.timeslot_id " +
                        "WHERE e.student_id = ? AND e.status = 'ENROLLED' " +
                        "  AND ts.day_of_week = ts2.day_of_week " +
                        "  AND ts.start_time < ts2.end_time " +
                        "  AND ts.end_time > ts2.start_time " +
                        "LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, newClassId);
            ps.setLong(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void insertEnrollment(Connection conn, Long studentId, Long classId) throws SQLException {
        // enrollment_id nếu là SERIAL thì không cần set
        String sql = "INSERT INTO enrollments (student_id, class_id, status, created_at) VALUES (?, ?, 'ENROLLED', NOW())";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            ps.setLong(2, classId);
            ps.executeUpdate();
        }
    }

    // helper class gộp timeslots
    private static class EnrolledAgg {
        final long classId;
        final String subjectName;
        final String status;
        final List<String> slots = new ArrayList<>();

        EnrolledAgg(long classId, String subjectName, String status) {
            this.classId = classId;
            this.subjectName = subjectName;
            this.status = status;
        }

        void addSlot(int day, LocalTime start, LocalTime end) {
            slots.add("T" + day + " " + start + "-" + end);
        }

        String buildTimeText() {
            if (slots.isEmpty()) return "";
            return String.join(", ", slots);
        }
    }
}
