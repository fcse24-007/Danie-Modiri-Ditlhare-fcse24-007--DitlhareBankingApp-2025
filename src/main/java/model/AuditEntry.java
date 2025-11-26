package model;

import java.time.LocalDateTime;
import java.util.List;

public class AuditEntry {
    private String auditId;
    private String action;
    private LocalDateTime timeStamp;
    private String userId;
    private String details;

    public AuditEntry(String auditId, String action, LocalDateTime timeStamp, String userId, String details) {
        this.auditId = auditId;
        this.action = action;
        this.timeStamp = timeStamp;
        this.userId = userId;
        this.details = details;
    }

    public void recordAudit() {
    }

    public List<AuditEntry> viewAuditTrail() {
        return null;
    }

    // Getters and Setters
    public String getAuditId() {
        return auditId;
    }

    public void setAuditId(String auditId) {
        this.auditId = auditId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "AuditEntry{" +
                "action='" + action + '\'' +
                ", auditId='" + auditId + '\'' +
                ", timeStamp=" + timeStamp +
                ", userId='" + userId + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}
