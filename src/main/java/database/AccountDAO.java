package database;

import model.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountDAO implements DAO<Account> {

    public CustomerDAO customerDAO;

    public AccountDAO() {
        this.customerDAO = new CustomerDAO();
    }

    public AccountDAO(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }
    
    @Override
    public Optional<Account> findById(String accountNumber) {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToAccount(rs, conn));
            }
        } catch (SQLException e) {
            System.err.println("Error finding account by number: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Account> findByCustomerId(String customerId) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE customer_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs, conn));
            }
        } catch (SQLException e) {
            System.err.println("Error finding accounts by customer ID: " + e.getMessage());
            e.printStackTrace();
        }
        return accounts;
    }

    public List<Account> findByStatus(AccountStatus status) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE status = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.toString());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs, conn));
            }
        } catch (SQLException e) {
            System.err.println("Error finding accounts by status: " + e.getMessage());
            e.printStackTrace();
        }
        return accounts;
    }

    public List<Account> findByType(String accountType) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE account_type = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, accountType);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs, conn));
            }
        } catch (SQLException e) {
            System.err.println("Error finding accounts by type: " + e.getMessage());
            e.printStackTrace();
        }
        return accounts;
    }

    @Override
    public List<Account> findAll() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                try {
                    Account account = mapResultSetToAccount(rs, conn);
                    accounts.add(account);
                } catch (SQLException e) {
                    System.err.println("Error mapping account from result set: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding all accounts: " + e.getMessage());
            e.printStackTrace();
        }
        return accounts;
    }

    @Override
    public boolean save(Account account) {
        String sql = """
            INSERT INTO accounts (account_number, balance, date_created, date_opened, 
                                 customer_id, status, account_type, interest_rate, 
                                 employer_name, employer_address, employment_status) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setAccountParameters(stmt, account);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving account: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Account account) {
        String sql = """
            UPDATE accounts SET balance = ?, date_created = ?, date_opened = ?,
                               customer_id = ?, status = ?, account_type = ?, interest_rate = ?,
                               employer_name = ?, employer_address = ?, employment_status = ?
            WHERE account_number = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set parameters in the correct order for UPDATE
            stmt.setDouble(1, account.getBalance());
            stmt.setDate(2, Date.valueOf(account.getDateCreated()));
            stmt.setDate(3, Date.valueOf(account.getDateOpened()));
            stmt.setString(4, account.getCustomer().getCustomerId());
            stmt.setString(5, account.getStatus().toString());

            // Determine account type and set specific fields
            if (account instanceof SavingsAccount) {
                SavingsAccount sa = (SavingsAccount) account;
                stmt.setString(6, "SAVINGS");
                stmt.setDouble(7, sa.getInterestRate());
                stmt.setNull(8, Types.VARCHAR);
                stmt.setNull(9, Types.VARCHAR);
                stmt.setNull(10, Types.BOOLEAN);

            } else if (account instanceof InvestmentAccount) {
                InvestmentAccount ia = (InvestmentAccount) account;
                stmt.setString(6, "INVESTMENT");
                stmt.setDouble(7, ia.getInterestRate());
                stmt.setNull(8, Types.VARCHAR);
                stmt.setNull(9, Types.VARCHAR);
                stmt.setNull(10, Types.BOOLEAN);

            } else if (account instanceof ChequeAccount) {
                ChequeAccount ca = (ChequeAccount) account;
                stmt.setString(6, "CHEQUE");
                stmt.setNull(7, Types.DOUBLE);
                stmt.setString(8, ca.getEmployerName());
                stmt.setString(9, ca.getEmployerAddress());
                stmt.setBoolean(10, ca.isEmploymentStatus());

            } else {
                stmt.setString(6, "SAVINGS"); // Default
                stmt.setNull(7, Types.DOUBLE);
                stmt.setNull(8, Types.VARCHAR);
                stmt.setNull(9, Types.VARCHAR);
                stmt.setNull(10, Types.BOOLEAN);
            }

            stmt.setString(11, account.getAccountNumber());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating account: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBalance(String accountNumber, double newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, newBalance);
            stmt.setString(2, accountNumber);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating account balance: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(String accountNumber) {
        String sql = "DELETE FROM accounts WHERE account_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, accountNumber);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting account: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Account mapResultSetToAccount(ResultSet rs, Connection conn) throws SQLException {
        String accountType = rs.getString("account_type");
        String accountNumber = rs.getString("account_number");
        double balance = rs.getDouble("balance");
        
        // Handle potential null dates
        LocalDate dateCreated = rs.getDate("date_created") != null ? 
            rs.getDate("date_created").toLocalDate() : LocalDate.now();
        LocalDate dateOpened = rs.getDate("date_opened") != null ? 
            rs.getDate("date_opened").toLocalDate() : LocalDate.now();
            
        String customerId = rs.getString("customer_id");
        AccountStatus status = AccountStatus.valueOf(rs.getString("status"));
        
        // Create a minimal customer object using the existing connection
        Customer minimalCustomer = createMinimalCustomer(customerId, conn);
        
        switch (accountType) {
            case "SAVINGS":
                SavingsAccount savingsAccount = new SavingsAccount(
                    accountNumber, balance, dateCreated, dateOpened, 
                    minimalCustomer, status
                );
                savingsAccount.setInterestRate(rs.getDouble("interest_rate"));
                return savingsAccount;
                
            case "INVESTMENT":
                InvestmentAccount investmentAccount = new InvestmentAccount(
                    accountNumber, balance, dateCreated, dateOpened, 
                    minimalCustomer, status
                );
                investmentAccount.setInterestRate(rs.getDouble("interest_rate"));
                return investmentAccount;
                
            case "CHEQUE":
                ChequeAccount chequeAccount = new ChequeAccount(
                    accountNumber, balance, dateCreated, dateOpened, 
                    minimalCustomer, status,
                    rs.getString("employer_name"),
                    rs.getString("employer_address"),
                    rs.getBoolean("employment_status")
                );
                return chequeAccount;
                
            default:
                return new Account(
                    accountNumber, balance, dateCreated, dateOpened, 
                    minimalCustomer, status
                );
        }
    }

    
    private Customer createMinimalCustomer(String customerId, Connection conn) {
        // Instead of creating a temp customer, try to load the real one using existing connection
        try {
            String sql = "SELECT * FROM customers WHERE customer_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, customerId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new Customer(
                            rs.getString("user_id"),
                            "temp_username", // We don't need the actual username for display
                            "TempPass@123", // Valid password that passes validation
                            rs.getString("customer_id"),
                            rs.getString("first_name"),
                            rs.getString("surname"),
                            rs.getString("address"),
                            rs.getString("phone_number"),
                            rs.getString("email"),
                            CustomerType.valueOf(rs.getString("customer_type"))
                        );
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading customer for account: " + e.getMessage());
        }
        return createFallbackCustomer(customerId);
    }
    
    private Customer createMinimalCustomer(String customerId) {
        // Fallback method for backward compatibility
        return createFallbackCustomer(customerId);
    }

    private Customer createFallbackCustomer(String customerId) {
        return new Customer(
            "temp_user_id", "temp_username", "TempPass@123",
            customerId, "Unknown", "Customer", "Unknown Address",
            "000-0000", "unknown@email.com", CustomerType.INDIVIDUAL
        );
    }

    private void setAccountParameters(PreparedStatement stmt, Account account) throws SQLException {
        stmt.setString(1, account.getAccountNumber());
        stmt.setDouble(2, account.getBalance());
        stmt.setDate(3, Date.valueOf(account.getDateCreated()));
        stmt.setDate(4, Date.valueOf(account.getDateOpened()));
        stmt.setString(5, account.getCustomer().getCustomerId());
        stmt.setString(6, account.getStatus().toString());
        
        // Determine account type and set specific fields
        if (account instanceof SavingsAccount) {
            SavingsAccount sa = (SavingsAccount) account;
            stmt.setString(7, "SAVINGS");
            stmt.setDouble(8, sa.getInterestRate());
            stmt.setNull(9, Types.VARCHAR);
            stmt.setNull(10, Types.VARCHAR);
            stmt.setNull(11, Types.BOOLEAN);
            
        } else if (account instanceof InvestmentAccount) {
            InvestmentAccount ia = (InvestmentAccount) account;
            stmt.setString(7, "INVESTMENT");
            stmt.setDouble(8, ia.getInterestRate());
            stmt.setNull(9, Types.VARCHAR);
            stmt.setNull(10, Types.VARCHAR);
            stmt.setNull(11, Types.BOOLEAN);
            
        } else if (account instanceof ChequeAccount) {
            ChequeAccount ca = (ChequeAccount) account;
            stmt.setString(7, "CHEQUE");
            stmt.setNull(8, Types.DOUBLE);
            stmt.setString(9, ca.getEmployerName());
            stmt.setString(10, ca.getEmployerAddress());
            stmt.setBoolean(11, ca.isEmploymentStatus());
            
        } else {
            stmt.setString(7, "SAVINGS"); // Default
            stmt.setNull(8, Types.DOUBLE);
            stmt.setNull(9, Types.VARCHAR);
            stmt.setNull(10, Types.VARCHAR);
            stmt.setNull(11, Types.BOOLEAN);
        }
    }
}
