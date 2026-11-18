-- ============================================================================
-- Medicine Reminder and Management System - MySQL Database Schema
-- Compatible with MySQL 8.0+
-- ============================================================================

CREATE DATABASE IF NOT EXISTS medicine_reminder_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE medicine_reminder_db;

-- ----------------------------------------------------------------------------
-- 1. Users Table
-- Stores registered user accounts with SHA-256 hashed passwords.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(100) NOT NULL,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(64)  NOT NULL,
    phone         VARCHAR(20)  DEFAULT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 2. Medicines Table
-- Stores Prescription and Over-The-Counter (OTC) medicines per user.
-- Relationship: Many Medicines belong to One User (CASCADE on delete).
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS medicines (
    medicine_id      INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT          NOT NULL,
    medicine_name    VARCHAR(100) NOT NULL,
    medicine_type    ENUM('PRESCRIPTION', 'OTC') NOT NULL DEFAULT 'OTC',
    dosage           VARCHAR(50)  NOT NULL,
    frequency        VARCHAR(60)  NOT NULL,
    start_date       DATE         NOT NULL,
    end_date         DATE         NOT NULL,
    doctor_name      VARCHAR(100) DEFAULT NULL,
    purpose_category VARCHAR(100) DEFAULT NULL,
    notes            VARCHAR(255) DEFAULT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_medicines_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    INDEX idx_medicines_user_dates (user_id, start_date, end_date)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 3. Reminders Table
-- Stores scheduled daily reminder times for specific medicines.
-- Relationships:
--   - Many Reminders belong to One Medicine (CASCADE on delete).
--   - Many Reminders belong to One User (CASCADE on delete).
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reminders (
    reminder_id    INT AUTO_INCREMENT PRIMARY KEY,
    medicine_id    INT          NOT NULL,
    user_id        INT          NOT NULL,
    reminder_time  TIME         NOT NULL,
    reminder_label VARCHAR(100) DEFAULT 'Scheduled Dose',
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reminders_medicine
        FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_reminders_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    INDEX idx_reminders_user_active_time (user_id, is_active, reminder_time)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 4. Medicine_History Table
-- Tracks doses marked as TAKEN or MISSED (or PENDING) for daily logs.
-- Relationships:
--   - Many History entries belong to One Medicine (CASCADE on delete).
--   - Optional link to Reminder (SET NULL if reminder is later removed).
--   - Many History entries belong to One User (CASCADE on delete).
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS medicine_history (
    history_id     INT AUTO_INCREMENT PRIMARY KEY,
    medicine_id    INT          NOT NULL,
    reminder_id    INT          DEFAULT NULL,
    user_id        INT          NOT NULL,
    dose_date      DATE         NOT NULL,
    scheduled_time TIME         NOT NULL,
    taken_at       TIMESTAMP    NULL DEFAULT NULL,
    status         ENUM('TAKEN', 'MISSED', 'PENDING') NOT NULL DEFAULT 'PENDING',
    remarks        VARCHAR(255) DEFAULT NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_history_medicine
        FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_history_reminder
        FOREIGN KEY (reminder_id) REFERENCES reminders(reminder_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT fk_history_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    INDEX idx_history_user_date (user_id, dose_date),
    INDEX idx_history_status (user_id, status)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Optional Sample Seed Data (Password for demo user 'arjun_med': MedTrack@123)
-- SHA-256("MedTrack@123") = ec4e3cc7dbb74d271e0641b8d61cc939aaeb2fce07d694dd70d6ce087bb33966
-- ----------------------------------------------------------------------------
INSERT IGNORE INTO users (user_id, full_name, username, email, password_hash, phone)
VALUES (1, 'Arjun Sharma', 'arjun_med', 'arjun.sharma@example.edu',
        'ec4e3cc7dbb74d271e0641b8d61cc939aaeb2fce07d694dd70d6ce087bb33966', '9876543210');

INSERT IGNORE INTO medicines (medicine_id, user_id, medicine_name, medicine_type, dosage, frequency, start_date, end_date, doctor_name, purpose_category, notes)
VALUES
    (1, 1, 'Amoxicillin', 'PRESCRIPTION', '500 mg', 'Twice Daily', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'Dr. Meera Nair', NULL, 'Take after meals with water'),
    (2, 1, 'Vitamin D3', 'OTC', '1000 IU', 'Once Daily', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), NULL, 'Nutritional Supplement', 'Take with breakfast');

INSERT IGNORE INTO reminders (reminder_id, medicine_id, user_id, reminder_time, reminder_label, is_active)
VALUES
    (1, 1, 1, '08:30:00', 'After Breakfast', TRUE),
    (2, 1, 1, '20:30:00', 'After Dinner', TRUE),
    (3, 2, 1, '09:00:00', 'Morning Supplement', TRUE);

INSERT IGNORE INTO medicine_history (history_id, medicine_id, reminder_id, user_id, dose_date, scheduled_time, taken_at, status, remarks)
VALUES
    (1, 1, 1, 1, CURDATE(), '08:30:00', NOW(), 'TAKEN', 'Taken on time after breakfast'),
    (2, 2, 3, 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:00:00', NULL, 'MISSED', 'Forgot during morning commute');
