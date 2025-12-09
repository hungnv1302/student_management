package org.example.service;

import org.example.domain.User;
import org.example.repository.Database;

public class AuthService {

    private final Database db;
    private User currentUser;

    public AuthService(Database db) {
        this.db = db;
    }

    public User login(String username, String password) {

        for (User u : db.getUsers()) {
            if (u.login(username, password)) {   // dùng method của bạn
                currentUser = u;
                return u;
            }
        }
        return null;
    }

    public void logout() {
        if (currentUser != null) currentUser.logout();
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
