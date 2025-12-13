package org.example.service;

public class SessionContext {
    private static String userId;
    private static String username;
    private static String role; // ADMIN / LECTURER / STUDENT

    public static void set(String _userId, String _username, String _role) {
        userId = _userId;
        username = _username;
        role = _role;
    }

    public static void clear() {
        userId = null;
        username = null;
        role = null;
    }

    public static String getUserId() { return userId; }
    public static String getUsername() { return username; }
    public static String getRole() { return role; }

    public static boolean isLoggedIn() { return userId != null; }
}
