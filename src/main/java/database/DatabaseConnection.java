package database;

import util.Passwords;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DatabaseConnection {

    private static final String DEFAULT_DB_DIR = "data";
    private static final String DEFAULT_DB_FILE = "banking_system.db";

    private static final String DB_FILE_PATH = determineDbPath();

    // JDBC URL for SQLite
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_FILE_PATH;

    // Singleton connection 
    private static Connection connection;

    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    private DatabaseConnection() { }

    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(JDBC_URL);
                enablePragmas(connection);
                // Trigger initialization once
                if (initialized.compareAndSet(false, true)) {
                    initializeDatabaseInternal();
                }
            } else {
                // ensure pragmas are set on reused connection as well
                enablePragmas(connection);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database: " + JDBC_URL, e);
        }
        return connection;
    }

    /**
     * Public trigger for initialization
     */
    public static void initializeDatabase() {
        getConnection();
    }

    private static void initializeDatabaseInternal() {
        System.out.println("Initializing database (internal)...");
        try (Connection conn = DriverManager.getConnection(JDBC_URL)) {
            enablePragmas(conn);
            conn.setAutoCommit(false);
            try {
                createTables(conn);
                seedInitialData(conn);
                conn.commit();
                System.out.println("Database initialized successfully!");
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                // restore auto-commit (safe default)
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    /**
     * Create schema if missing.
     */
    private static void createTables(Connection conn) throws SQLException {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                user_id TEXT PRIMARY KEY,
                username TEXT UNIQUE NOT NULL COLLATE NOCASE,
                password TEXT NOT NULL,
                role TEXT NOT NULL CHECK(role IN ('CUSTOMER', 'BANK_EMPLOYEE', 'ADMINISTRATOR')),
                created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;

        String createCustomersTable = """
            CREATE TABLE IF NOT EXISTS customers (
                customer_id TEXT PRIMARY KEY,
                user_id TEXT NOT NULL,
                first_name TEXT NOT NULL,
                surname TEXT NOT NULL,
                address TEXT NOT NULL,
                phone_number TEXT NOT NULL,
                email TEXT NOT NULL,
                customer_type TEXT NOT NULL CHECK(customer_type IN ('INDIVIDUAL', 'JOINT', 'BUSINESS')),
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            )
            """;

        String createAccountsTable = """
            CREATE TABLE IF NOT EXISTS accounts (
                account_number TEXT PRIMARY KEY,
                balance REAL NOT NULL DEFAULT 0.0,
                date_created DATE NOT NULL,
                date_opened DATE NOT NULL,
                customer_id TEXT NOT NULL,
                status TEXT NOT NULL CHECK(status IN ('ACTIVE', 'INACTIVE', 'CLOSED', 'SUSPENDED')),
                account_type TEXT NOT NULL CHECK(account_type IN ('SAVINGS', 'CHEQUE', 'INVESTMENT')),
                interest_rate REAL DEFAULT 0.0,
                employer_name TEXT,
                employer_address TEXT,
                employment_status BOOLEAN,
                FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
            )
            """;

        String createTransactionsTable = """
            CREATE TABLE IF NOT EXISTS transactions (
                transaction_id TEXT PRIMARY KEY,
                transaction_type TEXT NOT NULL CHECK(transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_INTERNAL', 'TRANSFER_EXTERNAL', 'INTEREST_PAYMENT')),
                amount REAL NOT NULL,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                description TEXT,
                account_number TEXT NOT NULL,
                FOREIGN KEY (account_number) REFERENCES accounts(account_number) ON DELETE CASCADE
            )
            """;

        String createAuditTable = """
            CREATE TABLE IF NOT EXISTS audit_trail (
                audit_id TEXT PRIMARY KEY,
                action TEXT NOT NULL,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                user_id TEXT NOT NULL,
                details TEXT,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            )
            """;

        String createEmployeesTable = """
            CREATE TABLE IF NOT EXISTS bank_employees (
                employee_id TEXT PRIMARY KEY,
                user_id TEXT NOT NULL,
                department TEXT NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            )
            """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createCustomersTable);
            stmt.execute(createAccountsTable);
            stmt.execute(createTransactionsTable);
            stmt.execute(createAuditTable);
            stmt.execute(createEmployeesTable);
        }

        System.out.println("All database tables verified/created.");
    }

    /**
     * Insert initial data if missing.
     */
    private static void seedInitialData(Connection conn) throws SQLException {
        // Check if admin already exists
        String checkAdminSql = "SELECT COUNT(*) FROM users WHERE role = 'ADMINISTRATOR'";
        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(checkAdminSql)) {
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Admin user already exists; checking for sample data...");
                // Check if sample accounts exist
                String checkAccountsSql = "SELECT COUNT(*) FROM accounts";
                try (Statement stmt2 = conn.createStatement();
                     ResultSet rs2 = stmt2.executeQuery(checkAccountsSql)) {
                    if (rs2.next() && rs2.getInt(1) == 0) {
                        System.out.println("No accounts found, creating sample data...");
                        createSampleData(conn);
                    } else {
                        System.out.println("Sample data already exists.");
                    }
                }
                return;
            }
        }

        String insertAdminSql = """
            INSERT INTO users (user_id, username, password, role)
            VALUES (?, ?, ?, ?)
            """;

        String defaultAdminPassword = System.getenv().getOrDefault("DEFAULT_ADMIN_PASSWORD", "Admin123!");
        String hashedAdminPass = Passwords.hashPassword(defaultAdminPassword);

        try (PreparedStatement pstmt = conn.prepareStatement(insertAdminSql)) {
            pstmt.setString(1, "ADM-001"); // Auto-generated format
            pstmt.setString(2, "admin");
            pstmt.setString(3, hashedAdminPass);
            pstmt.setString(4, "ADMINISTRATOR");
            
            int rowCount = pstmt.executeUpdate();
            if (rowCount > 0) {
                System.out.println("Default admin user created (user_id=ADM-001).");
            }
        }
        
        // Create sample data
        createSampleData(conn);
    }
    
    /**
     * Create sample customers and accounts for testing
     */
    private static void createSampleData(Connection conn) throws SQLException {
        System.out.println("Creating sample data...");
        
        // Create sample users and customers
        String[] sampleUsers = {
            "INSERT INTO users (user_id, username, password, role) VALUES ('USR-001', 'john_doe', ?, 'CUSTOMER')",
            "INSERT INTO users (user_id, username, password, role) VALUES ('USR-002', 'jane_smith', ?, 'CUSTOMER')",
            "INSERT INTO users (user_id, username, password, role) VALUES ('EMP-001', 'bank_emp', ?, 'BANK_EMPLOYEE')"
        };
        
        String[] sampleCustomers = {
            "INSERT INTO customers (customer_id, user_id, first_name, surname, address, phone_number, email, customer_type) VALUES ('CUST-001', 'USR-001', 'John', 'Doe', '123 Main St', '555-0123', 'john@email.com', 'INDIVIDUAL')",
            "INSERT INTO customers (customer_id, user_id, first_name, surname, address, phone_number, email, customer_type) VALUES ('CUST-002', 'USR-002', 'Jane', 'Smith', '456 Oak Ave', '555-0456', 'jane@email.com', 'INDIVIDUAL')"
        };
        
        String[] sampleAccounts = {
            "INSERT INTO accounts (account_number, balance, date_created, date_opened, customer_id, status, account_type, interest_rate) VALUES ('ACC-001', 1500.00, '2024-01-15', '2024-01-15', 'CUST-001', 'ACTIVE', 'SAVINGS', 2.5)",
            "INSERT INTO accounts (account_number, balance, date_created, date_opened, customer_id, status, account_type, interest_rate) VALUES ('ACC-002', 2500.00, '2024-02-20', '2024-02-20', 'CUST-001', 'ACTIVE', 'INVESTMENT', 4.0)",
            "INSERT INTO accounts (account_number, balance, date_created, date_opened, customer_id, status, account_type, employer_name, employer_address, employment_status) VALUES ('ACC-003', 800.00, '2024-03-10', '2024-03-10', 'CUST-002', 'ACTIVE', 'CHEQUE', 'Tech Corp', '789 Business Blvd', 1)",
            "INSERT INTO accounts (account_number, balance, date_created, date_opened, customer_id, status, account_type, interest_rate) VALUES ('ACC-004', 3200.00, '2024-04-05', '2024-04-05', 'CUST-002', 'ACTIVE', 'SAVINGS', 3.0)"
        };
        
        String hashedPassword = Passwords.hashPassword("password123");
        
        try (Statement stmt = conn.createStatement()) {
            // Insert sample users
            for (String sql : sampleUsers) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, hashedPassword);
                    pstmt.executeUpdate();
                }
            }
            
            // Insert sample customers
            for (String sql : sampleCustomers) {
                stmt.execute(sql);
            }
            
            // Insert sample accounts
            for (String sql : sampleAccounts) {
                stmt.execute(sql);
            }
            
            System.out.println("Sample data created successfully!");
            System.out.println("- 3 users (2 customers, 1 employee)");
            System.out.println("- 2 customers");
            System.out.println("- 4 accounts");
        }
    }

    private static void enablePragmas(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // enforces foreign key constraints on SQLite
            stmt.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            System.err.println("Warning: failed to set PRAGMA: " + e.getMessage());
        }
    }

    /**
     * Ensures the data directory exists and return the full DB file path.
     */
    private static String determineDbPath() {
        String env = System.getenv("DB_FILE");
        if (env != null && !env.isBlank()) {
            return env;
        }

        Path dataDir = Path.of(System.getProperty("user.dir")).resolve(DEFAULT_DB_DIR);
        try {
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
        } catch (Exception e) {
            // fallback to project root DB file if data dir could not be created
            System.err.println("Warning: failed to create data directory: " + e.getMessage());
            return Path.of(System.getProperty("user.dir")).resolve(DEFAULT_DB_FILE).toString();
        }
        return dataDir.resolve(DEFAULT_DB_FILE).toString();
    }

    /**
     * Close singleton connection.
     */
    public static synchronized void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }

    /**
     * Get the database file path (for backup purposes)
     */
    public static String getDbFilePath() {
        return DB_FILE_PATH;
    }
}
