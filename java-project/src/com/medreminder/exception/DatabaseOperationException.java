package com.medreminder.exception;

/**
 * Custom checked exception that wraps JDBC/SQL failures so the UI layer
 * can display clean, user-friendly error messages without exposing raw SQL internals.
 */
public class DatabaseOperationException extends Exception {

    public DatabaseOperationException(String message) {
        super(message);
    }

    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
