package model;

import java.util.Objects;

import util.Passwords;

public class User {
    private String userId;
    private String username;
    private String password;
    private UserRole role;
    private boolean loggedIn;

    public User(String userId, String username, String password, UserRole role) {
        this(userId, username, password, role, false);
    }

    public User(String userId, String username, String password, UserRole role, boolean isHashed) {
        setUserId(userId);
        setUsername(username);
        setRole(role);

        if (isHashed) {
            this.password = password;
        } else {
            setPassword(password);
        }
        loggedIn = false;
    }

    public boolean login() {
        if (username == null || username.trim().isEmpty() || password == null) {
            return false;
        }
        this.loggedIn = true;
        return true;
    }

    public void logout() {
        this.loggedIn = false;
    }

    public void resetPassword() {
        // In a real system, this would send an email or generate a temporary password
        this.password = generateTemporaryPassword();
    }

    public boolean authenticate() {
        return loggedIn && username != null && !username.trim().isEmpty();
    }

    private String generateTemporaryPassword() {
        // Generate a strong temporary password: Temp{timestamp}!@
        // Example: Temp1234567890!@
        long timestamp = System.currentTimeMillis() % 1000000;
        return "Temp" + timestamp + "!@";
    }

    public boolean changePassword(String oldPassword, String newPassword) {
        if (Objects.equals(this.password, oldPassword) && newPassword != null && newPassword.length() >= 6) {
            this.password = newPassword;
            return true;
        }
        return false;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            // Generate temporary password if none provided
            this.password = Passwords.hashPassword(generateTemporaryPassword());
            return;
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        if (!Passwords.isStrongPassword(password)) {
            throw new IllegalArgumentException(
                    "Password must contain uppercase, lowercase, digit, and special character");
        }

        this.password = Passwords.hashPassword(password);
    }

    // Add method to verify password without hashing
    public boolean verifyPassword(String inputPassword) {
        return Passwords.verifyPassword(inputPassword, this.password);
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        this.role = role;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", loggedIn=" + loggedIn +
                '}';
    }
}