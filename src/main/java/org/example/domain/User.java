package org.example.domain;

import java.time.LocalDateTime;

public class User {

    private String userID;
    private String username;
    private String password;
    private RoleType role;
    private LocalDateTime lastLogin;
    private String state;

    // Quan hệ belongsTo Person
    private Person person;

    public User() {
    }

    public User(String userID, String username, String password, RoleType role, Person person) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.role = role;
        this.person = person;
        this.state = "ACTIVE";
    }

    public boolean login(String username, String password) {
        boolean ok = this.username != null
                && this.username.equals(username)
                && this.password != null
                && this.password.equals(password);
        if (ok) {
            lastLogin = LocalDateTime.now();
            state = "ACTIVE";
        }
        return ok;
    }

    public void logout() {
        state = "INACTIVE";
    }

    public void forgotPassword(String email) {
        System.out.println("Forgot password for user " + username + ", email: " + email);
    }

    public boolean changePassword(String oldPw, String newPw) {
        if (password != null && password.equals(oldPw)) {
            this.password = newPw;
            return true;
        }
        return false;
    }

    public boolean hasPermission(String action) {
        // tạm thời cho phép hết; sau này gắn logic theo role
        return true;
    }

    // Getters & setters
    public String getUserID() { return userID; }
    public void setUserID(String userID) { this.userID = userID; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public RoleType getRole() { return role; }
    public void setRole(RoleType role) { this.role = role; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public Person getPerson() { return person; }
    public void setPerson(Person person) { this.person = person; }
}
