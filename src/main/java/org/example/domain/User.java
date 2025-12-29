package org.example.domain;

import org.example.domain.enums.RoleType;
import org.example.domain.enums.UserState;

import java.time.LocalDateTime;
import java.util.Objects;

public class User {
    private String userID;
    private String username;
    private String password;
    private RoleType role;
    private LocalDateTime lastLogin;
    private UserState state;

    public User() {}

    public User(String userID, String username, String password, RoleType role, UserState state) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.role = role;
        this.state = state;
    }

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

    public UserState getState() { return state; }
    public void setState(UserState state) { this.state = state; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(userID, user.userID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userID);
    }
}
