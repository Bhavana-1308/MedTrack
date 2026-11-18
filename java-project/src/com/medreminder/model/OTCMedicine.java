package com.medreminder.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents an Over-The-Counter (OTC) medicine or supplement.
 * Demonstrates Inheritance and Runtime Polymorphism.
 */
public class OTCMedicine extends Medicine {

    private String purposeCategory;

    public OTCMedicine() {
        super();
    }

    public OTCMedicine(int id, int userId, String medicineName, String dosage,
                       String frequency, LocalDate startDate, LocalDate endDate,
                       String purposeCategory, String notes, LocalDateTime createdAt) {
        super(id, userId, medicineName, dosage, frequency, startDate, endDate, notes, createdAt);
        this.purposeCategory = purposeCategory;
    }

    public String getPurposeCategory() {
        return purposeCategory;
    }

    public void setPurposeCategory(String purposeCategory) {
        this.purposeCategory = purposeCategory;
    }

    @Override
    public String getMedicineType() {
        return "OTC";
    }

    @Override
    public String getDosageInstructions() {
        String cat = (purposeCategory != null && !purposeCategory.isBlank()) ? purposeCategory : "General Care";
        return "Over-the-Counter (" + cat + "). Follow label limits: "
                + getDosage() + ", " + getFrequency() + ".";
    }

    @Override
    public String getSubTypeDetail() {
        return (purposeCategory != null && !purposeCategory.isBlank()) ? purposeCategory : "Over-the-Counter";
    }
}
