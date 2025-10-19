package model;

import java.util.List;

public class SystemAdministrator extends User {
    private int adminLevel;

    public SystemAdministrator(String userId, String username, String password, int adminLevel) {
        super(userId, username, password, UserRole.ADMINISTRATOR);
        this.adminLevel = adminLevel;
    }

    @Override
    public boolean authenticate() {
        // Implementation for system administrator authentication
        return super.authenticate();
    }

    public void manageUserRoles(User user) {
        // Implementation for managing user roles
    }

    public List<AuditEntry> viewAuditTrail() {
        // Implementation for viewing audit trail
        return null;
    }

    public void resetUserPassword(User user) {
        // Implementation for resetting user password
    }

    // Getters and Setters
    public int getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(int adminLevel) {
        this.adminLevel = adminLevel;
    }

    @Override
    public String toString() {
        return "SystemAdministrator{" +
                "adminLevel=" + adminLevel +
                "} " + super.toString();
    }
}
