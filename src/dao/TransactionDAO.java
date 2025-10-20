package dao;

import model.Account;
import model.Transaction;
import model.TransactionType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO implements BaseDAO<Transaction, String> {

    private final AccountDAO accountDAO = new AccountDAO();

    @Override
    public Transaction findById(String transactionId) {
        String sql = "SELECT t.*, a.account_number, a.balance, a.status, a.customer_id " +
                "FROM transactions t " +
                "JOIN accounts a ON t.account_number = a.account_number " +
                "WHERE t.transaction_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, transactionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractTransactionFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding transaction by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Transaction> findAll() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, a.account_number, a.balance, a.status, a.customer_id " +
                "FROM transactions t " +
                "JOIN accounts a ON t.account_number = a.account_number " +
                "ORDER BY t.time_stamp DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all transactions: " + e.getMessage());
            e.printStackTrace();
        }
        return transactions;
    }

    @Override
    public boolean update(Transaction transaction) {
        String sql = "UPDATE transactions SET type = ?, amount = ?, time_stamp = ?, description = ?, account_number = ? WHERE transaction_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, transaction.getTransactionType().name());
            stmt.setDouble(2, transaction.getAmount());
            stmt.setTimestamp(3, Timestamp.valueOf(transaction.getTimeStamp()));
            stmt.setString(4, transaction.getDescription());
            stmt.setString(5, transaction.getAccount().getAccountNumber());
            stmt.setString(6, transaction.getTransactionId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating transaction: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(String transactionId) {
        String sql = "DELETE FROM transactions WHERE transaction_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, transactionId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Transaction save(Transaction transaction) {
        String sql = "INSERT INTO transactions (transaction_id, type, amount, time_stamp, description, account_number) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, transaction.getTransactionId());
            stmt.setString(2, transaction.getTransactionType().name());
            stmt.setDouble(3, transaction.getAmount());
            stmt.setTimestamp(4, Timestamp.valueOf(transaction.getTimeStamp()));
            stmt.setString(5, transaction.getDescription());
            stmt.setString(6, transaction.getAccount().getAccountNumber());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return transaction;
            }
        } catch (SQLException e) {
            System.err.println("Error saving transaction: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Specialized method to retrieve transactions for a specific account.
     */
    public List<Transaction> findTransactionsByAccount(String accountNumber) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, a.account_number, a.balance, a.status, a.customer_id " +
                "FROM transactions t " +
                "JOIN accounts a ON t.account_number = a.account_number " +
                "WHERE t.account_number = ? ORDER BY t.time_stamp DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(extractTransactionFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding transactions by account: " + e.getMessage());
            e.printStackTrace();
        }
        return transactions;
    }

    /**
     * Find transactions within a date range
     */
    public List<Transaction> findTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, a.account_number, a.balance, a.status, a.customer_id " +
                "FROM transactions t " +
                "JOIN accounts a ON t.account_number = a.account_number " +
                "WHERE t.time_stamp BETWEEN ? AND ? ORDER BY t.time_stamp DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(extractTransactionFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding transactions by date range: " + e.getMessage());
            e.printStackTrace();
        }
        return transactions;
    }

    /**
     * Find transactions by type
     */
    public List<Transaction> findTransactionsByType(TransactionType type) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, a.account_number, a.balance, a.status, a.customer_id " +
                "FROM transactions t " +
                "JOIN accounts a ON t.account_number = a.account_number " +
                "WHERE t.type = ? ORDER BY t.time_stamp DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, type.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(extractTransactionFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding transactions by type: " + e.getMessage());
            e.printStackTrace();
        }
        return transactions;
    }

    // Helper method to map a database row to a Transaction object
    private Transaction extractTransactionFromResultSet(ResultSet rs) throws SQLException {
        // Extract account information
        Account account = accountDAO.findById(rs.getString("account_number"));

        return new Transaction(
                rs.getString("transaction_id"),
                TransactionType.valueOf(rs.getString("type")),
                rs.getDouble("amount"),
                rs.getTimestamp("time_stamp").toLocalDateTime(),
                rs.getString("description"),
                account
        );
    }
}