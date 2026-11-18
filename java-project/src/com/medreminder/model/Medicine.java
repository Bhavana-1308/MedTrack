package com.medreminder.model;

import com.medreminder.interfaces.Exportable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Abstract Medicine model demonstrating Abstraction, Encapsulation,
 * Inheritance (extends BaseEntity), and Interface implementation (Exportable).
 * Subclassed by PrescriptionMedicine and OTCMedicine for Polymorphism.
 */
public abstract class Medicine extends BaseEntity implements Exportable {

    private int userId;
    private String medicineName;
    private String dosage;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;

    protected Medicine() {
        super();
    }

    protected Medicine(int id, int userId, String medicineName, String dosage,
                       String frequency, LocalDate startDate, LocalDate endDate,
                       String notes, LocalDateTime createdAt) {
        super(id, createdAt);
        this.userId = userId;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.notes = notes;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActiveOn(LocalDate date) {
        if (startDate == null || endDate == null || date == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Polymorphic method returning subtype identifier ("PRESCRIPTION" or "OTC").
     */
    public abstract String getMedicineType();

    /**
     * Polymorphic method returning specialized clinical/safety guidance.
     */
    public abstract String getDosageInstructions();

    /**
     * Polymorphic method returning subtype-specific detail (doctor name or OTC category).
     */
    public abstract String getSubTypeDetail();

    @Override
    public String getEntityType() {
        return "Medicine";
    }

    @Override
    public String getSummary() {
        return medicineName + " (" + dosage + " - " + frequency + ")";
    }

    @Override
    public String getCsvHeader() {
        return "MedicineID,Name,Type,Dosage,Frequency,StartDate,EndDate,Detail,Notes";
    }

    @Override
    public String toCsvRow() {
        return String.format("%d,\"%s\",%s,\"%s\",\"%s\",%s,%s,\"%s\",\"%s\"",
                getId(),
                escapeCsv(medicineName),
                getMedicineType(),
                escapeCsv(dosage),
                escapeCsv(frequency),
                startDate,
                endDate,
                escapeCsv(getSubTypeDetail()),
                escapeCsv(notes != null ? notes : ""));
    }

    protected String escapeCsv(String value) {
        return value == null ? "" : value.replace("\"", "\"\"");
    }
}
