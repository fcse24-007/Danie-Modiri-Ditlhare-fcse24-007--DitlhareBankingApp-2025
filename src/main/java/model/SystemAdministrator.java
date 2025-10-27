package model;

import java.util.ArrayList;
import java.util.List;

public class SystemAdministrator extends User {
    private int adminLevel;
    private List<AuditEntry> systemAuditTrail;

    public SystemAdministrator(String userId, String username, String password, int adminLevel) {
        super(userId, username, password, UserRole.ADMINISTRATOR);
        setAdminLevel(adminLevel);
        this.systemAuditTrail = new ArrayList<>();
    }

    @Override
    public boolean authenticate() {
        // Additional security for administrators
        boolean baseAuth = super.authenticate();
        return baseAuth && adminLevel > 0;
    }

    public void manageUserRoles(User user, UserRole newRole) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (newRole == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        
        UserRole oldRole = user.getRole();
        user.setRole(newRole);
        
        recordSystemAudit("USER_ROLE_CHANGED", 
            String.format("User %s role changed from %s to %s by admin %s",
                user.getUserId(), oldRole, newRole, getUserId()));
    }

    public List<AuditEntry> viewAuditTrail() {
        // In a real system, this would fetch from a database
        return new ArrayList<>(systemAuditTrail);
    }

    public List<AuditEntry> viewUserAuditTrail(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        return customer.getAuditTrail();
    }

    public void resetUserPassword(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        user.resetPassword();
        
        recordSystemAudit("PASSWORD_RESET", 
            String.format("Password reset for user %s by admin %s",
                user.getUserId(), getUserId()));
    }

    public void lockUserAccount(User user) {
        if (user instanceof Customer) {
            Customer customer = (Customer) user;
            customer.getAccounts().forEach(account -> 
                account.updateStatus(AccountStatus.SUSPENDED));
            
            recordSystemAudit("STATUS_CHANGED", 
                String.format("User %s account locked by admin %s",
                    user.getUserId(), getUserId()));
        }
    }

    private void recordSystemAudit(String action, String details) {
        AuditEntry auditEntry = new AuditEntry(
            "SYS_AUDIT_" + System.currentTimeMillis(),
            action,
            java.time.LocalDateTime.now(),
            getUserId(),
            details
        );
        systemAuditTrail.add(auditEntry);
    }

    // Getters and Setters
    public int getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(int adminLevel) {
        if (adminLevel < 1 || adminLevel > 3) {
            throw new IllegalArgumentException("Admin level must be between 1 and 3");
        }
        this.adminLevel = adminLevel;
    }

    @Override
    public String toString() {
        return "SystemAdministrator{" +
                "adminLevel=" + adminLevel +
                "} " + super.toString();
    }
}