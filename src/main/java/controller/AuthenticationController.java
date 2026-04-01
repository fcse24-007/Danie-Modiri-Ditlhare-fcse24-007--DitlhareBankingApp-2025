// AuthenticationController.java
package controller;

import database.*;
import model.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class AuthenticationController {
    private UserDAO userDAO;
    private AuditDAO auditDAO;
    private User currentUser;
    private static final int MAX_ATTEMPTS = getIntProperty("auth.maxAttempts", 5);
    private static final long LOCKOUT_MILLIS = TimeUnit.SECONDS.toMillis(
            getLongProperty("auth.lockoutSeconds", 45));
    private final Map<String, LoginAttempt> attemptTracker = new ConcurrentHashMap<>();

    public AuthenticationController() {
        this.userDAO = new UserDAO();
        this.auditDAO = new AuditDAO();
    }

    
    public LoginResult login(String username, String password) {
        System.out.println("AuthenticationController: Processing login for " + username);
        String key = username == null ? "" : username.trim().toLowerCase();

        // Check lockout state before hitting the database
        LoginAttempt attempt = attemptTracker.getOrDefault(key, new LoginAttempt());
        long now = System.currentTimeMillis();
        if (attempt.lockoutUntil > 0 && attempt.lockoutUntil <= now) {
            attempt.failures = 0;
            attempt.lockoutUntil = 0;
            attemptTracker.remove(key);
        }
        if (attempt.lockoutUntil > now) {
            long secondsLeft = TimeUnit.MILLISECONDS.toSeconds(attempt.lockoutUntil - now) + 1;
            return new LoginResult(false, null,
                    "Too many failed attempts. Please try again in " + secondsLeft + "s.", null);
        }
        
        // Step 2-3: Authenticate user
        boolean authResult = userDAO.authenticate(username, password);
        
        if (authResult) {
            attemptTracker.remove(key);
            // Step 5-6: Get user role
            var userOpt = userDAO.findByUsername(username);
            if (userOpt.isPresent()) {
                currentUser = userOpt.get();
                UserRole role = currentUser.getRole();
                
                // Step 7: Record successful login audit
                auditDAO.recordAudit(currentUser.getUserId(), "LOGIN_SUCCESS", 
                    "User logged in successfully with role: " + role);
                
                System.out.println("AuthenticationController: Login SUCCESS for " + username + ", role: " + role);
                
                // Step 8: Return success with role
                return new LoginResult(true, role, "Login successful", currentUser);
            }
        }
        
        // Step 10: Record failed login audit and update attempt counter
        auditDAO.recordAudit("UNKNOWN", "LOGIN_FAILED", 
            "Failed login attempt for username: " + username);
        attempt.failures += 1;
        if (attempt.failures >= MAX_ATTEMPTS) {
            attempt.lockoutUntil = now + LOCKOUT_MILLIS;
            attempt.failures = 0;
        }
        attemptTracker.put(key, attempt);
        String message = attempt.lockoutUntil > now
                ? "Too many failed attempts. Please wait before retrying."
                : "Invalid username or password";
        
        System.out.println("AuthenticationController: Login FAILED for " + username);
        
        // Step 11: Return failure
        return new LoginResult(false, null, message, null);
    }

    public void logout() {
        if (currentUser != null) {
            auditDAO.recordAudit(currentUser.getUserId(), "LOGOUT", 
                "User logged out successfully");
            System.out.println("AuthenticationController: User " + currentUser.getUsername() + " logged out");
            currentUser = null;
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    // Helper class for login results
    public static class LoginResult {
        private final boolean success;
        private final UserRole role;
        private final String message;
        private final User user;

        public LoginResult(boolean success, UserRole role, String message, User user) {
            this.success = success;
            this.role = role;
            this.message = message;
            this.user = user;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public UserRole getRole() { return role; }
        public String getMessage() { return message; }
        public User getUser() { return user; }
    }

    private static class LoginAttempt {
        int failures = 0;
        long lockoutUntil = 0L;
    }

    private static int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(System.getProperty(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private static long getLongProperty(String key, long defaultValue) {
        try {
            return Long.parseLong(System.getProperty(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }
}
