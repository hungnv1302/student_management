package org.example.service;

public class SessionContext {

    // username = ID duy nhất (student_id / lecturer_id / admin_id)
    private static String username;
    private static String role; // ADMIN / LECTURER / STUDENT

    private SessionContext() {}

    public static void set(String _username, String _role) {
        username = _username;
        role = _role;
    }

    public static void clear() {
        username = null;
        role = null;
    }

    public static String getUsername() {
        return username;
    }

    public static String getRole() {
        return role;
    }

    public static boolean isLoggedIn() {
        return username != null;
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
}
