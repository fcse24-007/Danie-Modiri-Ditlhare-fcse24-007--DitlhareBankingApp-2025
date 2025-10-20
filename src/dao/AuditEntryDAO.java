package dao;

import model.AuditEntry;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditEntryDAO implements BaseDAO<AuditEntry, String> {

    @Override
    public AuditEntry findById(String auditId) {
        String sql = "SELECT * FROM audit_log WHERE audit_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, auditId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractAuditEntryFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding audit entry by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<AuditEntry> findAll() {
        List<AuditEntry> auditEntries = new ArrayList<>();
        String sql = "SELECT * FROM audit_log ORDER BY time_stamp DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                auditEntries.add(extractAuditEntryFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all audit entries: " + e.getMessage());
            e.printStackTrace();
        }
        return auditEntries;
    }

    @Override
    public boolean update(AuditEntry auditEntry) {
        String sql = "UPDATE audit_log SET action = ?, time_stamp = ?, user_id = ?, details = ? WHERE audit_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, auditEntry.getAction());
            stmt.setTimestamp(2, Timestamp.valueOf(auditEntry.getTimeStamp()));
            stmt.setString(3, auditEntry.getUserId());
            stmt.setString(4, auditEntry.getDetails());
            stmt.setString(5, auditEntry.getAuditId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating audit entry: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(String auditId) {
        String sql = "DELETE FROM audit_log WHERE audit_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, auditId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting audit entry: " + e.getMessage());
            e.printStackTrace();
        }
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
            stmt.setTimestamp(3, Timestamp.valueOf(entry.getTimeStamp()));
            stmt.setString(4, entry.getUserId());
            stmt.setString(5, entry.getDetails());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return entry;
            }
        } catch (SQLException e) {
            System.err.println("Error saving audit entry: " + e.getMessage());
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
            System.err.println("Error finding audit trail by user ID: " + e.getMessage());
            e.printStackTrace();
        }
        return entries;
    }

    /**
     * Find audit entries by action type
     */
    public List<AuditEntry> findAuditEntriesByAction(String action) {
        List<AuditEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM audit_log WHERE action = ? ORDER BY time_stamp DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, action);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(extractAuditEntryFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding audit entries by action: " + e.getMessage());
            e.printStackTrace();
        }
        return entries;
    }

    /**
     * Find audit entries within a date range
     */
    public List<AuditEntry> findAuditEntriesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<AuditEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM audit_log WHERE time_stamp BETWEEN ? AND ? ORDER BY time_stamp DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(extractAuditEntryFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding audit entries by date range: " + e.getMessage());
            e.printStackTrace();
        }
        return entries;
    }

    /**
     * Get recent audit entries (last N entries)
     */
    public List<AuditEntry> findRecentAuditEntries(int limit) {
        List<AuditEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM audit_log ORDER BY time_stamp DESC LIMIT ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(extractAuditEntryFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding recent audit entries: " + e.getMessage());
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
}