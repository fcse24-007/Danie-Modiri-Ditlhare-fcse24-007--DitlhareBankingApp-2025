package database;

import model.Customer;
import model.CustomerType;
import model.User;
import model.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDAO implements DAO<Customer> {

    private UserDAO userDAO = new UserDAO();
    
    public UserDAO getUserDAO() {
        return userDAO;
    }

    @Override
    public Optional<Customer> findById(String customerId) {
        String sql = """
            SELECT c.*, u.user_id, u.username, u.password, u.role 
            FROM customers c 
            JOIN users u ON c.user_id = u.user_id 
            WHERE c.customer_id = ?
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User(
                    rs.getString("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    UserRole.valueOf(rs.getString("role"))
                );
                
                Customer customer = new Customer(
                    user.getUserId(),
                    user.getUsername(),
                    user.getPassword(),
                    rs.getString("customer_id"),
                    rs.getString("first_name"),
                    rs.getString("surname"),
                    rs.getString("address"),
                    rs.getString("phone_number"),
                    rs.getString("email"),
                    CustomerType.valueOf(rs.getString("customer_type"))
                );
                
                return Optional.of(customer);
            }
        } catch (SQLException e) {
            System.err.println("Error finding customer by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Customer> findByUserId(String userId) {
        String sql = """
            SELECT c.*, u.user_id, u.username, u.password, u.role 
            FROM customers c 
            JOIN users u ON c.user_id = u.user_id 
            WHERE c.user_id = ?
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User(
                    rs.getString("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    UserRole.valueOf(rs.getString("role"))
                );
                
                Customer customer = new Customer(
                    user.getUserId(),
                    user.getUsername(),
                    user.getPassword(),
                    rs.getString("customer_id"),
                    rs.getString("first_name"),
                    rs.getString("surname"),
                    rs.getString("address"),
                    rs.getString("phone_number"),
                    rs.getString("email"),
                    CustomerType.valueOf(rs.getString("customer_type"))
                );
                
                return Optional.of(customer);
            }
        } catch (SQLException e) {
            System.err.println("Error finding customer by user ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        String sql = """
            SELECT c.*, u.user_id, u.username, u.password, u.role 
            FROM customers c 
            JOIN users u ON c.user_id = u.user_id
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = new User(
                    rs.getString("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    UserRole.valueOf(rs.getString("role"))
                );
                
                Customer customer = new Customer(
                    user.getUserId(),
                    user.getUsername(),
                    user.getPassword(),
                    rs.getString("customer_id"),
                    rs.getString("first_name"),
                    rs.getString("surname"),
                    rs.getString("address"),
                    rs.getString("phone_number"),
                    rs.getString("email"),
                    CustomerType.valueOf(rs.getString("customer_type"))
                );
                
                customers.add(customer);
            }
        } catch (SQLException e) {
            System.err.println("Error finding all customers: " + e.getMessage());
        }
        return customers;
    }

    @Override
    public boolean save(Customer customer) {
        // First save the user part
        User user = new User(
            customer.getUserId(),
            customer.getUsername(),
            customer.getPassword(),
            UserRole.CUSTOMER
        );
        
        if (!userDAO.save(user)) {
            return false;
        }
        
        // Then save the customer specific data
        String sql = """
            INSERT INTO customers (customer_id, user_id, first_name, surname, address, phone_number, email, customer_type) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, customer.getCustomerId());
            stmt.setString(2, customer.getUserId());
            stmt.setString(3, customer.getFirstName());
            stmt.setString(4, customer.getSurname());
            stmt.setString(5, customer.getAddress());
            stmt.setString(6, customer.getPhoneNumber());
            stmt.setString(7, customer.getEmail());
            stmt.setString(8, customer.getCustomerType().toString());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving customer: " + e.getMessage());
            // Rollback user creation
            userDAO.delete(customer.getUserId());
            return false;
        }
    }

    @Override
    public boolean update(Customer customer) {
        // Update user part
        User user = new User(
            customer.getUserId(),
            customer.getUsername(),
            customer.getPassword(),
            UserRole.CUSTOMER
        );
        
        if (!userDAO.update(user)) {
            return false;
        }
        
        // Update customer part
        String sql = """
            UPDATE customers SET first_name = ?, surname = ?, address = ?, phone_number = ?, email = ?, customer_type = ? 
            WHERE customer_id = ?
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, customer.getFirstName());
            stmt.setString(2, customer.getSurname());
            stmt.setString(3, customer.getAddress());
            stmt.setString(4, customer.getPhoneNumber());
            stmt.setString(5, customer.getEmail());
            stmt.setString(6, customer.getCustomerType().toString());
            stmt.setString(7, customer.getCustomerId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating customer: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String customerId) {
        // First get the user_id
        String getUserIdSql = "SELECT user_id FROM customers WHERE customer_id = ?";
        String deleteCustomerSql = "DELETE FROM customers WHERE customer_id = ?";
        String deleteUserSql = "DELETE FROM users WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            // Get user ID
            String userId = null;
            try (PreparedStatement stmt = conn.prepareStatement(getUserIdSql)) {
                stmt.setString(1, customerId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getString("user_id");
                }
            }
            
            // Delete customer
            try (PreparedStatement stmt = conn.prepareStatement(deleteCustomerSql)) {
                stmt.setString(1, customerId);
                stmt.executeUpdate();
            }
            
            // Delete user using same connection
            if (userId != null) {
                try (PreparedStatement stmt = conn.prepareStatement(deleteUserSql)) {
                    stmt.setString(1, userId);
                    stmt.executeUpdate();
                }
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting customer: " + e.getMessage());
            return false;
        }
    }
}
