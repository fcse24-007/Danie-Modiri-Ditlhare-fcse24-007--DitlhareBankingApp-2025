package dao;

import model.Customer;
import model.CustomerType;
import model.UserRole;

import java.sql.*;
import java.util.List; // Omitted implementation for brevity

public class CustomerDAO implements BaseDAO<Customer, String> {

    // Assumes UserDAO exists and can handle base user authentication/details
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
        return List.of();
    }

    @Override
    public boolean update(Customer entity) {
        return false;
    }

    @Override
    public boolean delete(String s) {
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
        // NOTE: username and password_hash are from the joined 'users' table
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

    // Omitted implementations for update(), delete(), and findAll()
}