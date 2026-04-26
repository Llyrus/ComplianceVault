package com.compliancevault.model;

public class User {
    private int userId;
    private String username;
    private String passwordHash; //super insecure update later!
    private Role role;

    public User(int userId, String username, String passwordHash, Role role) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public boolean login() {
        //TODO: finish logic
        return false;
    }

    public void logout() {
        //TODO: also finish this logic.
    }

    public boolean hasPermission(String action) {
        // TODO: update later to be more granular.
        return this.role == Role.ADMIN;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
}
