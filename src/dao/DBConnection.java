package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/ditlharebankdb";
    private static final String USER = "root";
    private static final String PASSWORD = "password";
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    private static DBConnection instance = null;
    private Connection connection;

    private DBConnection() {
        try {
            Class.forName(DRIVER_CLASS);
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connection established successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error connecting to the database. Check your URL, user, password, and ensure MySQL is running.");
            e.printStackTrace();
        }
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Returns the active Connection object, attempting to re-establish if necessary.
     * @return The active JDBC Connection.
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("Attempting to re-establish database connection...");
                instance = new DBConnection();
            }
        } catch (SQLException e) {
            System.err.println("Error validating database connection status.");
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Safely closes the database connection.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing the database connection.");
            e.printStackTrace();
        }
    }
}