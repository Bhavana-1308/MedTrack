package com.medreminder.model;

import com.medreminder.interfaces.Exportable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents a dose tracking record (TAKEN, MISSED, or PENDING) in medicine_history.
 * Implements Exportable for CSV file report generation.
 */
public class MedicineHistory extends BaseEntity implements Exportable {

    private int medicineId;
    private Integer reminderId;
    private int userId;
    private LocalDate doseDate;
    private LocalTime scheduledTime;
    private LocalDateTime takenAt;
    private String status; // "TAKEN", "MISSED", "PENDING"
    private String remarks;

    // Transient display helper
    private String medicineName;
    private String dosage;

    public MedicineHistory() {
        super();
    }

    public MedicineHistory(int id, int medicineId, Integer reminderId, int userId,
                           LocalDate doseDate, LocalTime scheduledTime, LocalDateTime takenAt,
                           String status, String remarks, LocalDateTime createdAt) {
        super(id, createdAt);
        this.medicineId = medicineId;
        this.reminderId = reminderId;
        this.userId = userId;
        this.doseDate = doseDate;
        this.scheduledTime = scheduledTime;
        this.takenAt = takenAt;
        this.status = status;
        this.remarks = remarks;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    public Integer getReminderId() {
        return reminderId;
    }

    public void setReminderId(Integer reminderId) {
        this.reminderId = reminderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDate getDoseDate() {
        return doseDate;
    }

    public void setDoseDate(LocalDate doseDate) {
        this.doseDate = doseDate;
    }

    public LocalTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public LocalDateTime getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(LocalDateTime takenAt) {
        this.takenAt = takenAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
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

    @Override
    public String getEntityType() {
        return "MedicineHistory";
    }

    @Override
    public String getSummary() {
        String name = medicineName != null ? medicineName : ("Medicine #" + medicineId);
        return doseDate + " " + scheduledTime + " - " + name + " [" + status + "]";
    }

    @Override
    public String getCsvHeader() {
        return "HistoryID,DoseDate,ScheduledTime,MedicineName,Dosage,Status,TakenAt,Remarks";
    }

    @Override
    public String toCsvRow() {
        return String.format("%d,%s,%s,\"%s\",\"%s\",%s,%s,\"%s\"",
                getId(),
                doseDate,
                scheduledTime,
                escape(medicineName != null ? medicineName : ("Medicine #" + medicineId)),
                escape(dosage != null ? dosage : ""),
                status,
                takenAt != null ? takenAt.toString() : "",
                escape(remarks != null ? remarks : ""));
    }

    private String escape(String text) {
        return text == null ? "" : text.replace("\"", "\"\"");
    }
}
