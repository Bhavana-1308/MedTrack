package com.medreminder.model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a scheduled daily reminder for a specific medicine.
 */
public class Reminder extends BaseEntity {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private int medicineId;
    private int userId;
    private LocalTime reminderTime;
    private String reminderLabel;
    private boolean active;

    // Transient display helper loaded via JOIN or HashMap lookup
    private String medicineName;

    public Reminder() {
        super();
        this.active = true;
    }

    public Reminder(int id, int medicineId, int userId, LocalTime reminderTime,
                    String reminderLabel, boolean active, LocalDateTime createdAt) {
        super(id, createdAt);
        this.medicineId = medicineId;
        this.userId = userId;
        this.reminderTime = reminderTime;
        this.reminderLabel = reminderLabel;
        this.active = active;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(LocalTime reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getFormattedTime() {
        return reminderTime != null ? reminderTime.format(TIME_FMT) : "--:--";
    }

    public String getReminderLabel() {
        return reminderLabel;
    }

    public void setReminderLabel(String reminderLabel) {
        this.reminderLabel = reminderLabel;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    @Override
    public String getEntityType() {
        return "Reminder";
    }

    @Override
    public String getSummary() {
        String name = medicineName != null ? medicineName : ("Medicine #" + medicineId);
        return getFormattedTime() + " - " + name + " (" + reminderLabel + ")";
    }
}
