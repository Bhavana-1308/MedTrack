package com.medreminder.service;

import com.medreminder.exception.InvalidInputException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class providing input validation (throwing InvalidInputException)
 * and SHA-256 password hashing.
 */
public final class InputValidator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private InputValidator() {
    }

    public static String requireNonEmpty(String value, String fieldName) throws InvalidInputException {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidInputException(fieldName, fieldName + " cannot be empty.");
        }
        return value.trim();
    }

    public static String validateEmail(String email) throws InvalidInputException {
        String cleaned = requireNonEmpty(email, "Email");
        if (!cleaned.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new InvalidInputException("Email", "Please enter a valid email address (e.g., user@example.com).");
        }
        return cleaned;
    }

    public static String validatePassword(String password) throws InvalidInputException {
        if (password == null || password.length() < 6) {
            throw new InvalidInputException("Password", "Password must be at least 6 characters long.");
        }
        return password;
    }

    public static LocalDate parseDate(String text, String fieldName) throws InvalidInputException {
        String cleaned = requireNonEmpty(text, fieldName);
        try {
            return LocalDate.parse(cleaned, DATE_FMT);
        } catch (DateTimeParseException e) {
            throw new InvalidInputException(fieldName, fieldName + " must follow YYYY-MM-DD format (e.g., 2026-11-18).");
        }
    }

    public static void validateDateRange(LocalDate startDate, LocalDate endDate) throws InvalidInputException {
        if (endDate.isBefore(startDate)) {
            throw new InvalidInputException("End Date", "End Date cannot be earlier than Start Date.");
        }
    }

    public static LocalTime parseTime(String text) throws InvalidInputException {
        String cleaned = requireNonEmpty(text, "Reminder Time");
        try {
            return LocalTime.parse(cleaned, TIME_FMT);
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Reminder Time", "Reminder Time must be in 24-hour HH:mm format (e.g., 08:30 or 20:15).");
        }
    }

    /**
     * Computes a 64-character lowercase SHA-256 hex digest for secure password storage.
     */
    public static String hashPassword(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available in JVM.", e);
        }
    }
}
