package com.compliancevault.service;

import com.compliancevault.dao.UserDAO;
import com.compliancevault.model.Role;
import com.compliancevault.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class AuthService {
    private final UserDAO userDAO;
    private User currentUser;  // session state — who's logged in right now

    public AuthService() {
        this.userDAO = new UserDAO();
    }


    //Creates a new user with a securely hashed password.
    //The plain-text password is never stored, only the bcrypt hash
    public void registerUser(String username, String plainPassword, Role role) throws SQLException {
        String hashed = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        User user = new User(0, username, hashed, role);
        userDAO.insert(user);
    }

    //Verifies credentials and starts a session.
    //Returns true on success, false on any failure (wrong username, wrong password)
    // Same response either way, doesn't leak whether the username exists
    public boolean login(String username, String plainPassword) throws SQLException {
        User user = userDAO.findByUsername(username);
        if (user == null) return false;

        if (BCrypt.checkpw(plainPassword, user.getPasswordHash())) {
            this.currentUser = user;
            return true;
        }
        return false;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }


    //Permission check — used by the GUI to enable/disable admin features
    // and by the service layer to gate write operations.
    public boolean hasAdminAccess() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }

    // second constructor for testing
    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
}