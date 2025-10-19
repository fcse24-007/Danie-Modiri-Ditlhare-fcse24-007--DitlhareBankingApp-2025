package model;

public interface Auditable {
    void recordAudit(Action action, String details);
}
