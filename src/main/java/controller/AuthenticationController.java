// AuthenticationController.java
package controller;

import database.*;
import model.*;

public class AuthenticationController {
    private UserDAO userDAO;
    private AuditDAO auditDAO;
    private User currentUser;

    public AuthenticationController() {
        this.userDAO = new UserDAO();
        this.auditDAO = new AuditDAO();
    }

    
    public LoginResult login(String username, String password) {
        System.out.println("AuthenticationController: Processing login for " + username);
        
        // Step 2-3: Authenticate user
        boolean authResult = userDAO.authenticate(username, password);
        
        if (authResult) {
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
        
        // Step 10: Record failed login audit
        auditDAO.recordAudit("UNKNOWN", "LOGIN_FAILED", 
            "Failed login attempt for username: " + username);
        
        System.out.println("AuthenticationController: Login FAILED for " + username);
        
        // Step 11: Return failure
        return new LoginResult(false, null, "Invalid username or password", null);
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
}
