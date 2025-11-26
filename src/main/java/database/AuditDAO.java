package database;

import model.AuditEntry;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuditDAO implements DAO<AuditEntry> {

    @Override
    public Optional<AuditEntry> findById(String auditId) {
        String sql = "SELECT * FROM audit_trail WHERE audit_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, auditId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToAuditEntry(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding audit entry by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<AuditEntry> findByUserId(String userId) {
        List<AuditEntry> auditEntries = new ArrayList<>();
        String sql = "SELECT * FROM audit_trail WHERE user_id = ? ORDER BY timestamp DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                auditEntries.add(mapResultSetToAuditEntry(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding audit entries by user: " + e.getMessage());
        }
        return auditEntries;
    }

    public List<AuditEntry> findByAction(String action) {
        List<AuditEntry> auditEntries = new ArrayList<>();
        String sql = "SELECT * FROM audit_trail WHERE action = ? ORDER BY timestamp DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, action);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                auditEntries.add(mapResultSetToAuditEntry(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding audit entries by action: " + e.getMessage());
        }
        return auditEntries;
    }

    @Override
    public List<AuditEntry> findAll() {
        List<AuditEntry> auditEntries = new ArrayList<>();
        String sql = "SELECT * FROM audit_trail ORDER BY timestamp DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                auditEntries.add(mapResultSetToAuditEntry(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all audit entries: " + e.getMessage());
        }
        return auditEntries;
    }

    @Override
    public boolean save(AuditEntry auditEntry) {
        String sql = """
            INSERT INTO audit_trail (audit_id, action, timestamp, user_id, details) 
            VALUES (?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, auditEntry.getAuditId());
            stmt.setString(2, auditEntry.getAction());
            stmt.setTimestamp(3, Timestamp.valueOf(auditEntry.getTimeStamp()));
            stmt.setString(4, auditEntry.getUserId());
            stmt.setString(5, auditEntry.getDetails());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving audit entry: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(AuditEntry auditEntry) {
        String sql = """
            UPDATE audit_trail SET action = ?, timestamp = ?, user_id = ?, details = ? 
            WHERE audit_id = ?
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, auditEntry.getAction());
            stmt.setTimestamp(2, Timestamp.valueOf(auditEntry.getTimeStamp()));
            stmt.setString(3, auditEntry.getUserId());
            stmt.setString(4, auditEntry.getDetails());
            stmt.setString(5, auditEntry.getAuditId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating audit entry: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String auditId) {
        String sql = "DELETE FROM audit_trail WHERE audit_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, auditId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting audit entry: " + e.getMessage());
            return false;
        }
    }

    // In AuditDAOImpl.java - Update the recordAudit method
    public void recordAudit(String userId, String action, String details) {
        // Validate that the user exists before recording audit
        UserDAO userDAO = new UserDAO();
        var userOpt = userDAO.findById(userId);
        
        // If user doesn't exist (and not a valid system ID), don't record audit
        // to avoid foreign key constraint violations
        if (userOpt.isEmpty()) {
            System.err.println("Warning: Audit record not saved - user not found: " + userId);
            return;
        }
        
        String auditId = "AUDIT_" + System.currentTimeMillis() + "_" + userId;
        AuditEntry auditEntry = new AuditEntry(
            auditId, action, LocalDateTime.now(), userId, details
        );
        save(auditEntry);
    }

    /**
     * Delete audit entries older than the specified number of days
     * @param daysOlderThan Number of days - entries older than this will be deleted
     * @return Number of entries deleted
     */
    public int deleteOldEntries(int daysOlderThan) {
        String sql = "DELETE FROM audit_trail WHERE timestamp < datetime('now', '-" + daysOlderThan + " days')";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            int deletedCount = stmt.executeUpdate(sql);
            System.out.println("Deleted " + deletedCount + " audit entries older than " + daysOlderThan + " days");
            return deletedCount;
        } catch (SQLException e) {
            System.err.println("Error deleting old audit entries: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    private AuditEntry mapResultSetToAuditEntry(ResultSet rs) throws SQLException {
        return new AuditEntry(
            rs.getString("audit_id"),
            rs.getString("action"),
            rs.getTimestamp("timestamp").toLocalDateTime(),
            rs.getString("user_id"),
            rs.getString("details")
        );
    }
}
