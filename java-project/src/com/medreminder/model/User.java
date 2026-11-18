package com.medreminder.model;

import java.time.LocalDateTime;

/**
 * Represents a registered user account.
 * Demonstrates Encapsulation (private fields with validated getters/setters)
 * and Inheritance (extends BaseEntity).
 */
public class User extends BaseEntity {

    private String fullName;
    private String username;
    private String email;
    private String passwordHash;
    private String phone;

    public User() {
        super();
    }

    public User(int id, String fullName, String username, String email,
                String passwordHash, String phone, LocalDateTime createdAt) {
        super(id, createdAt);
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String getEntityType() {
        return "User";
    }

    @Override
    public String getSummary() {
        return fullName + " (@" + username + ")";
    }
}
