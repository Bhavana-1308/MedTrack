package com.medreminder.model;

import java.time.LocalDateTime;

/**
 * Abstract base class demonstrating Abstraction and Inheritance.
 * Every domain entity shares an identifier, creation timestamp,
 * and a polymorphic summary representation.
 */
public abstract class BaseEntity {

    private int id;
    private LocalDateTime createdAt;

    protected BaseEntity() {
        this.createdAt = LocalDateTime.now();
    }

    protected BaseEntity(int id, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("Entity ID cannot be negative.");
        }
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns the domain type name (e.g., "User", "Medicine", "Reminder").
     */
    public abstract String getEntityType();

    /**
     * Returns a concise one-line human-readable summary of the entity.
     */
    public abstract String getSummary();
}
