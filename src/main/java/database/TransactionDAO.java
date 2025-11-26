package database;

import model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionDAO implements DAO<Transaction> {

    @Override
    public Optional<Transaction> findById(String transactionId) {
        String sql = "SELECT * FROM transactions WHERE transaction_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, transactionId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToTransaction(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding transaction by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Transaction> findByAccountNumber(String accountNumber) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_number = ? ORDER BY timestamp DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding transactions by account: " + e.getMessage());
            e.printStackTrace();
        }
        return transactions;
    }

    public List<Transaction> findByCustomerId(String customerId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = """
            SELECT t.* FROM transactions t 
            JOIN accounts a ON t.account_number = a.account_number 
            WHERE a.customer_id = ? 
            ORDER BY t.timestamp DESC
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding transactions by customer: " + e.getMessage());
            e.printStackTrace();
        }
        return transactions;
    }

    @Override
    public List<Transaction> findAll() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY timestamp DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all transactions: " + e.getMessage());
            e.printStackTrace();
        }
        return transactions;
    }

    @Override
    public boolean save(Transaction transaction) {
        String sql = """
            INSERT INTO transactions (transaction_id, transaction_type, amount, 
                                     timestamp, description, account_number) 
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, transaction.getTransactionId());
            stmt.setString(2, transaction.getTransactionType().toString());
            stmt.setDouble(3, transaction.getAmount());
            stmt.setTimestamp(4, Timestamp.valueOf(transaction.getTimeStamp()));
            stmt.setString(5, transaction.getDescription());
            stmt.setString(6, transaction.getAccount().getAccountNumber());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving transaction: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Transaction transaction) {
        String sql = """
            UPDATE transactions SET transaction_type = ?, amount = ?, timestamp = ?, 
                                  description = ?, account_number = ? 
            WHERE transaction_id = ?
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, transaction.getTransactionType().toString());
            stmt.setDouble(2, transaction.getAmount());
            stmt.setTimestamp(3, Timestamp.valueOf(transaction.getTimeStamp()));
            stmt.setString(4, transaction.getDescription());
            stmt.setString(5, transaction.getAccount().getAccountNumber());
            stmt.setString(6, transaction.getTransactionId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating transaction: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(String transactionId) {
        String sql = "DELETE FROM transactions WHERE transaction_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, transactionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        String transactionId = rs.getString("transaction_id");
        TransactionType type = TransactionType.valueOf(rs.getString("transaction_type"));
        double amount = rs.getDouble("amount");
        LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
        String description = rs.getString("description");
        String accountNumber = rs.getString("account_number");
        
        // Create a minimal account object to avoid circular dependencies
        Account minimalAccount = createMinimalAccount(accountNumber);
        
        return new Transaction(transactionId, type, amount, timestamp, description, minimalAccount);
    }

    private Account createMinimalAccount(String accountNumber) {
        // Create a minimal account with just the account number
        // Full account details can be loaded separately when needed
        return new Account(
            accountNumber, 
            0.0, 
            java.time.LocalDate.now(), 
            java.time.LocalDate.now(), 
            null,  // Customer will be null to avoid circular dependency
            AccountStatus.ACTIVE
        );
    }
}
