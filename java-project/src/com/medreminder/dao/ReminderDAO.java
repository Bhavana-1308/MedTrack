package com.medreminder.dao;

import com.medreminder.exception.DatabaseOperationException;
import com.medreminder.model.Reminder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Data Access Object for managing daily medicine reminders.
 */
public class ReminderDAO {

    public Reminder addReminder(Reminder reminder) throws DatabaseOperationException {
        String sql = "INSERT INTO reminders (medicine_id, user_id, reminder_time, reminder_label, is_active) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, reminder.getMedicineId());
            ps.setInt(2, reminder.getUserId());
            ps.setTime(3, Time.valueOf(reminder.getReminderTime()));
            ps.setString(4, reminder.getReminderLabel());
            ps.setBoolean(5, reminder.isActive());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    reminder.setId(keys.getInt(1));
                }
            }
            return reminder;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to create reminder.", e);
        }
    }

    public void toggleReminderStatus(int reminderId, int userId, boolean active)
            throws DatabaseOperationException {
        String sql = "UPDATE reminders SET is_active = ? WHERE reminder_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, reminderId);
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update reminder status.", e);
        }
    }

    public void deleteReminder(int reminderId, int userId) throws DatabaseOperationException {
        String sql = "DELETE FROM reminders WHERE reminder_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reminderId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete reminder.", e);
        }
    }

    public ArrayList<Reminder> getRemindersByUser(int userId) throws DatabaseOperationException {
        ArrayList<Reminder> list = new ArrayList<>();
        String sql = "SELECT r.*, m.medicine_name "
                   + "FROM reminders r "
                   + "INNER JOIN medicines m ON r.medicine_id = m.medicine_id "
                   + "WHERE r.user_id = ? "
                   + "ORDER BY r.reminder_time ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to load reminders.", e);
        }
    }

    /**
     * Fetches active reminders for medicines whose treatment window covers today's date.
     * Used by the background ReminderSchedulerThread.
     */
    public ArrayList<Reminder> getActiveRemindersForToday(int userId) throws DatabaseOperationException {
        ArrayList<Reminder> list = new ArrayList<>();
        String sql = "SELECT r.*, m.medicine_name "
                   + "FROM reminders r "
                   + "INNER JOIN medicines m ON r.medicine_id = m.medicine_id "
                   + "WHERE r.user_id = ? AND r.is_active = TRUE "
                   + "AND CURDATE() BETWEEN m.start_date AND m.end_date "
                   + "ORDER BY r.reminder_time ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to load today's active reminders.", e);
        }
    }

    private Reminder mapRow(ResultSet rs) throws SQLException {
        Time sqlTime = rs.getTime("reminder_time");
        Timestamp ts = rs.getTimestamp("created_at");
        Reminder r = new Reminder(
                rs.getInt("reminder_id"),
                rs.getInt("medicine_id"),
                rs.getInt("user_id"),
                sqlTime.toLocalTime(),
                rs.getString("reminder_label"),
                rs.getBoolean("is_active"),
                ts != null ? ts.toLocalDateTime() : null
        );
        r.setMedicineName(rs.getString("medicine_name"));
        return r;
    }
}
