package com.medreminder.dao;

import com.medreminder.exception.DatabaseOperationException;
import com.medreminder.model.User;
import com.medreminder.service.InputValidator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 * Data Access Object for user registration and authentication via JDBC.
 */
public class UserDAO {

    public boolean usernameExists(String username) throws DatabaseOperationException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to verify username availability.", e);
        }
    }

    public boolean emailExists(String email) throws DatabaseOperationException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to verify email availability.", e);
        }
    }

    /**
     * Registers a new user and populates the generated user_id on the User object.
     */
    public User registerUser(User user, String plainPassword) throws DatabaseOperationException {
        String hashedPassword = InputValidator.hashPassword(plainPassword);
        user.setPasswordHash(hashedPassword);

        String sql = "INSERT INTO users (full_name, username, email, password_hash, phone) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getEmail());
            ps.setString(4, hashedPassword);
            ps.setString(5, user.getPhone());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DatabaseOperationException("Creating user failed; no rows inserted.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
            return user;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to register user: " + e.getMessage(), e);
        }
    }

    /**
     * Authenticates a user by username or email and plain-text password.
     * Returns null if credentials do not match.
     */
    public User authenticate(String usernameOrEmail, String plainPassword) throws DatabaseOperationException {
        String sql = "SELECT user_id, full_name, username, email, password_hash, phone, created_at "
                   + "FROM users WHERE username = ? OR email = ?";
        String inputHash = InputValidator.hashPassword(plainPassword);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usernameOrEmail);
            ps.setString(2, usernameOrEmail);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (storedHash != null && storedHash.equals(inputHash)) {
                        Timestamp ts = rs.getTimestamp("created_at");
                        return new User(
                                rs.getInt("user_id"),
                                rs.getString("full_name"),
                                rs.getString("username"),
                                rs.getString("email"),
                                storedHash,
                                rs.getString("phone"),
                                ts != null ? ts.toLocalDateTime() : null
                        );
                    }
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Authentication query failed.", e);
        }
    }
}
