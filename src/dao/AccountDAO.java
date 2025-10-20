package dao;

import model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO implements BaseDAO<Account, String> {

    private final CustomerDAO customerDAO = new CustomerDAO();

    @Override
    public Account findById(String accountNumber) {
        String sql = "SELECT a.*, c.customer_id FROM accounts a " +
                "JOIN customers c ON a.customer_id = c.customer_id " +
                "WHERE a.account_number = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractAccountFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding account by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Account> findAll() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT a.*, c.customer_id FROM accounts a " +
                "JOIN customers c ON a.customer_id = c.customer_id";
        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                accounts.add(extractAccountFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all accounts: " + e.getMessage());
            e.printStackTrace();
        }
        return accounts;
    }

    @Override
    public boolean update(Account account) {
        String sql = "UPDATE accounts SET balance = ?, status = ?, customer_id = ? WHERE account_number = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, account.getBalance());
            stmt.setString(2, account.getStatus().name());
            stmt.setString(3, account.getCustomer().getCustomerId());
            stmt.setString(4, account.getAccountNumber());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating account: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(String accountNumber) {
        String sql = "DELETE FROM accounts WHERE account_number = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountNumber);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting account: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Account save(Account account) {
        String sql = "INSERT INTO accounts (account_number, balance, date_created, date_opened, status, customer_id, account_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, account.getAccountNumber());
            stmt.setDouble(2, account.getBalance());
            stmt.setDate(3, Date.valueOf(account.getDateCreated()));
            stmt.setDate(4, Date.valueOf(account.getDateOpened()));
            stmt.setString(5, account.getStatus().name());
            stmt.setString(6, account.getCustomer().getCustomerId());
            stmt.setString(7, getAccountType(account));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return account;
            }
        } catch (SQLException e) {
            System.err.println("Error saving account: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Find accounts by customer ID
     */
    public List<Account> findAccountsByCustomerId(String customerId) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT a.*, c.customer_id FROM accounts a " +
                "JOIN customers c ON a.customer_id = c.customer_id " +
                "WHERE a.customer_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    accounts.add(extractAccountFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding accounts by customer ID: " + e.getMessage());
            e.printStackTrace();
        }
        return accounts;
    }

    /**
     * Update account balance
     */
    public boolean updateBalance(String accountNumber, double newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, newBalance);
            stmt.setString(2, accountNumber);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating account balance: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Helper to determine the concrete type
    private String getAccountType(Account account) {
        if (account instanceof SavingsAccount) return AccountType.SAVINGS.name();
        if (account instanceof InvestmentAccount) return AccountType.INVESTMENT.name();
        if (account instanceof ChequeAccount) return AccountType.CHEQUE.name();
        return "UNKNOWN";
    }

    private Account extractAccountFromResultSet(ResultSet rs) throws SQLException {
        Customer customer = customerDAO.findById(rs.getString("customer_id"));
        AccountStatus status = AccountStatus.valueOf(rs.getString("status"));

        java.sql.Date sqlDateCreated = rs.getDate("date_created");
        java.sql.Date sqlDateOpened = rs.getDate("date_opened");

        String accountType = rs.getString("account_type");

        switch (AccountType.valueOf(accountType)) {
            case SAVINGS:
                return new SavingsAccount(
                        rs.getString("account_number"),
                        rs.getDouble("balance"),
                        sqlDateCreated.toLocalDate(),
                        sqlDateOpened.toLocalDate(),
                        customer, status
                );
            case INVESTMENT:
                return new InvestmentAccount(
                        rs.getString("account_number"),
                        rs.getDouble("balance"),
                        sqlDateCreated.toLocalDate(),
                        sqlDateOpened.toLocalDate(),
                        customer, status
                );
            case CHEQUE:
                return new ChequeAccount(
                        rs.getString("account_number"),
                        rs.getDouble("balance"),
                        sqlDateCreated.toLocalDate(),
                        sqlDateOpened.toLocalDate(),
                        customer, status,
                        "N/A", "N/A", false
                );
            default:
                return new Account(
                        rs.getString("account_number"),
                        rs.getDouble("balance"),
                        sqlDateCreated.toLocalDate(),
                        sqlDateOpened.toLocalDate(),
                        customer, status
                );
        }
    }
}