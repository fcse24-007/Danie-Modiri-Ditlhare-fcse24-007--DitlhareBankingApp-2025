package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.InputStream;
import java.util.Properties;

public class DBConnection {

    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static DBConnection instance = null;

    // Environment-based configuration
    private String url;
    private String username;
    private String password;

    private DBConnection() {
        loadConfiguration();
        try {
            Class.forName(DRIVER_CLASS);
            System.out.println("MySQL JDBC Driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found.");
            e.printStackTrace();
        }
    }

    private void loadConfiguration() {
        try {
            // Try to load from environment variables first (Codespaces/Production)
            this.url = System.getenv("DB_URL");
            this.username = System.getenv("DB_USERNAME");
            this.password = System.getenv("DB_PASSWORD");

            // If environment variables not set, use local defaults
            if (this.url == null) {
                this.url = "jdbc:mysql://localhost:3306/bankdb";
            }
            if (this.username == null) {
                this.username = "root";
            }
            if (this.password == null) {
                this.password = "";
            }

            System.out.println("✓ Database configuration loaded:");
            System.out.println("  URL: " + this.url);
            System.out.println("  User: " + this.username);

        } catch (Exception e) {
            System.err.println("✗ Error loading database configuration, using defaults");
            this.url = "jdbc:mysql://localhost:3306/bankdb";
            this.username = "root";
            this.password = "";
        }
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            // Use environment variables with fallbacks
            String url = System.getenv("DB_URL");
            String username = System.getenv("DB_USERNAME");
            String password = System.getenv("DB_PASSWORD");

            if (url == null) url = "jdbc:mysql://localhost:3306/bankdb";
            if (username == null) username = "root";
            if (password == null) password = "";

            Connection connection = DriverManager.getConnection(url, username, password);
            System.out.println("✓ Database connection established to: " + url);
            return connection;
        } catch (SQLException e) {
            System.err.println("✗ Error creating database connection");
            System.err.println("  URL: " + System.getenv("DB_URL"));
            System.err.println("  Error: " + e.getMessage());
            return null;
        }
    }

    public boolean isConnected() {
        try (Connection testConn = getConnection()) {
            return testConn != null && !testConn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}