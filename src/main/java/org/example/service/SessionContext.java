package org.example.service;

public class SessionContext {

    // loginName: thứ người dùng nhập (username/email)
    private static String loginName;

    // userId: ID thật trong hệ thống (person_id), thường = lecturer_id / student_id
    private static String userId;

    private static String role;

    private SessionContext() {}

    /** Chuẩn nhất: gọi sau login khi đã có userId thật */
    public static void set(String _loginName, String _role, String _userId) {
        loginName = _loginName;
        role = _role;
        userId = _userId;
    }

    /** Giữ tương thích code cũ: nếu chỉ set 2 tham số thì userId chưa có */
    public static void set(String _loginName, String _role) {
        loginName = _loginName;
        role = _role;
        userId = null;
    }

    public static void clear() {
        loginName = null;
        userId = null;
        role = null;
    }

    public static String getLoginName() { return loginName; }

    /** Controller cần dùng cái này để query dữ liệu theo người đăng nhập */
    public static String getUserId() { return userId; }

    /** Alias cho code cũ (nhưng không khuyên dùng) */
    public static String getUsername() { return loginName; }

    public static String getRole() { return role; }

    public static boolean isLoggedIn() { return loginName != null; }

    public static boolean isStudent() { return "STUDENT".equalsIgnoreCase(role); }
    public static boolean isLecturer() { return "LECTURER".equalsIgnoreCase(role); }
}
