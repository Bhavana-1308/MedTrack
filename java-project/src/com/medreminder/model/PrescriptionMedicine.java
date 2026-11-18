package com.medreminder.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a doctor-prescribed medicine.
 * Demonstrates Inheritance and Runtime Polymorphism by overriding
 * abstract methods from Medicine.
 */
public class PrescriptionMedicine extends Medicine {

    private String doctorName;

    public PrescriptionMedicine() {
        super();
    }

    public PrescriptionMedicine(int id, int userId, String medicineName, String dosage,
                                String frequency, LocalDate startDate, LocalDate endDate,
                                String doctorName, String notes, LocalDateTime createdAt) {
        super(id, userId, medicineName, dosage, frequency, startDate, endDate, notes, createdAt);
        this.doctorName = doctorName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    @Override
    public String getMedicineType() {
        return "PRESCRIPTION";
    }

    @Override
    public String getDosageInstructions() {
        String doctor = (doctorName != null && !doctorName.isBlank()) ? doctorName : "your physician";
        return "Prescription required (" + doctor + "). Complete full course as directed: "
                + getDosage() + ", " + getFrequency() + ".";
    }

    @Override
    public String getSubTypeDetail() {
        return (doctorName != null && !doctorName.isBlank()) ? "Prescribed by " + doctorName : "Prescription";
    }
}
