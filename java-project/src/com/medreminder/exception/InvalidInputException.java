package com.medreminder.exception;

/**
 * Custom checked exception thrown when user input fails validation rules
 * (e.g., empty required fields, invalid date ranges, malformed reminder time).
 */
public class InvalidInputException extends Exception {

    private final String fieldName;

    public InvalidInputException(String message) {
        super(message);
        this.fieldName = "General";
    }

    public InvalidInputException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
