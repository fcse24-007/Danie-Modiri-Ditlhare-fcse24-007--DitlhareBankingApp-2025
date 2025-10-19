package dao;

import model.*;
import dao.DBConnection;

import java.sql.*;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO implements BaseDAO<Account, String> {

    // Assumes you have a way to fetch a Customer object given a customer_id
    private final CustomerDAO customerDAO = new CustomerDAO();

    @Override
    public Account findById(String accountNumber) {
        String sql = "SELECT a.*, c.customer_id FROM accounts a " +
                "JOIN customers c ON a.customer_id = c.customer_id " + // Join to get Customer ID
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
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Account> findAll() {
        return List.of();
    }

    @Override
    public boolean update(Account entity) {
        return false;
    }

    @Override
    public boolean delete(String s) {
        return false;
    }

    // --- Core Implementations ---

    @Override
    public Account save(Account account) {
        // Simplified SQL for the base Account table. Subtype fields (like interestRate)
        // would require separate tables/updates.
        String sql = "INSERT INTO accounts (account_number, balance, date_created, date_opened, status, customer_id, account_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, account.getAccountNumber());
            stmt.setDouble(2, account.getBalance());
            // Map LocalDate to SQL Date
            stmt.setDate(3, Date.valueOf(account.getDateCreated()));
            stmt.setDate(4, Date.valueOf(account.getDateOpened()));
            stmt.setString(5, account.getStatus().name());
            stmt.setString(6, account.getCustomer().getCustomerId());

            // Determine the AccountType to save in the DB
            String type = getAccountType(account);
            stmt.setString(7, type);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                // You would need to call a subtype DAO (e.g., SavingsAccountDAO) to save
                // any specific fields (like InterestRate) here.
                return account;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Helper to determine the concrete type
    private String getAccountType(Account account) {
        if (account instanceof SavingsAccount) return AccountType.SAVINGS.name();
        if (account instanceof InvestmentAccount) return AccountType.INVESTMENT.name();
        if (account instanceof ChequeAccount) return AccountType.CHEQUE.name();
        return "UNKNOWN";
    }

    // Helper method to map a database row to the correct Account object
    private Account extractAccountFromResultSet(ResultSet rs) throws SQLException {
        // Fetch the associated Customer (requires CustomerDAO)
        Customer customer = customerDAO.findById(rs.getString("customer_id"));

        AccountStatus status = AccountStatus.valueOf(rs.getString("status"));

        // Convert SQL Date to Java LocalDate
        java.sql.Date sqlDateCreated = rs.getDate("date_created");
        java.sql.Date sqlDateOpened = rs.getDate("date_opened");

        String accountType = rs.getString("account_type");

        // Logic to instantiate the correct subtype based on the stored type
        // In a complete system, you would fetch subtype-specific fields here.
        switch (AccountType.valueOf(accountType)) {
            case SAVINGS:
                return new SavingsAccount(
                        rs.getString("account_number"),
                        rs.getDouble("balance"),
                        sqlDateCreated.toLocalDate(),
                        sqlDateOpened.toLocalDate(),
                        customer, status
                        // + fetch interestRate from a SAVINGS_ACCOUNT table
                );
            case INVESTMENT:
                return new InvestmentAccount(
                        rs.getString("account_number"),
                        rs.getDouble("balance"),
                        sqlDateCreated.toLocalDate(),
                        sqlDateOpened.toLocalDate(),
                        customer, status
                        // + fetch interestRate from an INVESTMENT_ACCOUNT table
                );
            case CHEQUE:
                return new ChequeAccount(
                        rs.getString("account_number"),
                        rs.getDouble("balance"),
                        sqlDateCreated.toLocalDate(),
                        sqlDateOpened.toLocalDate(),
                        customer, status,
                        "N/A", "N/A", false // + fetch employer details from a CHEQUE_ACCOUNT table
                );
            default:
                // Return the base Account object if no subtype matches
                return new Account(
                        rs.getString("account_number"),
                        rs.getDouble("balance"),
                        sqlDateCreated.toLocalDate(),
                        sqlDateOpened.toLocalDate(),
                        customer, status
                );
        }
    }

    // ... findAll, update, delete methods follow the same pattern ...
}
