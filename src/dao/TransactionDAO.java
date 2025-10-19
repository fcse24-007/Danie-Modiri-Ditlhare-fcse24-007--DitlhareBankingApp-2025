package dao;

import model.Account;
import model.Transaction;
import model.TransactionType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO implements BaseDAO<Transaction, String> {

    // Assumes AccountDAO exists to fetch the associated account object
    private final AccountDAO accountDAO = new AccountDAO();

    @Override
    public Transaction findById(String transactionId) {
        // Implementation omitted for brevity
        return null;
    }

    @Override
    public List<Transaction> findAll() {
        return List.of();
    }

    @Override
    public boolean update(Transaction entity) {
        return false;
    }

    @Override
    public boolean delete(String s) {
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
            // Map LocalDateTime to SQL TIMESTAMP
            stmt.setTimestamp(4, Timestamp.valueOf(transaction.getTimeStamp()));
            stmt.setString(5, transaction.getDescription());
            stmt.setString(6, transaction.getAccount().getAccountNumber());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return transaction;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Specialized method to retrieve transactions for a specific account.
     */
    public List<Transaction> findTransactionsByAccount(String accountNumber) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_number = ? ORDER BY time_stamp DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                Account account = accountDAO.findById(accountNumber); // Fetch account once
                while (rs.next()) {
                    transactions.add(extractTransactionFromResultSet(rs, account));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    // Helper method to map a database row to a Transaction object
    private Transaction extractTransactionFromResultSet(ResultSet rs, Account account) throws SQLException {
        return new Transaction(
                rs.getString("transaction_id"),
                TransactionType.valueOf(rs.getString("type")),
                rs.getDouble("amount"),
                rs.getTimestamp("time_stamp").toLocalDateTime(), // Map SQL TIMESTAMP to LocalDateTime
                rs.getString("description"),
                account
        );
    }

    // Omitted implementations for update(), delete(), and findAll()
}