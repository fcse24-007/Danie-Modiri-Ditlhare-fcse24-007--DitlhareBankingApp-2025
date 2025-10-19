package dao;

import model.Action;
import model.AuditEntry;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditEntryDAO implements BaseDAO<AuditEntry, String> {

    @Override
    public AuditEntry findById(String auditId) {
        // Implementation omitted for brevity
        return null;
    }

    @Override
    public List<AuditEntry> findAll() {
        return List.of();
    }

    @Override
    public boolean update(AuditEntry entity) {
        return false;
    }

    @Override
    public boolean delete(String s) {
        return false;
    }

    @Override
    public AuditEntry save(AuditEntry entry) {
        String sql = "INSERT INTO audit_log (audit_id, action, time_stamp, user_id, details) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entry.getAuditId());
            stmt.setString(2, entry.getAction());
            // Map LocalDateTime to SQL TIMESTAMP
            stmt.setTimestamp(3, Timestamp.valueOf(entry.getTimeStamp()));
            stmt.setString(4, entry.getUserId());
            stmt.setString(5, entry.getDetails());

            stmt.executeUpdate();
            // Audit logs are typically never updated or deleted, so we just return the entry
            return entry;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Specialized method to retrieve the audit trail for a user.
     */
    public List<AuditEntry> findAuditTrailByUserId(String userId) {
        List<AuditEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM audit_log WHERE user_id = ? ORDER BY time_stamp DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(extractAuditEntryFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

    // Helper method to map a database row to an AuditEntry object
    private AuditEntry extractAuditEntryFromResultSet(ResultSet rs) throws SQLException {
        return new AuditEntry(
                rs.getString("audit_id"),
                rs.getString("action"),
                rs.getTimestamp("time_stamp").toLocalDateTime(),
                rs.getString("user_id"),
                rs.getString("details")
        );
    }

    // Omitted implementations for update(), delete(), and findAll()
}