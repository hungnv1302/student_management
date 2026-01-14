package org.example.service;

public final class SessionContext {

    // loginName: thứ người dùng nhập (username/email)
    private static volatile String loginName;

    // userId: ID thật trong hệ thống (person_id), thường = lecturer_id / student_id
    private static volatile String userId;

    // role: STUDENT / LECTURER / ADMIN
    private static volatile String role;

    private SessionContext() {}

    /** Chuẩn nhất: gọi sau login khi đã có userId thật */
    public static void set(String _loginName, String _role, String _userId) {
        loginName = norm(_loginName);
        role = normRole(_role);
        userId = norm(_userId);
    }

    /**
     * Giữ tương thích code cũ:
     * - Nếu bạn login bằng username 8 số => username chính là person_id => auto set userId = loginName
     * - Nếu bạn login bằng email mà chưa có userId => userId sẽ null (và isLoggedIn() sẽ false)
     */
    public static void set(String _loginName, String _role) {
        loginName = norm(_loginName);
        role = normRole(_role);

        // Nếu loginName giống mã 8 chữ số (person_id) thì dùng luôn làm userId để tránh null
        if (loginName != null && loginName.matches("^\\d{8}$")) {
            userId = loginName;
        } else {
            userId = null;
        }
    }

    public static void clear() {
        loginName = null;
        userId = null;
        role = null;
    }

    public static String getLoginName() {
        return loginName;
    }

    /** ID thật dùng để query dữ liệu theo người đăng nhập */
    public static String getUserId() {
        return userId;
    }

    /** Alias cho code cũ: KHÔNG khuyên dùng để query DB */
    public static String getUsername() {
        return loginName;
    }

    public static String getRole() {
        return role;
    }

    /** Logged in nghĩa là phải có userId thật (để controller dùng được) */
    public static boolean isLoggedIn() {
        return userId != null && !userId.isBlank();
    }

    public static boolean isStudent() {
        return "STUDENT".equalsIgnoreCase(role);
    }

    public static boolean isLecturer() {
        return "LECTURER".equalsIgnoreCase(role);
    }

    public static boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    /** Tiện check nếu bạn muốn */
    public static boolean hasUserId() {
        return isLoggedIn();
    }

    // ===== helpers =====
    private static String norm(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String normRole(String r) {
        String t = norm(r);
        return t == null ? null : t.toUpperCase();
    }
}
