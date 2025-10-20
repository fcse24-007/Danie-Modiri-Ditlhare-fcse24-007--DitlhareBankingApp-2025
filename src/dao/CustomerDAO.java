package dao;

import model.Customer;
import model.CustomerType;
import model.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO implements BaseDAO<Customer, String> {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public Customer findById(String customerId) {
        String sql = "SELECT u.user_id, u.username, u.password_hash, c.* " +
                "FROM users u JOIN customers c ON u.user_id = c.user_id " +
                "WHERE c.customer_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractCustomerFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT u.user_id, u.username, u.password_hash, c.* " +
                "FROM users u JOIN customers c ON u.user_id = c.user_id";
        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                customers.add(extractCustomerFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    @Override
    public boolean update(Customer customer) {
        // Update the base User part first
        userDAO.update(customer);

        // Then update Customer-specific fields
        String sql = "UPDATE customers SET first_name = ?, address = ?, phone_number = ?, email = ?, customer_type = ? WHERE customer_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getFirstName());
            stmt.setString(2, customer.getAddress());
            stmt.setString(3, customer.getPhoneNumber());
            stmt.setString(4, customer.getEmail());
            stmt.setString(5, customer.getCustomerType().name());
            stmt.setString(6, customer.getCustomerId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(String customerId) {
        // First get the customer to find their user_id
        Customer customer = findById(customerId);
        if (customer == null) return false;

        // Delete from customers table first (due to foreign key constraints)
        String deleteCustomerSQL = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteCustomerSQL)) {

            stmt.setString(1, customerId);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                // Then delete from users table
                return userDAO.delete(customer.getUserId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Customer save(Customer customer) {
        // 1. Save the base User portion first
        customer.setRole(UserRole.CUSTOMER);
        userDAO.save(customer);

        // 2. Save the Customer-specific fields
        String sql = "INSERT INTO customers (customer_id, user_id, first_name, address, phone_number, email, customer_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getCustomerId());
            stmt.setString(2, customer.getUserId());
            stmt.setString(3, customer.getFirstName());
            stmt.setString(4, customer.getAddress());
            stmt.setString(5, customer.getPhoneNumber());
            stmt.setString(6, customer.getEmail());
            stmt.setString(7, customer.getCustomerType().name());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return customer;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Helper method to map a database row to a Customer object
    private Customer extractCustomerFromResultSet(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getString("user_id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("customer_id"),
                rs.getString("first_name"),
                rs.getString("address"),
                rs.getString("phone_number"),
                rs.getString("email"),
                CustomerType.valueOf(rs.getString("customer_type"))
        );
    }
}