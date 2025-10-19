package dao;

import model.User;
import model.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements BaseDAO<User, String> {

    @Override
    public User findById(String userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- Core Implementations ---

    @Override
    public User save(User user) {
        // NOTE: In a real system, you'd use a more complex logic to save specialized
        // users (Customer, BankEmployee, Admin) and hash the password.
        String sql = "INSERT INTO users (user_id, username, password_hash, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUserId());
            stmt.setString(2, user.getUsername());
            // ⚠️ Placeholder: You MUST hash the password before storing it!
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getRole().name());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ... update and delete methods (similar structure to save/findById) ...

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public boolean update(User entity) {
        return false;
    }

    @Override
    public boolean delete(String s) {
        return false;
    }

    // --- Specialized Method ---
    public User findByUsernameAndPassword(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?"; // Again, use HASH!
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password); // Should be a hashed password

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Helper method to map a database row to a User object
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        // The User class is abstract/base, so we should return the correct subtype (Customer, Employee, Admin)
        // This requires joining tables, or using different DAOs for each subtype.
        // For simplicity here, we return the base User.
        return new User(
                rs.getString("user_id"),
                rs.getString("username"),
                rs.getString("password_hash"), // Stored password (should be hash)
                UserRole.valueOf(rs.getString("role"))
        );
    }
}